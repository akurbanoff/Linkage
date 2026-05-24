package com.akurbanoff.linkage

import io.github.akurbanoff.linkage.LinkageDeepLink

sealed interface DeepLinkHierarchy

@LinkageDeepLink("myapp://user/{name}/profile")
data class User(
    val name: String,
) : DeepLinkHierarchy

@LinkageDeepLink("app://person/{name}")
data class PersonNameDeepLink(val name: String) : DeepLinkHierarchy

@LinkageDeepLink("app://person/{id}/{name}")
data class Person(
    val id: Int,
    val name: String,
) : DeepLinkHierarchy

@LinkageDeepLink("app://home")
data object Home : DeepLinkHierarchy

@LinkageDeepLink("app://contacts")
data object Contacts : DeepLinkHierarchy

@LinkageDeepLink("elementx://open/{userId}/{roomId}")
data class Room(
    val userId: String,
    val roomId: String,
) : DeepLinkHierarchy

@LinkageDeepLink("app://person/{id}/{name}/{age}/{timestamp}/{isNew}")
data class BigPerson(
    val id: Int,
    val name: String,
    val age: Double,
    val timestamp: Long,
    val isNew: Boolean,
) : DeepLinkHierarchy

@LinkageDeepLink(
    url = "app://note/{id}?source={source}",
    mayContainLinkParams = ["source"]
)
data class Note(
    val id: Int,
    val source: String,
) : DeepLinkHierarchy

@LinkageDeepLink(
    url = "app://note/{id}?source={source}",
    mayContainLinkParams = ["source"]
)
data class Notes(
    val id: Int,
    val source: String,
)

@LinkageDeepLink(
    url = "app://note/{id}/{source}/{name}",
    mayContainLinkParams = ["source"]
)
data class Item(
    val id: Int,
    val source: String,
    val name: String,
)

@LinkageDeepLink(
    url = "app://note/{id}/{name}?source={source}",
    mayContainLinkParams = ["source"]
)
data class Items(
    val id: Int,
    val source: String,
    val name: String,
)