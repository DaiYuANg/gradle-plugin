package io.github.daiyuang.docker.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass", "unused")
abstract class DockerPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val ext = project.extensions.create(DockerExtension.EXTENSION_NAME, DockerExtension::class.java, project)

    TASK_DEFS.forEach { def ->
      project.tasks.register(def.name, def.type) { task ->
        task.group = DOCKER_TASK_GROUP
      }
    }
  }
}
