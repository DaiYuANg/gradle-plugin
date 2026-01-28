package io.github.daiyuang.docker.plugin.task

import io.github.daiyuang.docker.plugin.command.DockerBuildCommand
import io.github.daiyuang.docker.plugin.dsl.DockerfileBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class DockerBuildTask : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerBuild"
    const val DEFAULT_DOCKER_FILE = "Dockerfile"
  }

  init {
    description = "Build Docker images using docker-java"

    // Default values
    buildContext.convention(project.layout.projectDirectory.asFile.absolutePath)
    dockerfile.convention(project.layout.projectDirectory.file(DEFAULT_DOCKER_FILE).asFile.absolutePath)
//    platform.convention(detectPlatform())
    noCache.convention(false)
    pull.convention(false)
    labels.convention(mapOf())
    buildArgs.convention(mapOf())
    cacheFrom.convention(listOf())
    printInspectAfterBuild.convention(false)
//    authConfigs.convention(AuthConfigurations())
    templateVars.convention(mapOf())
  }

//  @get:ServiceReference(DockerService.SERVICE_NAME)
//  abstract val dockerService: Property<DockerService>

  // -------------------------
  // Build context & Dockerfile
  // -------------------------

  @get:Input
  @get:Option(option = "path", description = "Docker build context directory")
  @get:Optional
  abstract val buildContext: Property<String>

  @get:Input
  @get:Option(option = "dockerfile", description = "Path to Dockerfile")
  @get:Optional
  abstract val dockerfile: Property<String>

  // -------------------------
  // Tags
  // -------------------------

  @get:Input
  @get:Option(option = "tag", description = "A single image tag")
  @get:Optional
  abstract val tag: Property<String>

  @get:Input
  @get:Option(option = "tags", description = "Multiple image tags (comma-separated or repeated flags)")
  @get:Optional
  abstract val tags: ListProperty<String>

  // -------------------------
  // Platform
  // -------------------------

  @get:Input
  @get:Option(option = "platform", description = "Target platform such as linux/amd64")
  @get:Optional
  abstract val platform: Property<String>

  // -------------------------
  // Build Args / Labels / Cache
  // -------------------------

  @get:Input
  @get:Option(option = "build-arg", description = "Build args: key=value (repeatable)")
  @get:Optional
  abstract val buildArgs: MapProperty<String, String>

  @get:Input
  @get:Option(option = "label", description = "Image label: key=value (repeatable)")
  @get:Optional
  abstract val labels: MapProperty<String, String>

  @get:Input
  @get:Option(option = "cache-from", description = "Cache from: comma-separated list")
  @get:Optional
  abstract val cacheFrom: ListProperty<String>

  // -------------------------
  // Flags
  // -------------------------

  @get:Input
  @get:Option(option = "no-cache", description = "Disable docker cache")
  @get:Optional
  abstract val noCache: Property<Boolean>

  @get:Input
  @get:Option(option = "pull", description = "Always pull base image")
  @get:Optional
  abstract val pull: Property<Boolean>

  @get:Input
  @get:Option(option = "target", description = "Build target for multi-stage Dockerfile")
  @get:Optional
  abstract val target: Property<String>

  @get:Input
  @get:Option(option = "printInspectAfterBuild", description = "print inspect infomation after docker build")
  @get:Optional
  abstract val printInspectAfterBuild: Property<Boolean>

  // -------------------------
  // Dockerfile DSL
  // -------------------------
  @get:Input
  @get:Optional
  abstract val dockerfileDsl: Property<DockerfileBuilder.() -> Unit>

  // -------------------------
  // Remote Dockerfile Template
  // -------------------------
  @get:Input
  @get:Optional
  abstract val remoteDockerfileTemplate: Property<String> // URL 或本地路径

  @get:Input
  @get:Optional
  abstract val templateVars: MapProperty<String, Any>

  // -------------------------
  // Build Logic
  // -------------------------

  @TaskAction
  fun buildAction() {
    logger.lifecycle("Starting Docker build...")

    // 1️⃣ Build context 和 Dockerfile
    val contextDir = buildContext.orNull ?: project.projectDir.absolutePath
    val dockerfilePath = dockerfile.orNull ?: "${contextDir}/Dockerfile"

    // 2️⃣ 收集 tags
    val tagList = tags.orNull?.takeIf { it.isNotEmpty() }
      ?: tag.orNull?.let { listOf(it) }
      ?: listOf("latest") // 默认 tag

    // 3️⃣ 构建 DockerBuildCommand 对象
    val buildCommand = DockerBuildCommand(
      logger = logger,
      contextDir = contextDir,
      dockerfile = dockerfilePath,
      tags = tagList,
      platform = platform.orNull,
      buildArgs = buildArgs.orNull ?: emptyMap(),
      labels = labels.orNull ?: emptyMap(),
      cacheFrom = cacheFrom.orNull ?: emptyList(),
      noCache = noCache.orNull ?: false,
      pull = pull.orNull ?: false,
      target = target.orNull,
      printInspect = printInspectAfterBuild.orNull ?: false
    )

    // 4️⃣ 执行 build
    buildCommand.execute()

    logger.lifecycle("Docker build finished for tags: ${tagList.joinToString(", ")}")
  }
}

