package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerRunCommand(
  logger: Logger,
  private val image: String,
  private val name: String? = null,
  private val rm: Boolean = false,
  private val detach: Boolean = false,
  private val volumes: List<String> = emptyList(),
  private val ports: List<String> = emptyList(),
  private val extraArgs: List<String> = emptyList(),
  private val env: Map<String, String> = emptyMap() // 新增
) : DockerCommand(logger) {

  override fun buildArgs(): List<String> {
    val args = mutableListOf("docker", "run")

    if (rm) args += "--rm"
    if (detach) args += "-d"
    name?.let { args += listOf("--name", it) }

    // 添加环境变量
    env.forEach { (key, value) ->
      args += listOf("-e", "$key=$value")
    }

    volumes.forEach { args += listOf("-v", it) }
    ports.forEach { args += listOf("-p", it) }
    args += extraArgs
    args += image

    return args
  }
}
