package io.github.daiyuang.docker.plugin.command

import io.github.daiyuang.docker.plugin.constant.Platform
import org.gradle.api.logging.Logger
import java.io.BufferedReader
import java.io.InputStreamReader

class DockerBuildCommand(
  logger: Logger,
  private val contextDir: String,
  private val dockerfile: String,
  private val tags: List<String> = emptyList(),
  private val platform: Platform? = null,
  private val buildArgs: Map<String, String> = emptyMap(),
  private val labels: Map<String, String> = emptyMap(),
  private val cacheFrom: List<String> = emptyList(),
  private val noCache: Boolean = false,
  private val pull: Boolean = false,
  private val target: String? = null,
  private val printInspect: Boolean = false,
  private val useBuildx: Boolean = false,
  private val multiPlatforms: List<Platform> = emptyList()
) : DockerCommand(logger) {

  override fun buildArgs(): List<String> {
    val args = mutableListOf<String>()

    if (useBuildx) {
      args += listOf("buildx", "build")
    } else {
      args += "build"
    }

    if (noCache) args += "--no-cache"
    if (pull) args += "--pull"

    // Buildx 多平台逻辑
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

    args += listOf("-f", dockerfile, contextDir)
    return args
  }

  /**
   * 执行 Docker build 并返回输出
   * Task 层可以自行用 StyledTextOutput 渲染
   */
  override fun execute(): List<String> {
    val args = listOf(dockerPath) + buildArgs()
    logger.lifecycle("🔹 Executing: ${args.joinToString(" ")}")

    val output = mutableListOf<String>()
    val process = ProcessBuilder(args)
      .redirectErrorStream(true)
      .start()

    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
      var line: String?
      while (reader.readLine().also { line = it } != null) {
        output += line!!
      }
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
      logger.error("❌ Docker build failed with exit code $exitCode")
      throw RuntimeException("Docker build failed with exit code $exitCode")
    }

    // printInspect 逻辑也返回输出而不打印
    if (printInspect) {
      tags.forEach { tag ->
        val inspectArgs = listOf(dockerPath, "image", "inspect", tag)
        val inspectProcess = ProcessBuilder(inspectArgs)
          .redirectErrorStream(true)
          .start()

        BufferedReader(InputStreamReader(inspectProcess.inputStream)).use { reader ->
          var line: String?
          while (reader.readLine().also { line = it } != null) {
            output += line!!
          }
        }
        val inspectExit = inspectProcess.waitFor()
        if (inspectExit != 0) {
          logger.error("❌ Docker inspect failed for tag $tag with exit code $inspectExit")
          throw RuntimeException("Docker inspect failed for tag $tag")
        }
      }
    }

    return output
  }
}
