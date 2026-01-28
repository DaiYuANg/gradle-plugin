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
  private val extraArgs: List<String> = emptyList()
) : DockerCommand(logger) {

  override fun buildArgs(): List<String> {
    val args = mutableListOf("docker", "run")
    if (rm) args += "--rm"
    if (detach) args += "-d"
    name?.let { args += listOf("--name", it) }
    volumes.forEach { args += listOf("-v", it) }
    ports.forEach { args += listOf("-p", it) }
    args += extraArgs
    args += image
    return args
  }
}
