pluginManagement {
    repositories {
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BudgetTrackerFull"
include(":app")
include(":core:data")
include(":core:design")
include(":core:remote")
include(":core:navigation")
include(":core:uicomposers")
include(":core:uisystem")
include(":feature:dashboard")
include(":feature:transactions")
include(":feature:transactionsform")
