// Copyright 2025, miuix-kotlin-multiplatform contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    kotlin("jvm")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

// No executable entry point needed; this module only provides a custom Gradle task.

dependencies {
    implementation(project(":miuix"))
}

val iconsSourceDir = project.rootDir.resolve("miuix/src/commonMain/kotlin/top/yukonga/miuix/kmp/icon")
val outputDir = layout.buildDirectory.dir("generated-svg")

// Simple conversion of Compose ImageVector.kt definitions into minimal SVG path data
// This is heuristic: it parses the path{ ... } blocks and extracts moveTo/curveTo/lineTo commands.

abstract class GenerateSvgTask : DefaultTask() {
    @get:InputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:OutputDirectory
    abstract val destinationDir: DirectoryProperty

    @TaskAction
    fun run() {
        val src = sourceDir.asFile.get()
        val dest = destinationDir.asFile.get()
        project.logger.lifecycle("Generating SVG icons from: ${'$'}src -> ${'$'}dest")
        if (!dest.exists()) dest.mkdirs()

    val ktFiles = src.walkTopDown().filter { it.isFile && it.extension == "kt" && it.name != "MiuixIcon.kt" }
        ktFiles.forEach { file ->
            val content = file.readText()
            val builderMatch = Regex("ImageVector.Builder\\(\\\"([^\\\"]+)\\\", *([0-9.]+)\\.dp, *([0-9.]+)\\.dp, *([0-9.]+)f, *([0-9.]+)f\\)").find(content)
            val iconName = builderMatch?.groupValues?.getOrNull(1) ?: file.nameWithoutExtension
            val viewportWidth = builderMatch?.groupValues?.getOrNull(4)?.toFloatOrNull() ?: 24f
            val viewportHeight = builderMatch?.groupValues?.getOrNull(5)?.toFloatOrNull() ?: 24f

            val pathBlocks = Regex("path\\(.*?\\) *\\{(.*?)\\}", setOf(RegexOption.DOT_MATCHES_ALL)).findAll(content)
            val paths = mutableListOf<String>()
            pathBlocks.forEach { m ->
                val body = m.groupValues[1]
                val sb = StringBuilder()
                val moveRegex = Regex("moveTo\\((.*?)f, (.*?)f\\)")
                val lineToRegex = Regex("lineTo\\((.*?)f, (.*?)f\\)")
                val curveToRegex = Regex("curveTo\\((.*?)f, (.*?)f, (.*?)f, (.*?)f, (.*?)f, (.*?)f\\)")
                val closeRegex = Regex("close\\(\\)")
                body.lineSequence().forEach { line ->
                    moveRegex.findAll(line).forEach { r ->
                        sb.append("M${r.groupValues[1]} ${r.groupValues[2]} ")
                    }
                    lineToRegex.findAll(line).forEach { r ->
                        sb.append("L${r.groupValues[1]} ${r.groupValues[2]} ")
                    }
                    curveToRegex.findAll(line).forEach { r ->
                        sb.append("C${r.groupValues[1]} ${r.groupValues[2]} ${r.groupValues[3]} ${r.groupValues[4]} ${r.groupValues[5]} ${r.groupValues[6]} ")
                    }
                    if (closeRegex.containsMatchIn(line)) sb.append("Z ")
                }
                if (sb.isNotBlank()) paths += sb.toString().trim()
            }
            if (paths.isEmpty()) return@forEach
            val svg = buildString {
                appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                appendLine("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$viewportWidth\" height=\"$viewportHeight\" viewBox=\"0 0 $viewportWidth $viewportHeight\" fill=\"none\" stroke=\"none\">")
                paths.forEach { p ->
                    appendLine("  <path d=\"$p\" fill=\"currentColor\" />")
                }
                appendLine("</svg>")
            }
            val relativeDir = file.relativeTo(src).parentFile
            val outDir = File(dest, relativeDir.path)
            outDir.mkdirs()
            File(outDir, iconName + ".svg").writeText(svg)
        }
    }
}

val generateSvg by tasks.registering(GenerateSvgTask::class) {
    sourceDir.set(iconsSourceDir)
    destinationDir.set(outputDir)
}

// Make it easy to call from root: ./gradlew :docs:iconGen:generateSvg

