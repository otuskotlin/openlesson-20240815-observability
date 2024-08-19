pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.0"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
        id("org.openapi.generator") version "7.7.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

include("ok-marketplace-api-log1")
