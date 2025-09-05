plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":miuix"))
                implementation("io.github.classgraph:classgraph:4.8.174")
            }
        }
    }
}

val outputDir = layout.projectDirectory.dir("../../build/icon-svgs").asFile

tasks.register<JavaExec>("generateIconSvgs") {
    group = "documentation"
    description = "Scan ImageVector in top.yukonga.miuix.kmp.icon.icons and export SVGs"
    val mainClassName = "top.yukonga.miuix.tools.iconsvg.MainKt"

    dependsOn(tasks.named("jvmJar"))

    // classpath = compiled jar + runtime deps
    val jvm = kotlin.targets.getByName("jvm")
    val mainCompilation = jvm.compilations.getByName("main")
    classpath(
        files(mainCompilation.output.allOutputs.files),
        mainCompilation.runtimeDependencyFiles
    )
    mainClass.set(mainClassName)

    // default args
    args = listOf(
        "--package", providers.environmentVariable("ICON_PACKAGE").orElse("top.yukonga.miuix.kmp.icon.icons").get(),
        "--out", outputDir.absolutePath
    )
}
