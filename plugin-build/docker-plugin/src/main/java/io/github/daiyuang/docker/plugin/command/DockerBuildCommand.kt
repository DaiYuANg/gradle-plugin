package io.github.daiyuang.docker.plugin.command

import io.github.daiyuang.docker.plugin.constant.Platform
import org.gradle.api.logging.Logger

class DockerBuildCommand(
  logger: Logger,
  private val contextDir: String,
  private val dockerfile: String,
  private val tags: List<String> = emptyList(),
  private val platform: Platform? = null, // 单平台
  private val buildArgs: Map<String, String> = emptyMap(),
  private val labels: Map<String, String> = emptyMap(),
  private val cacheFrom: List<String> = emptyList(),
  private val noCache: Boolean = false,
  private val pull: Boolean = false,
  private val target: String? = null,
  private val printInspect: Boolean = false,
  private val useBuildx: Boolean = false,           // 新增: 是否启用 buildx
  private val multiPlatforms: List<Platform> = emptyList() // 多平台构建
) : DockerCommand(logger) {

  override fun buildArgs(): List<String> {
    val args = mutableListOf<String>()

    if (useBuildx) {
      args += "docker"
      args += "buildx"
      args += "build"
    } else {
      args += "docker"
      args += "build"
    }

    if (noCache) args += "--no-cache"
    if (pull) args += "--pull"

    // Buildx 多平台
    when {
      multiPlatforms.isNotEmpty() -> {
        args += "--platform"
        args += multiPlatforms.joinToString(",") { it.value }
      }
      platform != null -> {
        args += "--platform"
        args += platform.value
      }
    }

    target?.let { args += listOf("--target", it) }

    buildArgs.forEach { (k, v) -> args += listOf("--build-arg", "$k=$v") }
    labels.forEach { (k, v) -> args += listOf("--label", "$k=$v") }
    cacheFrom.forEach { args += listOf("--cache-from", it) }
    tags.forEach { args += listOf("-t", it) }

    // Buildx 如果指定 --push 或 --load，则上下文也要加
    args += listOf("-f", dockerfile, contextDir)

    // buildx 默认不 push/load，可在 Task 里设置
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
