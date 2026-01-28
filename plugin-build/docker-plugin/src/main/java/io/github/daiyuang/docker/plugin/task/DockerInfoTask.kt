package io.github.daiyuang.docker.plugin.task

import io.github.daiyuang.docker.plugin.DockerExtension
import io.github.daiyuang.docker.plugin.command.DockerCommand
import io.github.daiyuang.docker.plugin.command.DockerInfoCommand
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class DockerInfoTask : DefaultTask() {
  companion object {
    const val TASK_NAME = "dockerInfo"
  }

  @TaskAction
  fun printInfo() {
    val dockerConfig = project.extensions.getByType(DockerExtension::class.java)
    DockerInfoCommand(logger)
      .also {
        it.dockerPath = dockerConfig.dockerPath.get()
      }
      .execute()
  }
}
