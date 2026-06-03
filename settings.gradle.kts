@file:Suppress("UnstableApiUsage")

include(":mylibrary")


pluginManagement {
    repositories {
        maven {
            name = "LocalCacheGoogle"
            setUrl("http://192.168.0.90:50004/maven-google")
            isAllowInsecureProtocol = true
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
            name = "LocalCacheMavenCentral"
            setUrl("http://192.168.0.90:50004/maven")
            isAllowInsecureProtocol = true
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            name = "LocalCacheGoogle"
            setUrl("http://192.168.0.90:50004/maven-google")
            isAllowInsecureProtocol = true
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
            name = "LocalCacheMavenCentral"
            setUrl("http://192.168.0.90:50004/maven")
            isAllowInsecureProtocol = true
        }
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "Winfxklia"
include(":app")
 