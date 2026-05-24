# Linkage — Deep Link Parsing Library for Kotlin (Android)

## Overview

Linkage simplifies deep link handling in Android apps by automatically parsing URIs into Kotlin data
classes or sealed class hierarchies. You define URL patterns with placeholders using the
`@LinkageDeepLink` annotation, and the library does the rest — extracting parameters, converting
types, and constructing objects.

## Installation

Add the library to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.akurbanoff:linkage:0.0.1")
}
```

## Getting Started

### 1. Define a Deep Link Pattern

Annotate a class or sealed class with `@DeepLink("url_pattern")`. Use curly braces `{param}` to
declare placeholders that will be extracted from the URI and passed to the constructor parameters
with matching names.

```kotlin
sealed interface DeepLinkHierarchy

@LinkageDeepLink("app://person/{id}/{name}")
data class Person(
    val id: Int,
    val name: String
) : DeepLinkHierarchy

@LinkageDeepLink("app://room/{id}/")
data class Room(
    val id: Int
) : DeepLinkHierarchy

@LinkageDeepLink(
    url = "app://note/{id}?source={source}",
    mayContainLinkParams = ["source"]
)
data class Notes(
    val id: Int,
    val source: String
)
```

### 2. Parse a URI

Call `LinkageParserImpl.parse<YourClass>(uri)`. The function returns `null` if the URI doesn't match
any pattern or if parameter extraction fails.

```kotlin
private val linkageParser = provideLinkageParser()

val uri = Uri.parse("app://person/42/alex")
val person = linkageParser.parse<Person>(uri.toString())

if (person != null) {
    println("Person ID: ${person.id}")
}

override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
    super.onNewIntent(intent, caller)

    val deeplink = linkageParser.parse<DeepLinkHierarchy>(intent.data)

    when (deeplink) {
        is Person -> println("Person ID: ${deeplink.id}")
        is Room -> println("Room ID: ${deeplink.id}")
    }
}
```

## Supported Types for Placeholder Conversion

The library can convert string values from URIs into the following Kotlin types:

- `String`
- `Int`
- `Long`
- `Float`
- `Double`
- `Boolean` (strict: `"true"` → `true`, `"false"` → `false`, others → `null`)

If conversion fails, the whole parse returns `null`.

## Using Sealed Classes for Multiple Patterns

Sealed classes allow you to handle different deep link formats in a type-safe way.  
Annotate each subclass with its own `@DeepLink` pattern.

```kotlin
sealed class Screen {
    @LinkageDeepLink("app://profile/{userId}")
    data class Profile(val userId: String) : Screen()

    @LinkageDeepLink("app://settings/{section}")
    data class Settings(val section: String = "general") : Screen()

    @LinkageDeepLink("app://home")
    object Home : Screen()
}
```

Parsing returns the appropriate subclass:

```kotlin
val screen = linkageParser.parse<Screen>(Uri.parse("app://profile/abc123"))
when (screen) {
    is Screen.Profile -> println("User: ${screen.userId}")
    is Screen.Settings -> println("Section: ${screen.section}")
    Screen.Home -> println("Home screen")
    null -> println("Unknown deep link")
}
```

## Optional Constructor Parameters

Parameters that have default values are **optional**. If the placeholder is missing from the URI,
the default value will be used. If a required parameter (without a default) is absent, parsing
returns `null`.

## Pattern Format Rules

- Patterns must begin with a scheme (e.g., `app://`).
- Placeholders are written as `{name}` and match any non‑empty sequence of characters except `/`,
  `?`, `#`.
- Query parameters and fragments are ignored during matching.
- The same placeholder name can appear only once per pattern.
- Name deeplink url and params exacts same name (e.g., `{isNew}` == `val isNew: Boolean`)

Examples of valid patterns:

- `app://item/{id}`
- `myapp://user/{name}/profile`
- `app://note/{id}?source={source}`

## Complete Usage Example

```kotlin
// 1. Define your classes
@LinkageDeepLink("app://product/{productId}")
data class Product(val productId: Long, val track: String = "default")

// 2. Parse a URI anywhere in the app
fun handleDeepLink(uri: Uri?) {
    val product = linkageParser.parse<Product>(uri)
    if (product != null) {
        showProductScreen(product.productId)
    } else {
        showFallbackScreen()
    }
}
```

## API Reference

### `LinkageParserImpl.parse<T>(uri: Uri?): T?`

- **T** : The type to parse into. Must be a class or sealed class annotated with `@DeepLink`, and
  `reified` so the type is available at runtime.
- **uri** : The `android.net.Uri` to parse. Nullable; if `null` is passed, the function returns
  `null` immediately.
- **Returns** : An instance of `T` populated with values from the URI, or `null` if:
    - The URI doesn't match any pattern,
    - Required parameters are missing,
    - Type conversion fails,
    - No suitable constructor is found.

All other methods are internal and should not be called directly.

## Error Handling & Best Practices

- Always check for `null` after parsing; handle failures gracefully (show a fallback screen,
  redirect, etc.).
- Use sealed classes when you have multiple mutually exclusive deep link destinations — the branch
  selection is done automatically.
- Keep your deep link patterns unambiguous to avoid conflicts between different subclasses.
- For optional parameters, provide sensible default values so the URI remains shorter.

## License

Copyright 2026 Artem Kurbanov.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.