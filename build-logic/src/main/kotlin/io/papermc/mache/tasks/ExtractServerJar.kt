package io.papermc.mache.tasks

import io.papermc.mache.util.convertToPath
import io.papermc.mache.util.ensureClean
import io.papermc.mache.util.useZip
import io.papermc.mache.util.whitespace
import kotlin.io.path.copyTo
import kotlin.io.path.notExists
import kotlin.io.path.readLines
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ExtractServerJar : DefaultTask() {

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val downloadedJar: RegularFileProperty

    @get:OutputFile
    abstract val inputJar: RegularFileProperty

    @TaskAction
    fun run() {
        val jar = downloadedJar.convertToPath()
        val out = inputJar.convertToPath().ensureClean()

        jar.useZip { root ->
            val metaInf = root.resolve("META-INF")
            val versionsList = metaInf.resolve("versions.list")
            if (versionsList.notExists()) {
                throw Exception("Could not find versions.list")
            }

            val lines = versionsList.readLines()
            if (lines.size != 1) {
                throw Exception("versions.list is invalid")
            }

            val line = lines.first()
            val parts = line.split(whitespace)
            if (parts.size != 3) {
                throw Exception("versions.list line is invalid")
            }

            val inputJarInJar = metaInf.resolve("versions").resolve(parts[2])
            if (inputJarInJar.notExists()) {
                throw Exception("Could not find version jar")
            }

            inputJarInJar.copyTo(out)
        }
    }
}
