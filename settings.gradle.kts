// Copyright 2025, miuix-kotlin-multiplatform contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "miuix"

include(":miuix")
include(":example")
include(":docs:demo")
include(":docs:icon-gen")
