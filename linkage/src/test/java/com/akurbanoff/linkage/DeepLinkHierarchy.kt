package com.akurbanoff.linkage

import io.github.akurbanoff.linkage.LinkageDeepLink

data class SessionId(val sessionId: UserId)

data class UserId(val userId: String)

data class Token(val token: String)

data class Profile(
    val sessionId: SessionId,
    val info: ProfileInfo,
)

data class ProfileInfo(
    val name: String,
    val age: Int,
    val isAdmin: Boolean,
)

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

@LinkageDeepLink(
    url = "app://session/{sessionId}"
)
data class Session(
    val sessionId: SessionId,
)

@LinkageDeepLink(
    url = "app://temporary_user/{sessionId}/{token}"
)
data class TemporaryUser(
    val sessionId: SessionId,
    val token: Token,
)

@LinkageDeepLink(
    url = "app://session/{sessionId}"
)
data class SessionDeeplink(
    val sessionId: SessionId,
) : DeepLinkHierarchy

//@LinkageDeepLink(
//    url = "app://profile/{sessionId}/{profile}"
//)
data class ProfileDeeplink(
    val profile: Profile,
)