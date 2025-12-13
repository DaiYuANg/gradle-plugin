package io.github.daiyuang.docker.plugin.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.AuthConfig
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class DockerService :
  BuildService<DockerService.Params>,
  AutoCloseable {

  companion object {
    const val SERVICE_NAME = "dockerService"
  }

  @Volatile
  private var _client: DockerClient? = null

  fun client(): DockerClient =
    _client ?: synchronized(this) {
      _client ?: createClient().also { _client = it }
    }

  /**
   * 显式暴露 registry auth（给 push / pull 使用）
   */
  fun requireAuthConfig(): AuthConfig {
    val registry = parameters.registryUrl.orNull
      ?: error("dockerConfig.registryUrl must be set")

    val username = parameters.registryUsername.orNull
      ?: error("dockerConfig.registryUsername must be set")

    val password = parameters.registryPassword.orNull
      ?: error("dockerConfig.registryPassword must be set")

    val registryAddress =
      if (registry.startsWith("http")) registry
      else "https://$registry"

    return AuthConfig()
      .withRegistryAddress(registryAddress)
      .withUsername(username)
      .withPassword(password)
  }

  private fun createClient(): DockerClient {
    val builder = DefaultDockerClientConfig.createDefaultConfigBuilder()

    parameters.host.orNull?.let { builder.withDockerHost(it) }
    parameters.apiVersion.orNull?.let { builder.withApiVersion(it) }

    // registry 仅用于 auth config 管理（不会决定 push 目标）
    parameters.registryUrl.orNull?.let { builder.withRegistryUrl(it) }
    parameters.registryUsername.orNull?.let { builder.withRegistryUsername(it) }
    parameters.registryPassword.orNull?.let { builder.withRegistryPassword(it) }
    parameters.registryEmail.orNull?.let { builder.withRegistryEmail(it) }

    val config: DockerClientConfig = builder.build()

    val httpClient = ApacheDockerHttpClient.Builder()
      .dockerHost(config.dockerHost)
      .sslConfig(config.sslConfig)
      .build()

    return DockerClientImpl.getInstance(config, httpClient)
  }

  override fun close() {
    _client?.close()
  }

  interface Params : BuildServiceParameters {
    val host: Property<String>
    val apiVersion: Property<String>

    val registryUrl: Property<String>
    val registryUsername: Property<String>
    val registryPassword: Property<String>
    val registryEmail: Property<String>
  }
}
