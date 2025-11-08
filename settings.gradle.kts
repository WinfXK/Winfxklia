pluginManagement {
    repositories {
        maven {
            name = "AliyunGoogle"
            setUrl("https://maven.aliyun.com/repository/google")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
            name = "AliyunMavenCentral"
            setUrl("https://maven.aliyun.com/repository/central")
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            name = "AliyunGoogle"
            setUrl("https://maven.aliyun.com/repository/google")
        }
        maven {
            name = "AliyunMavenCentral"
            setUrl("https://maven.aliyun.com/repository/central")
        }
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "Winfxklia"
include(":app")
include(":mylibrary")
