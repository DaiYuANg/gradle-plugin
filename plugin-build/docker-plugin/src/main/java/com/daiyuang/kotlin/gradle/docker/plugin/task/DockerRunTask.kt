package com.daiyuang.kotlin.gradle.docker.plugin.task

import com.daiyuang.kotlin.gradle.docker.plugin.service.DockerService
import com.daiyuang.kotlin.gradle.docker.plugin.service.DockerService.Companion.SERVICE_NAME
import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.util.*

abstract class DockerRunTask : DefaultTask() {

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

  @get:ServiceReference(SERVICE_NAME)
  abstract val dockerService: Property<DockerService>

  // -------------------------
  // Basic
  // -------------------------

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

  // -------------------------
  // Environment Variables
  // -------------------------

  @get:Input
  @get:Optional
  @get:Option(option = "env", description = "Environment variable key=value (repeatable)")
  abstract val env: MapProperty<String, String>

  // -------------------------
  // Ports: 8080:80 or 127.0.0.1:8080:80
  // -------------------------

  @get:Input
  @get:Optional
  @get:Option(option = "port", description = "Port mapping: HOST:CONTAINER or HOST_IP:HOST:CONTAINER")
  abstract val ports: ListProperty<String>

  // -------------------------
  // Volumes / Binds
  // -------------------------

  @get:Input
  @get:Optional
  @get:Option(option = "volume", description = "Container volume path")
  abstract val volumes: ListProperty<String>

  @get:Input
  @get:Optional
  @get:Option(option = "bind", description = "Bind mount: host:container[:ro|rw]")
  abstract val binds: ListProperty<String>

  // -------------------------
  // Labels
  // -------------------------

  @get:Input
  @get:Optional
  @get:Option(option = "label", description = "Container label: key=value")
  abstract val labels: MapProperty<String, String>

  // -------------------------
  // Network
  // -------------------------

  @get:Input
  @get:Optional
  @get:Option(option = "network", description = "Network name")
  abstract val network: Property<String>

  // -------------------------
  // Restart Policy
  // -------------------------

  @get:Input
  @get:Optional
  @get:Option(option = "restart", description = "Restart policy: no, on-failure, always, unless-stopped")
  abstract val restart: Property<String>

  // -------------------------
  // Commands
  // -------------------------

  @get:Input
  @get:Optional
  @get:Option(option = "commands", description = "Command and args (string)")
  abstract val commands: ListProperty<String>

  @get:Input
  @get:Optional
  @get:Option(option = "pullIfMissing", description = "Automatically pull image if missing")
  abstract val pullIfMissing: Property<Boolean>

  // -------------------------
  // Main action
  // -------------------------

  @TaskAction
  fun runAction() {
    logger.lifecycle("Starting docker run...")

    val client = dockerService.get().client()

    val imageName = image.orNull ?: throw IllegalArgumentException("image must be provided")
    if (pullIfMissing.orNull == true) {
      logger.lifecycle("Pulling image $imageName...")
      client.pullImageCmd(imageName).start().awaitCompletion()
    }

    // Parse port bindings
    val portBindings = parsePorts(ports.get())
    val exposedPorts = portBindings.map { it.exposedPort }

    // Parse volumes
    val volumeList = volumes.get().map { Volume(it) }.toList()

    // Parse binds
    val bindList = binds.get().map { parseBind(it) }.toList()

    // Restart policy
    val restartPolicy = parseRestartPolicy(restart.orNull)

    // Create container
    val cmd: CreateContainerCmd = client.createContainerCmd(imageName)
      .withName(containerName.orNull)
      .withEnv(env.get().map { "${it.key}=${it.value}" })
      .withHostConfig(
        HostConfig()
          .withAutoRemove(autoRemove.get())
          .withBinds(bindList)
          .withPortBindings(*portBindings.toTypedArray())
          .withNetworkMode(network.get())
          .withRestartPolicy(restartPolicy)
      )
      .withExposedPorts(exposedPorts)
      .withVolumes(volumeList)

    workdir.orNull?.let { cmd.withWorkingDir(it) }
    if (commands.get().isNotEmpty()) cmd.withCmd(commands.get())
    if (labels.get().isNotEmpty()) cmd.withLabels(labels.get())

    val containerId = cmd.exec().id

    logger.lifecycle("Container created: $containerId")

    // Start
    client.startContainerCmd(containerId).exec()

    logger.lifecycle("Container started: $containerId")
  }

  // -------------------------
  // Helpers
  // -------------------------

  private fun parsePorts(portDefs: List<String>): List<PortBinding> {
    return portDefs.map { def ->
      val parts = def.split(":")
      when (parts.size) {
        2 -> {
          // HOST:CONTAINER
          PortBinding(
            Ports.Binding.bindPort(parts[0].toInt()),
            ExposedPort.tcp(parts[1].toInt())
          )
        }

        3 -> {
          // HOST_IP:HOST:CONTAINER
          PortBinding(
            Ports.Binding.bindIpAndPort(parts[0], parts[1].toInt()),
            ExposedPort.tcp(parts[2].toInt())
          )
        }

        else -> throw IllegalArgumentException("Invalid port format: $def")
      }
    }
  }

  private fun parseBind(text: String): Bind {
    val parts = text.split(":")
    return when (parts.size) {
      2 -> Bind(parts[0], Volume(parts[1]))
      3 -> {
        val mode = if (parts[2].equals("ro", true)) AccessMode.ro else AccessMode.rw
        Bind(parts[0], Volume(parts[1]), mode)
      }

      else -> throw IllegalArgumentException("Invalid bind format: $text")
    }
  }

  private fun parseRestartPolicy(value: String?): RestartPolicy {
    return when (value?.lowercase()) {
      null, "", "no" -> RestartPolicy.noRestart()
      "always" -> RestartPolicy.alwaysRestart()
      "unless-stopped" -> RestartPolicy.unlessStoppedRestart()
      "on-failure" -> RestartPolicy.onFailureRestart(0)
      else -> throw IllegalArgumentException("Invalid restart policy: $value")
    }
  }
}
