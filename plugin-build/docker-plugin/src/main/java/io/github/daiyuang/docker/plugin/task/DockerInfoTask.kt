package io.github.daiyuang.docker.plugin.task

import io.github.daiyuang.docker.plugin.DockerExtension
import io.github.daiyuang.docker.plugin.command.DockerInfoCommand
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.internal.logging.text.StyledTextOutput
import javax.inject.Inject

abstract class DockerInfoTask
@Inject constructor(
  private val outputFactory: StyledTextOutputFactory
) : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerInfo"
  }

  @TaskAction
  fun printInfo() {
    val dockerConfig = project.extensions.getByType(DockerExtension::class.java)

    val command = DockerInfoCommand(logger).also {
      it.dockerPath = dockerConfig.dockerPath.get()
    }

    val output = command.execute()

    val styledOut: StyledTextOutput = outputFactory.create("docker-info")

    output.forEach { line ->
      // 可以根据内容选择不同 style
      when {
        line.startsWith("Server:") || line.startsWith("Containers:") ->
          styledOut.withStyle(StyledTextOutput.Style.Identifier).println("⚡ $line")
        line.startsWith("WARNING") ->
          styledOut.withStyle(StyledTextOutput.Style.Failure).println("⚠️ $line")
        else ->
          styledOut.withStyle(StyledTextOutput.Style.Normal).println("  $line")
      }
    }
  }
}
