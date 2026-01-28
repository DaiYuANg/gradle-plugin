package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerPushCommand(logger: Logger, private val image: String) : DockerCommand(logger) {
  override fun buildArgs(): List<String> = listOf("docker", "push", image)
}
