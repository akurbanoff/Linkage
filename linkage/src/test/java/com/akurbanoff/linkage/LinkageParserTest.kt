package com.akurbanoff.linkage

import io.github.akurbanoff.linkage.provideLinkageParser
import org.junit.Assert.assertEquals
import org.junit.Test

class LinkageParserTest {
    private val linkageParser = provideLinkageParser()

    @Test
    fun test_parse_deeplink_with_one_param() {
        val person = linkageParser.parse<DeepLinkHierarchy>("app://person/artem")
        assert(person is PersonNameDeepLink)
    }

    @Test
    fun test_deeplink_with_two_params() {
        val person = linkageParser.parse<DeepLinkHierarchy>("app://person/5/artem")

        assert(person is Person)
        assertEquals(5, (person as Person).id)
        assertEquals("artem", (person as Person).name)
    }

    @Test
    fun test_real_example_of_deeplink() {
        val room =
            linkageParser.parse<DeepLinkHierarchy>("elementx://open/@alice:server.org/!aRoomId:domain")

        assert(room is Room)
        assertEquals("@alice:server.org", (room as Room).userId)
        assertEquals("!aRoomId:domain", (room as Room).roomId)
    }

    @Test
    fun test_deeplink_without_params() {
        val home = linkageParser.parse<DeepLinkHierarchy>("app://home")

        assert(home is Home)
    }

    @Test
    fun test_different_deeplinks_without_params() {
        val home = linkageParser.parse<DeepLinkHierarchy>("app://home")
        val contacts = linkageParser.parse<DeepLinkHierarchy>("app://contacts")

        assert(home is Home)
        assert(contacts is Contacts)
    }

    @Test
    fun test_params_doesnt_match() {
        val person = linkageParser.parse<DeepLinkHierarchy>("app://person/artem/5")

        assert(person == null)
    }

    @Test
    fun test_multiple_params_with_different_types() {
        val person = linkageParser.parse<DeepLinkHierarchy>(
            "app://person/5/artem/22/12314151915/true"
        )

        assert(person is BigPerson)
        assertEquals(5, (person as BigPerson).id)
        assertEquals("artem", person.name)
        assertEquals(22.0, person.age, 1.0)
        assertEquals(12314151915L, person.timestamp)
        assertEquals(true, person.isNew)
    }

    @Test
    fun test_pattern_and_direct_parsing() {
        val note = linkageParser.parse<Note>("app://note/10?source=screen")

        assert(note != null)
        assertEquals(10, note?.id)
        assertEquals("screen", note?.source)
    }

    @Test
    fun test_pattern() {
        val deeplink = linkageParser.parse<DeepLinkHierarchy>("myapp://user/artem/profile")

        assert(deeplink is User)
        assertEquals("artem", (deeplink as User).name)
    }

    @Test
    fun test_parsing_link_in_values() {
        val deepLink =
            linkageParser.parse<DeepLinkHierarchy>("app://note/5?source=https://google.com/")

        assert(deepLink is Note)
        assertEquals("https://google.com/", (deepLink as Note).source)
    }

    @Test
    fun test_parsing_link_in_object_values() {
        val deepLink = linkageParser.parse<Notes>("app://note/5?source=https://google.com/")

        assert(deepLink is Notes)
        assertEquals("https://google.com/", (deepLink as Notes).source)
    }

    @Test
    fun test_parsing_link_in_values_2() {
        val deepLink = linkageParser.parse<Item>("app://note/5/https://google.com//artem")

        assert(deepLink is Item)
        assertEquals("https://google.com/", (deepLink as Item).source)
        assertEquals("artem", (deepLink as Item).name)
    }

    @Test
    fun test_parsing_link_in_values_3() {
        val deepLink = linkageParser.parse<Items>("app://note/5/artem?source=https://google.com/")

        assert(deepLink is Items)
        assertEquals("https://google.com/", (deepLink as Items).source)
        assertEquals("artem", (deepLink as Items).name)
    }
}