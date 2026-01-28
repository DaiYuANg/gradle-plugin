package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerTagCommand(
  logger: Logger,
  private val source: String,
  private val target: String
) : DockerCommand(logger) {
  override fun buildArgs(): List<String> = listOf("docker", "tag", source, target)
}
