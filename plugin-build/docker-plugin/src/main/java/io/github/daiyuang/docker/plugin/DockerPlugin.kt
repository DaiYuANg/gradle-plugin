package io.github.daiyuang.docker.plugin

import io.github.daiyuang.docker.plugin.service.DockerService
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass")
abstract class DockerPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val ext = project.extensions.create(DockerExtension.EXTENSION_NAME, DockerExtension::class.java, project)
    project.gradle.sharedServices.registerIfAbsent(
      DockerService.Companion.SERVICE_NAME,
      DockerService::class.java,
      { spec ->
        run {
          spec.parameters.host.set(ext.dockerHost)
          spec.parameters.apiVersion.set(ext.apiVersion)
        }
      },
    )

    TASK_DEFS.forEach { def ->
      project.tasks.register(def.name, def.type) { task ->
        task.group = DOCKER_TASK_GROUP
      }
    }
  }
}
