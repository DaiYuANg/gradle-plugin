package io.github.daiyuang.docker.plugin.command

import org.gradle.api.logging.Logger

class DockerPushCommand(logger: Logger,
                        private val registries: List<String>,
                        private val imageName: String,
                        private val tags: List<String>) : DockerCommand(logger) {
  override fun buildArgs(): List<String> = emptyList()

  override fun execute(): List<String> {
    val output = mutableListOf<String>()

    imageRefs().forEach { ref ->
      val args = listOf(dockerPath, "push", ref)
      logger.lifecycle("🔹 Executing: ${args.joinToString(" ")}")

      val process = ProcessBuilder(args)
        .redirectErrorStream(true)
        .start()

      process.inputStream.bufferedReader().useLines { lines ->
        lines.forEach { output += it }
      }

      val exitCode = process.waitFor()
      if (exitCode != 0) {
        throw RuntimeException("Docker push failed for $ref (exit code $exitCode)")
      }
    }

    return output
  }

  private fun imageRefs(): List<String> {
    require(imageName.isNotBlank()) {
      "imageName must not be blank"
    }
    require(!imageName.contains(":")) {
      "imageName must not contain ':'"
    }

    val rs = if (registries.isEmpty()) listOf("docker.io") else registries
    val ts = if (tags.isEmpty()) listOf("latest") else tags

    return rs.flatMap { registry ->
      val prefix = if (registry.isBlank()) "" else "$registry/"
      ts.map { tag ->
        "$prefix$imageName:$tag"
      }
    }
  }
}
