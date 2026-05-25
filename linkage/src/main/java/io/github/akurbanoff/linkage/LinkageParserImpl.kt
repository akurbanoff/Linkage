/*
 * Copyright 2026 Artem Kurbanov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.akurbanoff.linkage

import io.github.akurbanoff.linkage.LinkageParserImpl.parse
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmName

/**
 * Основной объект библиотеки для разбора deep link URI.
 *
 * Публичным остаётся методы [parse] — они принимают URI и тип,
 * размеченный аннотациями [LinkageDeepLink], и возвращает готовый объект.
 */
object LinkageParserImpl {

    /**
     * Разбирает переданный URI в экземпляр типа [T].
     *
     * Использование:
     * ```kotlin
     * val person = LinkageParserImpl.parse<Person>("app://person/42".toUri())
     * ```
     *
     * @param uri Ссылка deep link, которую нужно разобрать. Может быть null.
     * @return Экземпляр класса с заполненными полями, или null, если разбор не удался.
     */
    inline fun <reified T : Any> parse(
        uriString: String?,
        noinline doOnError: (e: Exception, constructorName: String?) -> Unit = { _, _ -> },
    ): T? {
        if (uriString == null) return null

        val kClass = T::class

        return if (kClass.isSealed) {
            parseSealedClass(kClass, uriString, doOnError)
        } else {
            parseObject(kClass, uriString, doOnError)
        }
    }

    /**
     * Обрабатывает sealed-класс: перебирает все его прямые наследники,
     * пытаясь найти тот, чей шаблон из [LinkageDeepLink] совпадает с [uriString].
     *
     *
     * @param kClass Sealed-класс, подклассы которого проверяются.
     * @param uriString Строковое представление URI.
     * @return Экземпляр подходящего подкласса или null.
     */
    fun <T : Any> parseSealedClass(
        kClass: KClass<T>,
        uriString: String,
        doOnError: (e: Exception, constructorName: String?) -> Unit,
    ): T? {
        // Перебираем все прямые наследники sealed-класса
        for (subclass in kClass.sealedSubclasses) {
            val annotation = subclass.findAnnotation<LinkageDeepLink>() ?: continue
            val patternUrl = annotation.url
            val mayContainLinkParams = annotation.mayContainLinkParams.toSet()

            val regex = patternToRegex(patternUrl, mayContainLinkParams)
            val matchResult = regex.matchEntire(uriString) ?: continue

            val placeholderNames = extractPlaceholderNames(patternUrl)
            val valuesMap = mutableMapOf<String, String>()
            for (name in placeholderNames) {
                valuesMap[name] = matchResult.groups[name]?.value ?: ""
            }

            val constructor = subclass.primaryConstructor ?: return subclass.objectInstance
            val args = getParamsForConstructor(constructor, valuesMap)

            if (args == null || args.isEmpty()) {
                doOnError(ConstructorParamsException(kClass.jvmName), constructor.name)
                continue
            }

            try {
                return constructor.callBy(args)
            } catch (e: Exception) {
                doOnError(e, constructor.name)
                continue
            }
        }
        return null
    }

    /**
     * Обрабатывает обычный класс (не sealed): берёт его аннотацию [LinkageDeepLink],
     * проверяет совпадение с [uriString] и создаёт экземпляр через primary конструктор.
     *
     * @param kClass Класс, аннотированный [LinkageDeepLink].
     * @param uriString Строковое представление URI.
     * @return Экземпляр класса или null.
     */
    fun <T : Any> parseObject(
        kClass: KClass<T>,
        uriString: String,
        doOnError: (e: Exception, constructorName: String?) -> Unit,
    ): T? {
        val annotation = kClass.findAnnotation<LinkageDeepLink>()
        if (annotation == null) {
            doOnError(AnnotationException(kClassName = kClass.jvmName), kClass.jvmName)
            return null
        }
        val patternUrl = annotation.url
        val mayContainLinkParams = annotation.mayContainLinkParams.toSet()

        val regex = patternToRegex(patternUrl, mayContainLinkParams)
        val matchResult = regex.matchEntire(uriString)
        if (matchResult == null) {
            doOnError(MatchUrlException(uriString), kClass.jvmName)
            return null
        }

        val placeholderNames = extractPlaceholderNames(patternUrl)
        val valuesMap = mutableMapOf<String, String>()
        for (name in placeholderNames) {
            valuesMap[name] = matchResult.groups[name]?.value ?: ""
        }

        val constructor = kClass.primaryConstructor ?: return kClass.objectInstance
        val args = getParamsForConstructor(constructor, valuesMap)

        if (args == null || args.isEmpty()) {
            doOnError(ConstructorParamsException(kClass.jvmName), constructor.name)
            return null
        }

        return try {
            constructor.callBy(args)
        } catch (e: Exception) {
            doOnError(e, constructor.name)
            null
        }
    }

