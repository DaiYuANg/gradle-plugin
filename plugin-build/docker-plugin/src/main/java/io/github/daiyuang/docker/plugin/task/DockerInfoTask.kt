package io.github.daiyuang.docker.plugin.task

import io.github.daiyuang.docker.plugin.service.DockerService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.TaskAction

abstract class DockerInfoTask : DefaultTask() {
  companion object {
    const val TASK_NAME = "dockerInfo"
  }

  @get:ServiceReference(DockerService.Companion.SERVICE_NAME)
  abstract val dockerService: Property<DockerService>

  @TaskAction
  fun printInfo() {
    val client = dockerService.get().client()

    val info = client.infoCmd().exec()
    println("Docker info$info")
    val version = client.versionCmd().exec()
    println("Docker Version: ${version.version}")
  }
}
