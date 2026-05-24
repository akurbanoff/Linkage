package com.akurbanoff.linkage

import io.github.akurbanoff.linkage.provideLinkageParser
import io.github.akurbanoff.linkage.provideLinkageUriConverter
import org.junit.Assert.assertEquals
import org.junit.Test


class LinkageUriConverterTest {

    private val linkageParser = provideLinkageParser()
    private val linkageUriConverter = provideLinkageUriConverter()

    @Test
    fun test_uri_converter() {
        val deeplink = linkageParser.parse<DeepLinkHierarchy>("myapp://user/artem/profile")
        val uri = linkageUriConverter.toUriString(deeplink)

        assertEquals("myapp://user/artem/profile", uri)
    }

    @Test
    fun test_uri_converter_with_link() {
        val deeplink = linkageParser.parse<Items>("app://note/5/artem?source=https://google.com/")
        val uri = linkageUriConverter.toUriString(deeplink)

        assertEquals("app://note/5/artem?source=https://google.com/", uri)
    }
}