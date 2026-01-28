package io.github.daiyuang.docker.plugin.task

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
    DockerInfoCommand(logger).execute()
//    val client = dockerService.get().client()

//    val info = client.infoCmd().exec()
//    println("Docker info$info")
//    val version = client.versionCmd().exec()
//    println("Docker Version: ${version.version}")
  }
}