    private fun <T : Any> getParamsForConstructor(
        constructor: KFunction<T>,
        valuesMap: Map<String, String>,
    ): Map<KParameter, Any?>? {
        val args = mutableMapOf<KParameter, Any?>()
        for (param in constructor.parameters) {
            val paramName = param.name ?: return null
            val stringValue = valuesMap[paramName]
            if (stringValue != null) {
                val type = param.type.classifier as? KClass<*> ?: return null
                val converted = convertOrConstruct(stringValue, type, valuesMap)
                if (converted == null) return null
                args[param] = converted
            } else if (!param.isOptional) {
                return null
            }
        }
        return args
    }

    /**
     * Пытается создать объект типа [type] из строки [value].
     * Сначала пробует стандартные типы (String, Int...), затем рекурсивно
     * собирает объект через primary конструктор, используя [valuesMap].
     * Если не удалось (например, параметры конструктора не соответствуют ключам),
     * для одного параметра пытается передать [value] напрямую.
     */
    private fun convertOrConstruct(
        value: String,
        type: KClass<*>,
        valuesMap: Map<String, String>,
    ): Any? {
        convertValue(value, type)?.let { return it }

        val constructor = type.primaryConstructor ?: return null
        if (constructor.parameters.isEmpty()) return null

        val nestedArgs = getParamsForConstructor(constructor, valuesMap)
        if (nestedArgs != null) {
            return try {
                constructor.callBy(nestedArgs)
            } catch (e: Exception) {
                null
            }
        }

        if (constructor.parameters.size == 1) {
            val param = constructor.parameters.single()
            val paramType = param.type.classifier as? KClass<*>
            val convertedInner = convertOrConstruct(value, paramType!!, valuesMap)
                ?: return null
            return try {
                constructor.call(convertedInner)
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    /**
     * Преобразует шаблон URL с плейсхолдерами вида `{name}` в регулярное выражение.
     *
     * Пример:
     * `app://person/{id}` → `^app://person/(?<id>[^/?#]+)$`
     */
    internal fun patternToRegex(pattern: String, linkParams: Set<String> = emptySet()): Regex {
        val sb = StringBuilder()
        val placeholderRegex = Regex("\\{(\\w+)\\}")
        var lastIndex = 0

        for (match in placeholderRegex.findAll(pattern)) {
            val literal = pattern.substring(lastIndex, match.range.first)
            sb.append(Regex.escape(literal))

            val placeholderName = match.groupValues[1]
            val capture = if (placeholderName in linkParams) {
                "(?<$placeholderName>.+)"   // жадный захват для параметра-ссылки
            } else {
                "(?<$placeholderName>[^/?#]+)"
            }
            sb.append(capture)
            lastIndex = match.range.last + 1
        }
        sb.append(Regex.escape(pattern.substring(lastIndex)))
        return Regex("^${sb}$")
    }

    /**
     * Преобразует строковое значение из URI в целевой тип конструктора.
     *
     * Поддерживает: String, Int, Long, Float, Double, Boolean.
     * При неудаче конвертации возвращает null.
     */
    internal fun convertValue(value: String, type: KClass<*>?): Any? {
        return when (type) {
            String::class -> value
            Int::class -> value.toIntOrNull()
            Long::class -> value.toLongOrNull()
            Float::class -> value.toFloatOrNull()
            Double::class -> value.toDoubleOrNull()
            Boolean::class -> value.toBooleanStrictOrNull()
            else -> null
        }
    }

    /**
     * Извлекает список имён плейсхолдеров из шаблона URL.
     *
     * Например, для `app://note/{id}/{title}` вернёт `["id", "title"]`.
     */
    internal fun extractPlaceholderNames(pattern: String): List<String> {
        val placeholderRegex = Regex("\\{(\\w+)\\}")
        return placeholderRegex.findAll(pattern).map { it.groupValues[1] }.toList()
    }
}