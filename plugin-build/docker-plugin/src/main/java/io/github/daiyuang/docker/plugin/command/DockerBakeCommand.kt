package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerBakeCommand(
  logger: Logger,
  private val bakeArgs: List<String>
) : DockerCommand(logger) {
  override fun buildArgs(): List<String> = listOf("docker", "bake") + bakeArgs
}
