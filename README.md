# Linkage — Deep Link Parsing Library for Kotlin (Android)

**Linkage** is a Kotlin library for Android that simplifies deep link handling. It automatically
parses URIs into Kotlin data classes or sealed class hierarchies, eliminating the need for manual
string manipulation and routing logic.

## ✨ Features

* **Annotation-Driven**: Define deep link patterns directly in your Kotlin classes using
  `@LinkageDeepLink`.
* **Type-Safe Parsing**: The library handles URI parameter extraction, type conversion (`String`,
  `Int`, `Long`, `Float`, `Double`, `Boolean`), and object construction.
* **Sealed Class Support**: Model multiple, mutually exclusive deep link destinations in a type-safe
  way. The library automatically selects the correct subclass based on the URI.
* **Bidirectional Conversion**: Not only can you parse URIs into objects, but you can also generate
  URIs from your data objects using `LinkageUriConverter`.
* **Flexible Pattern Rules**: Supports path parameters with a simple `{param}` syntax and optional
  query parameters.
* **Error Handling**: Gracefully returns `null` if parsing fails and provides optional error
  callbacks for debugging.

## 📦 Installation

Add the library to your module's `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.akurbanoff:linkage:<version>")
}
```

## 🚀 Getting Started

### 1. Define a Deep Link Pattern

Annotate a class or sealed class with `@LinkageDeepLink`. Use curly braces `{param}` to declare
placeholders that will be extracted from the URI. The placeholder names must exactly match the
constructor parameter names.

```kotlin
@LinkageDeepLink("app://person/{id}/{name}")
data class Person(
    val id: Int,
    val name: String
)
```

### 2. Parse a URI

Call the `parse` function provided by `LinkageParserImpl`.

```kotlin
val linkageParser = provideLinkageParser()

val uri = Uri.parse("app://person/42/alex")
val person = linkageParser.parse<Person>(uri.toString())

if (person != null) {
  println("Person ID: ${person.id}, Name: ${person.name}")
}
```

### 3. Using Sealed Classes for Multiple Patterns

For multiple deep link formats, use a sealed class. Annotate each subclass with its own
`@LinkageDeepLink` pattern.

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
val screen = linkageParser.parse<Screen>("app://profile/abc123")
when (screen) {
    is Screen.Profile -> println("User: ${screen.userId}")
    is Screen.Settings -> println("Section: ${screen.section}")
    Screen.Home -> println("Home screen")
    null -> println("Unknown deep link")
}
```

### 4. Optional Constructor Parameters

Constructor parameters with default values are **optional**. If the placeholder is missing from the
URI, the default value is used. If a required parameter (without a default) is absent, parsing
returns `null`.

```kotlin
@LinkageDeepLink("app://note/{id}?source={source}")
data class Notes(
  val id: Int,
  val source: String = "default" // 'source' is optional in the URI
)
```

## 🔄 Generating URIs from Objects

You can reverse the process and create a URI from an instance of your data class.

```kotlin
val uriConverter = provideLinkageUriConverter()
val person = Person(42, "alex")
val uri = uriConverter.toUri(person)

println(uri.toString()) // Output: app://person/42/alex
```

## 📜 Pattern Format Rules

* Patterns must begin with a scheme (e.g., `app://`).
* Placeholders are written as `{name}` and match any non‑empty sequence of characters except `/`,
  `?`, `#`.
* Query parameters and fragments are ignored during matching.
* The same placeholder name can appear only once per pattern.
* Placeholder names must exactly match constructor parameter names.

## 🛠️ Advanced Usage

### Custom Parameter Matching

Use the `mayContainLinkParams` array to specify which parameters should use greedy matching,
allowing them to include special characters like `/`, `?`, and `#`.

```kotlin
@LinkageDeepLink(
  url = "app://webview/{url}",
  mayContainLinkParams = ["url"]
)
data class WebViewLink(val url: String)
```

### Error Handling Callback

The `parse` function accepts an optional `doOnError` callback for debugging purposes.

```kotlin
val person = linkageParser.parse<Person>(uri.toString()) { error, constructorName ->
  Log.e("Linkage", "Error parsing $constructorName: ${error.message}")
}
```

## 📚 API Reference

### `linkageParser.parse<T>(uriString: String?): T?`

* **T**: The type to parse into. Must be a class or sealed class annotated with `@LinkageDeepLink`.
* **uriString**: The URI string to parse. If `null` is passed, the function returns `null`
  immediately.
* **Returns**: An instance of `T` populated with values from the URI, or `null` if parsing fails.

### `linkageUriConverter.toUri(link: Any?): Uri?`

* **link**: An instance of a class annotated with `@LinkageDeepLink`.
* **Returns**: A `Uri` built from the object, or `null` if conversion fails.

## 🧠 How It Works Under the Hood

When you call `parse<T>`:

1. The library retrieves the `@LinkageDeepLink` annotation from the target class.
2. It converts the pattern URL into a regular expression to match the incoming URI.
3. For each placeholder in the pattern, it extracts the corresponding value from the URI.
4. It then uses Kotlin reflection to call the primary constructor, passing in the extracted values.
5. The library handles basic type conversions automatically for `String`, `Int`, `Long`, `Float`,
   `Double`, and `Boolean`.

## 📄 License

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