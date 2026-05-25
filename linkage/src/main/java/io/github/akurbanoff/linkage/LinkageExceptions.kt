package io.github.akurbanoff.linkage

class AnnotationException(
    kClassName: String,
    message: String = "The $kClassName is not annotated. Please check that you have specified the @LinkageDeepLink annotation.",
) : Exception(message)

class MatchUrlException(
    url: String,
    message: String = "Unable to match parameters to $url. Please check that the URL in the annotation is correct.",
) : Exception(message)

class ConstructorParamsException(
    kClassName: String,
    message: String = "Unable to find the required parameters to create $kClassName. Please ensure you have created the deep link correctly.",
) : Exception(message)