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

import android.net.Uri
import io.github.akurbanoff.linkage.LinkageParserImpl.extractPlaceholderNames
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object LinkageUriConverter {
    fun toUri(link: Any?): Uri? {
        val resultUrl = toUriString(link)
        return Uri.parse(resultUrl)
    }

    /**
     * Превращает объект, класс которого аннотирован [LinkageDeepLink], в [Uri],
     * подставляя значения его свойств в шаблон аннотации.
     *
     * @param link объект (data-класс или экземпляр sealed-иерархии)
     * @return Uri, если аннотация найдена и все плейсхолдеры заполнены, иначе null
     */
    fun toUriString(link: Any?): String? {
        if (link == null) return null

        val kClass = link::class
        val annotation = kClass.findAnnotation<LinkageDeepLink>() ?: return null
        val patternUrl = annotation.url
        val placeholderNames = extractPlaceholderNames(patternUrl)

        var resultUrl = patternUrl
        for (name in placeholderNames) {
            val property = kClass.memberProperties.firstOrNull { it.name == name }
                ?: return null

            @Suppress("UNCHECKED_CAST")
            val value = (property as KProperty1<Any, *>).get(link)
            val uriValue = unwrapToUriString(value) ?: return null
            resultUrl = resultUrl.replace("{$name}", uriValue)
        }

        return resultUrl
    }

    /**
     * Рекурсивно "разворачивает" объект до его примитивного строкового представления.
     * Для объектов с одним параметром конструктора (value class) извлекает внутреннее значение,
     * для примитивов и строк возвращает их toString().
     */
    private fun unwrapToUriString(value: Any?): String? {
        if (value == null) return null
        val kClass = value::class
        when (kClass) {
            String::class, Int::class, Long::class, Float::class,
            Double::class, Boolean::class,
                -> return value.toString()
        }

        val constructor = kClass.primaryConstructor ?: return null
        if (constructor.parameters.size != 1) return null
        val param = constructor.parameters.single()
        val property = kClass.memberProperties.firstOrNull { it.name == param.name }
            ?: return null
        val innerValue = (property as KProperty1<Any, *>).get(value)
        return unwrapToUriString(innerValue)
    }
}