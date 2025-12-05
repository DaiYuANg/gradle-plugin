package com.daiyuang.kotlin.gradle.docker.plugin.task

import com.daiyuang.kotlin.gradle.docker.plugin.dsl.DockerfileBuilder
import com.daiyuang.kotlin.gradle.docker.plugin.dsl.dockerfile
import com.daiyuang.kotlin.gradle.docker.plugin.func.toPrettyJson
import com.daiyuang.kotlin.gradle.docker.plugin.service.DockerService
import com.daiyuang.kotlin.gradle.docker.plugin.service.DockerService.Companion.SERVICE_NAME
import com.github.dockerjava.api.model.AuthConfigurations
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.net.URI
import java.nio.file.Files

abstract class DockerBuildTask : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerBuild"
  }

  init {
    description = "Build Docker images using docker-java"

    // Default values
    buildContext.convention(project.layout.projectDirectory.asFile.absolutePath)
    dockerfile.convention(project.layout.projectDirectory.file("Dockerfile").asFile.absolutePath)
    platform.convention(detectPlatform())
    tags.convention(listOf(defaultTag(project.name)))
    noCache.convention(false)
    pull.convention(false)
    labels.convention(mapOf())
    buildArgs.convention(mapOf())
    cacheFrom.convention(listOf())
    printInspectAfterBuild.convention(false)
    authConfigs.convention(AuthConfigurations())
    templateVars.convention(mapOf())
  }

  @get:ServiceReference(SERVICE_NAME)
  abstract val dockerService: Property<DockerService>

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
  // Auth
  // -------------------------

  @get:Input
  @get:Optional
  abstract val authConfigs: Property<AuthConfigurations>

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

    val client = dockerService.get().client()

    val contextDir = File(buildContext.get())
    // -------------------------
    // Dockerfile 处理
    // -------------------------
    val dockerfilePath: File = when {
      dockerfileDsl.orNull != null -> {
        val tmp = Files.createTempFile("Dockerfile-", ".tmp").toFile()
        val content = dockerfile { dockerfileDsl.get().invoke(this) }.build()
        tmp.writeText(content)
        logger.lifecycle("Generated Dockerfile (DSL):\n$content")
        tmp
      }

      remoteDockerfileTemplate.orNull != null -> {
        val tmp = File(project.layout.buildDirectory.asFile.get(), "docker/Dockerfile-remote.tmp")
        tmp.parentFile.mkdirs()
        val templateContent = if (remoteDockerfileTemplate.get().startsWith("http")) {
          URI(remoteDockerfileTemplate.get()).toURL().readText()
        } else {
          File(remoteDockerfileTemplate.get()).readText()
        }

        val cfg = Configuration(Configuration.VERSION_2_3_31).apply {
          defaultEncoding = "UTF-8"
          templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
          logTemplateExceptions = false
          wrapUncheckedExceptions = true
        }

        val template = Template("dockerfile", StringReader(templateContent), cfg)
        val writer = StringWriter()
        template.process(templateVars.get(), writer)
        tmp.writeText(writer.toString())
        logger.lifecycle("Generated Dockerfile (Freemarker):\n${writer}")
        tmp
      }

      else -> File(dockerfile.get())
    }


    val allTags = resolveTags()

    logger.lifecycle("")
    logger.lifecycle("Docker Build Config:")
    logger.lifecycle("  context     = $contextDir")
    logger.lifecycle("  dockerfile  = $dockerfilePath")
    logger.lifecycle("  tags        = $allTags")
    logger.lifecycle("  platform    = ${platform.orNull}")
    logger.lifecycle("  noCache     = ${noCache.get()}")
    logger.lifecycle("  pull        = ${pull.get()}")
    logger.lifecycle("  buildArgs   = ${buildArgs.get()}")
    logger.lifecycle("  labels      = ${labels.get()}")
    logger.lifecycle("  cacheFrom   = ${cacheFrom.get()}")
    logger.lifecycle("")

    client.buildImageCmd().withBuildAuthConfigs(AuthConfigurations())
    // Build command
    val cmd = client.buildImageCmd()
      .withBaseDirectory(contextDir)
      .withDockerfile(dockerfilePath)
      .withTags(allTags)

    platform.orNull?.let { cmd.withPlatform(it) }
    target.orNull?.let { cmd.withTarget(it) }

    // flags
    cmd.withNoCache(noCache.get())
    cmd.withPull(pull.get())

    // build args
    if (buildArgs.get().isNotEmpty()) {
      buildArgs.get().forEach { (k, v) ->
        cmd.withBuildArg(k, v)
      }
    }

    // labels
    if (labels.get().isNotEmpty()) {
      cmd.withLabels(labels.get())
    }

    // cache-from
    if (cacheFrom.get().isNotEmpty()) {
      cmd.withCacheFrom(cacheFrom.get().toSet())
    }

    // Run build
    val imageId = cmd.start().awaitImageId()

    logger.lifecycle("Docker build completed successfully. ImageId = $imageId")

    if (printInspectAfterBuild.get()) {
      val inspectResult = client.inspectImageCmd(imageId).exec()
      logger.lifecycle("Image Inspect ${inspectResult.toPrettyJson()}")
    }
  }

  // ---------------------------------------
  // Helpers
  // ---------------------------------------

  private fun resolveTags(): Set<String> {
    val result = mutableSetOf<String>()

    tag.orNull?.let { result.add(it) }

    tags.orNull
      ?.flatMap { it.split(",") }
      ?.map { it.trim() }
      ?.filter { it.isNotBlank() }
      ?.let { result.addAll(it) }

    return result
  }

  private fun defaultTag(projectName: String): String {
    val normalized = projectName.replaceFirst("-", "/")
    return "$normalized:latest"
  }

  private fun detectPlatform(): String {
    return when (SystemUtils.OS_ARCH.lowercase()) {
      "aarch64", "arm64" -> "linux/arm64"
      "x86_64", "amd64" -> "linux/amd64"
      else -> "linux/amd64"
    }
  }
}
