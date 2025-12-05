package com.daiyuang.kotlin.gradle.docker.plugin

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class DockerExtension @Inject constructor(project: Project) {

  companion object{
    const val EXTENSION_NAME = "dockerConfig"
  }

  private val objects = project.objects

  val dockerHost: Property<String> = objects.property(String::class.java)
  val apiVersion: Property<String> = objects.property(String::class.java)

  val dockerTlsVerify: Property<Boolean> = objects.property(Boolean::class.java)
  val dockerCertPath: Property<String> = objects.property(String::class.java)

  val registryUrl: Property<String> = objects.property(String::class.java)
  val registryUsername: Property<String> = objects.property(String::class.java)
  val registryPassword: Property<String> = objects.property(String::class.java)
  val registryEmail: Property<String> = objects.property(String::class.java)
}
