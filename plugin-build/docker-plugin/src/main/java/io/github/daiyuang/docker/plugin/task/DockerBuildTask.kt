package io.github.daiyuang.docker.plugin.task

import io.github.daiyuang.docker.plugin.DockerExtension
import io.github.daiyuang.docker.plugin.command.DockerBuildCommand
import io.github.daiyuang.docker.plugin.constant.Platform
import io.github.daiyuang.docker.plugin.dsl.DockerfileBuilder
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
import javax.inject.Inject

abstract class DockerBuildTask
@Inject constructor(
  private val outputFactory: StyledTextOutputFactory
) : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerBuild"
    const val DEFAULT_DOCKER_FILE = "Dockerfile"
  }

  init {
    description = "Build Docker images using docker-java"

    buildContext.convention(project.layout.projectDirectory.asFile.absolutePath)
    dockerfile.convention(project.layout.projectDirectory.file(DEFAULT_DOCKER_FILE).asFile.absolutePath)
    platform.convention(detectPlatform())
    noCache.convention(false)
    pull.convention(false)
    labels.convention(mapOf())
    buildArgs.convention(mapOf())
    cacheFrom.convention(listOf())
    printInspectAfterBuild.convention(false)
    templateVars.convention(mapOf())
  }

  // -------------------------
  // Properties
  // -------------------------
  @get:Input
  @get:Option(option = "path", description = "Docker build context directory")
  @get:Optional
  abstract val buildContext: Property<String>

  @get:Input
  @get:Option(option = "dockerfile", description = "Path to Dockerfile")
  @get:Optional
  abstract val dockerfile: Property<String>

  @get:Input
  @get:Option(option = "tag", description = "A single image tag")
  @get:Optional
  abstract val tag: Property<String>

  @get:Input
  @get:Option(option = "tags", description = "Multiple image tags")
  @get:Optional
  abstract val tags: ListProperty<String>

  @get:Input
  @get:Option(option = "platform", description = "Target platform")
  @get:Optional
  abstract val platform: Property<Platform>

  @get:Input
  @get:Option(option = "build-arg", description = "Build args")
  @get:Optional
  abstract val buildArgs: MapProperty<String, String>

  @get:Input
  @get:Option(option = "label", description = "Labels")
  @get:Optional
  abstract val labels: MapProperty<String, String>

  @get:Input
  @get:Option(option = "cache-from", description = "Cache from")
  @get:Optional
  abstract val cacheFrom: ListProperty<String>

  @get:Input
  @get:Option(option = "no-cache", description = "Disable cache")
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
  @get:Option(option = "printInspectAfterBuild", description = "Print inspect info after build")
  @get:Optional
  abstract val printInspectAfterBuild: Property<Boolean>

  @get:Input
  @get:Optional
  abstract val dockerfileDsl: Property<DockerfileBuilder.() -> Unit>

  @get:Input
  @get:Optional
  abstract val remoteDockerfileTemplate: Property<String>

  @get:Input
  @get:Optional
  abstract val templateVars: MapProperty<String, Any>

  // -------------------------
  // TaskAction
  // -------------------------
  @TaskAction
  fun buildAction() {
    val contextDir = buildContext.orNull ?: project.projectDir.absolutePath
    val dockerfilePath = dockerfile.orNull ?: "$contextDir/Dockerfile"

    val tagList = tags.orNull?.takeIf { it.isNotEmpty() }
      ?: tag.orNull?.let { listOf(it) }
      ?: listOf("latest")

    val dockerConfig = project.extensions.getByType(DockerExtension::class.java)

    // 检测本机平台
    val localPlatform = detectPlatform()

    // 自动启用 buildx，如果指定 platform 和本机 platform 不同
    val userPlatform = platform.orNull
    val useBuildx = userPlatform != null && userPlatform != localPlatform

    val buildCommand = DockerBuildCommand(
      logger = logger,
      contextDir = contextDir,
      dockerfile = dockerfilePath,
      tags = tagList,
      platform = userPlatform,
      buildArgs = buildArgs.orNull ?: emptyMap(),
      labels = labels.orNull ?: emptyMap(),
      cacheFrom = cacheFrom.orNull ?: emptyList(),
      noCache = noCache.orNull ?: false,
      pull = pull.orNull ?: false,
      target = target.orNull,
      printInspect = printInspectAfterBuild.orNull ?: false,
      useBuildx = useBuildx
    ).also {
      it.dockerPath = dockerConfig.dockerPath.get()
    }

    // 使用 StyledTextOutput 美化输出
    val styledOut = outputFactory.create("docker-build")
    styledOut.withStyle(StyledTextOutput.Style.Identifier)
      .println("🛠️  Building Docker image(s): ${tagList.joinToString(", ")}")
    if (useBuildx) {
      styledOut.withStyle(StyledTextOutput.Style.Info)
        .println("⚡ Platform ${userPlatform.value} differs from local ${localPlatform.value}, enabling Buildx")
    }

    val output = buildCommand.execute()
    output.forEach { line ->
      val style = when {
        line.contains("WARNING", ignoreCase = true) -> StyledTextOutput.Style.Failure
        line.contains("error", ignoreCase = true) -> StyledTextOutput.Style.Failure
        line.startsWith("Step ") -> StyledTextOutput.Style.Info
        line.startsWith(" --->") -> StyledTextOutput.Style.Success
        else -> StyledTextOutput.Style.Normal
      }
      styledOut.withStyle(style).println(line)
    }

    styledOut.withStyle(StyledTextOutput.Style.Success)
      .println("✅ Docker build finished for tags: ${tagList.joinToString(", ")}")
  }

  private fun detectPlatform(): Platform {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    return when {
      os.contains("linux") && arch.contains("amd64") -> Platform.LINUX_AMD64
      os.contains("linux") && arch.contains("aarch64") -> Platform.LINUX_ARM64
      os.contains("linux") && arch.startsWith("arm") -> Platform.LINUX_ARM_V7
      os.contains("mac") && arch.contains("x86_64") -> Platform.LINUX_AMD64
      os.contains("mac") && arch.contains("aarch64") -> Platform.LINUX_ARM64
      os.contains("windows") && arch.contains("amd64") -> Platform.LINUX_AMD64
      else -> Platform.LINUX_AMD64
    }
  }
}
