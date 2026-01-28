package io.github.daiyuang.docker.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class DockerExtension @Inject constructor(project: Project) {

  companion object{
    const val EXTENSION_NAME = "dockerConfig"
  }

  private val objects = project.objects

}
