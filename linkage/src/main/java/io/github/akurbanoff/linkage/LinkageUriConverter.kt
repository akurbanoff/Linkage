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
            val value = (property as KProperty1<Any, *>).get(link)?.toString() ?: return null
            resultUrl = resultUrl.replace("{$name}", value)
        }

        return resultUrl
    }
}