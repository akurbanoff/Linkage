import org.jreleaser.model.Active

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("signing")
    id("org.jreleaser")
}

version = "0.0.1"
group = "io.github.akurbanoff"

android {
    namespace = "io.github.akurbanoff.linkage"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation(libs.kotlin.reflect)
    testImplementation(libs.junit)
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                setUrl(layout.buildDirectory.dir("staging-deploy"))
            }
        }
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "io.github.akurbanoff"
                artifactId = "linkage"
                version = "0.0.1"

                pom {
                    name.set("Deep Link Parsing Library for Kotlin (Android)")
                    description.set("Linkage simplifies deep link handling in Android apps by automatically parsing URIs into Kotlin data classes or sealed class hierarchies. You define URL patterns with placeholders using the `@LinkageDeepLink` annotation, and the library does the rest — extracting parameters, converting types, and constructing objects.")
                    url.set("https://github.com/akurbanoff/Linkage.git")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            name = "Artem Kurbanov"
                            email = "kurbanoff.ae@gmail.com"
                        }
                    }

                    scm {
                        connection.set("scm:git.github.com/akurbanoff/Linkage.git")
                        developerConnection.set("scm:git:ssh://github.com/akurbanoff/Linkage.git")
                        url.set("https://github.com/akurbanoff/Linkage.git")
                    }
                }
            }
        }
    }

    signing {
        val signingKey = findProperty("signing.key") as? String
        val signingPassword = findProperty("signing.password") as? String

        if (signingKey != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["release"])
        }
    }
}

jreleaser {
    release {
        github {
            skipRelease = true
            skipTag = true
            gitRootSearch = true
        }
    }

    signing {
        active = Active.ALWAYS
        armored = true
        verify = true
    }

    project {
        inceptionYear = "2026"
        author("Artem Kurbanov")
    }

    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                setAuthorization("Basic")
                applyMavenCentralRules = false
                sign = true
                checksums = true
                sourceJar = true
                javadocJar = true
                retryDelay = 60
            }
        }
    }
}