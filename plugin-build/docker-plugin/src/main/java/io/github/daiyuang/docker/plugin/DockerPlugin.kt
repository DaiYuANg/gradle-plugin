package io.github.daiyuang.docker.plugin

import io.github.daiyuang.docker.plugin.service.DockerService
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass", "unused")
abstract class DockerPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val ext = project.extensions.create(DockerExtension.EXTENSION_NAME, DockerExtension::class.java, project)
    project.gradle.sharedServices.registerIfAbsent(
      DockerService.SERVICE_NAME,
      DockerService::class.java,
      { spec ->
        run {
          spec.parameters.host.set(ext.dockerHost)
          spec.parameters.apiVersion.set(ext.apiVersion)
          spec.parameters.registryUrl.set(ext.registryUrl.orNull)
          spec.parameters.registryUsername.set(ext.registryUsername)
          spec.parameters.registryPassword.set(ext.registryPassword)
          spec.parameters.registryEmail.set(ext.registryEmail)
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
