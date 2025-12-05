package com.daiyuang.kotlin.gradle.docker.plugin.func

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.dockerjava.api.command.InspectImageResponse


fun InspectImageResponse.toPrettyJson(): String {
  val mapper = ObjectMapper()
  // 启用美化输出
  mapper.enable(SerializationFeature.INDENT_OUTPUT)
  return mapper.writeValueAsString(this)
}
