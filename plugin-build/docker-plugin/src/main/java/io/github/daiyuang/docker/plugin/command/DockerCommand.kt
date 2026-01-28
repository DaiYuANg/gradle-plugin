package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger
import java.io.BufferedReader
import java.io.InputStreamReader

abstract class DockerCommand(protected val logger: Logger) {

  /** Docker 可执行命令路径 */
  var dockerPath: String = "docker"

  abstract fun buildArgs(): List<String>

  open fun execute(): List<String> {
    val args = listOf(dockerPath) + buildArgs().drop(1)

    // 统一打印执行命令
    logger.lifecycle("🔹 Executing command: ${args.joinToString(" ")}")

    val process = ProcessBuilder(args)
      .redirectErrorStream(true)
      .start()

    // 逐行读取输出
    val output = mutableListOf<String>()
    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
      var line: String?
      while (reader.readLine().also { line = it } != null) {
        output += line!!
      }
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
      throw RuntimeException("Docker command failed with exit code $exitCode")
    }

    return output
  }
}
