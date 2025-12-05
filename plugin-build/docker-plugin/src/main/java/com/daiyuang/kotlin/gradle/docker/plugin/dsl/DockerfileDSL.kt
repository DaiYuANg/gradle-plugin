package com.daiyuang.kotlin.gradle.docker.plugin.dsl

@DslMarker
annotation class DockerfileDsl

@DockerfileDsl
class DockerfileBuilder {

  private val instructions = mutableListOf<String>()

  // ----------------- 基本指令 -----------------
  fun from(image: String, alias: String? = null) {
    instructions += if (alias != null) "FROM $image AS $alias" else "FROM $image"
  }

  fun arg(name: String, default: String? = null) {
    instructions += if (default != null) "ARG $name=$default" else "ARG $name"
  }

  fun env(name: String, value: String) {
    instructions += "ENV $name=\"$value\""
  }

  fun workdir(path: String) {
    instructions += "WORKDIR $path"
  }

  fun copy(src: String, dest: String, from: String? = null) {
    instructions += if (from != null) "COPY --from=$from $src $dest" else "COPY $src $dest"
  }

  fun run(vararg commands: String) {
    val cmdLine = commands.joinToString(" && ") { it }
    instructions += "RUN $cmdLine"
  }

  fun entrypoint(vararg command: String) {
    val cmd = command.joinToString(", ") { "\"$it\"" }
    instructions += "ENTRYPOINT [$cmd]"
  }

  fun label(vararg labels: Pair<String, String>) {
    labels.forEach { (k, v) ->
      instructions += """LABEL $k="$v""""
    }
  }

  fun raw(line: String) {
    instructions += line
  }

  fun build(templateVars: Map<String, String> = emptyMap()): String {
    var content = instructions.joinToString("\n")
    templateVars.forEach { (k, v) ->
      content = content.replace("\${$k}", v)
    }
    return content
  }
}

// DSL 函数
fun dockerfile(block: DockerfileBuilder.() -> Unit): DockerfileBuilder {
  val builder = DockerfileBuilder()
  builder.block()
  return builder
}
