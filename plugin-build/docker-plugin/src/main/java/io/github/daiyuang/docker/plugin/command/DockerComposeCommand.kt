package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerComposeCommand(
  logger: Logger,
  private val composeArgs: List<String>
) : DockerCommand(logger) {
  override fun buildArgs(): List<String> = listOf("docker", "compose") + composeArgs
}
