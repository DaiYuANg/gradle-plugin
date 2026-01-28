package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerInfoCommand(logger: Logger) : DockerCommand(logger) {
  override fun buildArgs(): List<String> = listOf("docker", "info")
}
