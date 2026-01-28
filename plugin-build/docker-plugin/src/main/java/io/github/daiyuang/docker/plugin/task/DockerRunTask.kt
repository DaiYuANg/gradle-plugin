package io.github.daiyuang.docker.plugin.task

import io.github.daiyuang.docker.plugin.DockerExtension
import io.github.daiyuang.docker.plugin.command.DockerRunCommand
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.internal.logging.text.StyledTextOutput
import java.util.*
import javax.inject.Inject

abstract class DockerRunTask
@Inject constructor(
  private val outputFactory: StyledTextOutputFactory
) : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerRun"
  }

  init {
    description = "Run Docker container using docker-java"

    image.convention(project.name.replace("-", "/") + ":latest")
    containerName.convention(UUID.randomUUID().toString())
    detach.convention(true)
    autoRemove.convention(true)
    env.convention(emptyMap())
    ports.convention(emptyList())
    volumes.convention(emptyList())
    binds.convention(emptyList())
    labels.convention(emptyMap())
    network.convention("bridge")
    pullIfMissing.convention(true)
  }

  @get:Input
  @get:Option(option = "image", description = "Docker image to run")
  abstract val image: Property<String>

  @get:Input
  @get:Optional
  @get:Option(option = "containerName", description = "Container name")
  abstract val containerName: Property<String>

  @get:Input
  @get:Option(option = "detach", description = "Run container in detached mode")
  @get:Optional
  abstract val detach: Property<Boolean>

  @get:Input
  @get:Option(option = "rm", description = "Auto-remove container when stopped")
  @get:Optional
  abstract val autoRemove: Property<Boolean>

  @get:Input
  @get:Optional
  @get:Option(option = "workdir", description = "Working directory inside container")
  abstract val workdir: Property<String>

  @get:Input
  @get:Optional
  @get:Option(option = "env", description = "Environment variable key=value (repeatable)")
  abstract val env: MapProperty<String, String>

  @get:Input
  @get:Optional
  @get:Option(option = "port", description = "Port mapping: HOST:CONTAINER or HOST_IP:HOST:CONTAINER")
  abstract val ports: ListProperty<String>

  @get:Input
  @get:Optional
  @get:Option(option = "volume", description = "Container volume path")
  abstract val volumes: ListProperty<String>

  @get:Input
  @get:Optional
  @get:Option(option = "bind", description = "Bind mount: host:container[:ro|rw]")
  abstract val binds: ListProperty<String>

  @get:Input
  @get:Optional
  @get:Option(option = "label", description = "Container label: key=value")
  abstract val labels: MapProperty<String, String>

  @get:Input
  @get:Optional
  @get:Option(option = "network", description = "Network name")
  abstract val network: Property<String>

  @get:Input
  @get:Optional
  @get:Option(option = "restart", description = "Restart policy: no, on-failure, always, unless-stopped")
  abstract val restart: Property<String>

  @get:Input
  @get:Optional
  @get:Option(option = "commands", description = "Command and args (string)")
  abstract val commands: ListProperty<String>

  @get:Input
  @get:Optional
  @get:Option(option = "pullIfMissing", description = "Automatically pull image if missing")
  abstract val pullIfMissing: Property<Boolean>

  @TaskAction
  fun runAction() {
    val dockerConfig = project.extensions.getByType(DockerExtension::class.java)

    val styledOut: StyledTextOutput = outputFactory.create("docker-run")
    styledOut.withStyle(StyledTextOutput.Style.Identifier)
      .println("▶️ Running Docker container: ${image.get()}")

    val dockerCommand = DockerRunCommand(
      logger = logger,
      image = image.get(),
      name = containerName.orNull,
      rm = autoRemove.orNull ?: false,
      detach = detach.orNull ?: false,
      volumes = volumes.orNull ?: emptyList(),
      ports = ports.orNull ?: emptyList(),
      env = env.orNull ?: emptyMap(),
      extraArgs = emptyList() // 如果你有额外参数可以传入
    ).also {
      it.dockerPath = dockerConfig.dockerPath.get()
    }

    // 执行命令并获取输出
    val output = dockerCommand.execute()
    output.forEach { line ->
      val style = when {
        line.contains("error", ignoreCase = true) -> StyledTextOutput.Style.Failure
        line.contains("WARNING", ignoreCase = true) -> StyledTextOutput.Style.Info
        else -> StyledTextOutput.Style.Normal
      }
      styledOut.withStyle(style).println(line)
    }

    styledOut.withStyle(StyledTextOutput.Style.Success)
      .println("✅ Docker container ${containerName.orNull ?: "anonymous"} started successfully!")
  }
}
