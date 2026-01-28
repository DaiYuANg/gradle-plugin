package io.github.daiyuang.docker.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class DockerExtension @Inject constructor(project: Project) {

  companion object{
    const val EXTENSION_NAME = "dockerConfig"
    const val PROPERTY_KEY = "docker.binary.path"
  }

  private val objects = project.objects

  /**
   * Docker 可执行文件路径
   * 优先级：
   * 1. project property (gradle.properties 或 -P)
   * 2. DSL 配置
   * 3. 默认 "docker"
   */
  val dockerPath: Property<String> = objects.property(String::class.java).convention(
    project.findProperty(PROPERTY_KEY)?.toString() ?: "docker"
  )
}
