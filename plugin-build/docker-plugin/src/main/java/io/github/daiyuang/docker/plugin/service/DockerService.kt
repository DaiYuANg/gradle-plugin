package io.github.daiyuang.docker.plugin.service

import com.github.dockerjava.api.DockerClient
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

  private fun createClient(): DockerClient {
    val default = DefaultDockerClientConfig.createDefaultConfigBuilder()

    // 判断用户是否提供了任何 host/api/registry 配置
    val empty = listOf(
      parameters.host.orNull,
      parameters.apiVersion.orNull,
      parameters.registryUrl.orNull,
      parameters.registryUsername.orNull,
      parameters.registryPassword.orNull,
      parameters.registryEmail.orNull
    ).all { it == null }

    val finalConfig: DockerClientConfig = if (empty) {
      // 全部未配置 → 使用 docker-java 默认配置
      default.build()
    } else {
      // patch 默认配置
      parameters.host.orNull?.let { default.withDockerHost(it) }
      parameters.apiVersion.orNull?.let { default.withApiVersion(it) }

      // registry 可选
      parameters.registryUrl.orNull?.let { default.withRegistryUrl(it) }
      parameters.registryUsername.orNull?.let { default.withRegistryUsername(it) }
      parameters.registryPassword.orNull?.let { default.withRegistryPassword(it) }
      parameters.registryEmail.orNull?.let { default.withRegistryEmail(it) }

      default.build()
    }

    val httpClient = ApacheDockerHttpClient.Builder()
      .dockerHost(finalConfig.dockerHost)
      .sslConfig(finalConfig.sslConfig)
      .build()

    return DockerClientImpl.getInstance(finalConfig, httpClient)
  }

  override fun close() {
    _client?.close()
  }

  interface Params : BuildServiceParameters {
    val host: Property<String>
    val apiVersion: Property<String>

    // registry 配置（全部可选）
    val registryUrl: Property<String>
    val registryUsername: Property<String>
    val registryPassword: Property<String>
    val registryEmail: Property<String>
  }
}
