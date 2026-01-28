package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

abstract class DockerCommand(protected val logger: Logger) {

  /** Docker 可执行命令路径 */
  var dockerPath: String = "docker"

  abstract fun buildArgs(): List<String>

  open fun execute() {
    val args = listOf(dockerPath) + buildArgs().drop(1)
    logger.lifecycle("Executing: ${args.joinToString(" ")}")

    val process = ProcessBuilder(args)
      .redirectErrorStream(true)
      .start()

    process.inputStream.bufferedReader().useLines { lines ->
      lines.forEach { logger.lifecycle(it) }
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
      throw RuntimeException("Docker command failed with exit code $exitCode")
    }
  }
}
