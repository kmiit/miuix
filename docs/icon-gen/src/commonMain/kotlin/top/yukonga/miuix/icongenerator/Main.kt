package top.yukonga.miuix.tools.svggen

import io.github.classgraph.ClassGraph
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File
import kotlin.system.exitProcess
import java.lang.reflect.Modifier

fun main(args: Array<String>) {
    var pkg = "top.yukonga.miuix.kmp.icon.icons"
    var outDir = File("../../build/icon-svgs") // projectRoot/build/icon-svgs (from docs/svg-gen)

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--package" -> { pkg = args.getOrNull(i + 1) ?: pkg; i++ }
            "--out" -> { outDir = File(args.getOrNull(i + 1) ?: outDir.path); i++ }
        }
        i++
    }
    outDir.mkdirs()
    println("Generating SVGs from package: $pkg -> ${outDir.absolutePath}")

    val found = mutableMapOf<String, ImageVector>()
    ClassGraph()
        .acceptPackages(pkg)
        .enableClassInfo()
        .enableFieldInfo()
        .enableMethodInfo()
        .scan().use { scanResult ->
            for (clsInfo in scanResult.allClasses) {
                val cls = clsInfo.loadClass()

                // 1) 静态字段（可能是 object 内声明的 @JvmField）
                cls.declaredFields
                    .filter { Modifier.isStatic(it.modifiers) && it.type.name == ImageVector::class.java.name }
                    .forEach { f ->
                        try {
                            f.isAccessible = true
                            val value = f.get(null) as? ImageVector ?: return@forEach
                            found.putIfAbsent(f.name, value)
                        } catch (e: Throwable) {
                            System.err.println("Skip field ${cls.name}.${f.name}: ${e.message}")
                        }
                    }

                // 2) 静态 getter（Kotlin 顶层 val/属性 getter -> getXxx()）
                cls.declaredMethods
                    .filter {
                        Modifier.isStatic(it.modifiers)
                            && it.parameterCount == 0
                            && it.returnType.name == ImageVector::class.java.name
                    }
                    .forEach { m ->
                        val name = m.name.removePrefix("get").removePrefix("_").removeSuffix("$annotations")
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        try {
                            m.isAccessible = true
                            val value = m.invoke(null) as? ImageVector ?: return@forEach
                            // 若名称为空则用类名+方法名
                            val key = if (name.isNotBlank()) name else "${cls.simpleName}_${m.name}"
                            found.putIfAbsent(key, value)
                        } catch (e: Throwable) {
                            System.err.println("Skip method ${cls.name}.${m.name}: ${e.message}")
                        }
                    }
            }
        }

    if (found.isEmpty()) {
        System.err.println("No ImageVector found under $pkg")
        exitProcess(0)
    }

    var ok = 0
    var fail = 0
    found.toSortedMap().forEach { (name, vec) ->
        try {
            val file = File(outDir, "${sanitize(name)}.svg")
            SvgWriter.write(vec, file, name = name)
            ok++
        } catch (e: Throwable) {
            System.err.println("Failed ${name}: ${e.message}")
            fail++
        }
    }
    println("Done. Success: $ok, Failed: $fail, Output: ${outDir.absolutePath}")
}

private fun sanitize(name: String): String =
    name.replace(Regex("[^A-Za-z0-9._-]"), "_")