package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerBuildCommand(
  logger: Logger,
  private val contextDir: String,
  private val dockerfile: String,
  private val tags: List<String> = emptyList(),
  private val platform: String? = null,
  private val buildArgs: Map<String, String> = emptyMap(),
  private val labels: Map<String, String> = emptyMap(),
  private val cacheFrom: List<String> = emptyList(),
  private val noCache: Boolean = false,
  private val pull: Boolean = false,
  private val target: String? = null,
  private val printInspect: Boolean = false
) : DockerCommand(logger) {

  override fun buildArgs(): List<String> {
    val args = mutableListOf("docker", "build")

    if (noCache) args += "--no-cache"
    if (pull) args += "--pull"
    platform?.let { args += listOf("--platform", it) }
    target?.let { args += listOf("--target", it) }

    buildArgs.forEach { (k, v) -> args += listOf("--build-arg", "$k=$v") }
    labels.forEach { (k, v) -> args += listOf("--label", "$k=$v") }
    cacheFrom.forEach { args += listOf("--cache-from", it) }
    tags.forEach { args += listOf("-t", it) }

    args += listOf("-f", dockerfile, contextDir)

    return args
  }

  override fun execute() {
    val args = buildArgs()
    logger.lifecycle("Executing: ${args.joinToString(" ")}")

    val process = ProcessBuilder(args)
      .redirectErrorStream(true)
      .start()

    process.inputStream.bufferedReader().useLines { lines ->
      lines.forEach { logger.lifecycle(it) }
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
      throw RuntimeException("Docker build failed with exit code $exitCode")
    }

    if (printInspect) {
      tags.forEach { tag ->
        val inspect = ProcessBuilder("docker", "image", "inspect", tag)
          .redirectErrorStream(true)
          .start()
        inspect.inputStream.bufferedReader().useLines { lines ->
          lines.forEach { logger.lifecycle(it) }
        }
        inspect.waitFor()
      }
    }
  }
}
