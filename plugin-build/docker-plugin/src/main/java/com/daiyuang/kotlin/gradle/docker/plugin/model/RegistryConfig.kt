package com.daiyuang.kotlin.gradle.docker.plugin.model

import org.gradle.api.Named
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * 单个 registry 配置
 */
abstract class RegistryConfig @Inject constructor(name: String) : Named {
  abstract val registryUrl: Property<String>
  abstract val username: Property<String>
  abstract val password: Property<String>
  abstract val email: Property<String>

  override fun getName(): String = name
}
