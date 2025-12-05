package com.daiyuang.kotlin.gradle.docker.plugin.task

import com.daiyuang.kotlin.gradle.docker.plugin.service.DockerService
import com.daiyuang.kotlin.gradle.docker.plugin.service.DockerService.Companion.SERVICE_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.TaskAction

abstract class DockerInfoTask : DefaultTask() {
  companion object {
    const val TASK_NAME = "dockerInfo"
  }

  @get:ServiceReference(SERVICE_NAME)
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
