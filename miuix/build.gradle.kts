// Copyright 2025, miuix-kotlin-multiplatform contributors
// SPDX-License-Identifier: Apache-2.0

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.spotless)
    id("module.publication")
}

android {
    namespace = "top.yukonga.miuix.kmp"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

kotlin {
    withSourcesJar(true)

    jvmToolchain(21)

    androidTarget {
        publishLibraryVariants("release")
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
    ).forEach {
        it.compilerOptions {
            freeCompilerArgs.add("-Xbinary=preCodegenInlineThreshold=40")
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser { binaries.executable() }
    }
    js(IR) {
        browser { binaries.executable() }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity) // Android BackHandler
            implementation(libs.androidx.window) // Android WindowMetrics
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.compose.window.size)
        }
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        licenseHeaderFile(rootProject.file("./spotless/copyright.txt"), "(^(?![\\/ ]\\**).*$)")
    }

    kotlinGradle {
        target("*.kts")
        licenseHeaderFile(rootProject.file("./spotless/copyright.txt"), "(^(?![\\/ ]\\**).*$)")
    }
}

tasks.matching { it.name.startsWith("dokka") }.configureEach {
    // 生成 SVG（输出到 root/build/icon-svgs）
    dependsOn(tasks.getByPath(":docs:icon-gen:generateIconSvgs"))
}

val copyIconSvgsToDokka by tasks.register<Copy>("copyIconSvgsToDokka") {
    val fromDir = rootProject.layout.buildDirectory.dir("icon-svgs")
    val toDir = layout.buildDirectory.dir("dokka/html/icons")
    from(fromDir)
    into(toDir)
}

tasks.named("dokkaGenerate").configure {
    finalizedBy(copyIconSvgsToDokka)
}
