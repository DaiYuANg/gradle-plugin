package com.daiyuang.kotlin.gradle.docker.plugin.model

import org.gradle.api.DefaultTask

data class TaskDef(
  val name: String,
  val type: Class<out DefaultTask>,
  val description: String? = null,
  val dependsOn: List<String> = emptyList()
)

