package io.github.daiyuang.docker.plugin.task

import io.github.daiyuang.docker.plugin.DockerExtension
import io.github.daiyuang.docker.plugin.command.DockerPushCommand
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.internal.logging.text.StyledTextOutput
import javax.inject.Inject

abstract class DockerPushTask
@Inject constructor(
  private val outputFactory: StyledTextOutputFactory
) : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerPush"
  }

  init {
    description = "Push Docker images to one or more registries"
    group = "DOCKER"
  }

  // -------------------------
  // Properties
  // -------------------------
  @get:Input
  @get:Optional
  @get:Option(option = "image", description = "Docker image name (without tag)")
  abstract val imageName: Property<String>

  @get:Input
  @get:Optional
  @get:Option(option = "tags", description = "List of tags to push")
  abstract val tags: ListProperty<String>

  @get:Input
  @get:Optional
  @get:Option(option = "registries", description = "Docker registries to push to")
  abstract val registries: ListProperty<String>

  // -------------------------
  // Task Action
  // -------------------------
  @TaskAction
  fun pushImages() {
    val imgName = imageName.orNull
      ?: throw IllegalArgumentException("Property 'imageName' must be set")

    val tagList = tags.orNull?.takeIf { it.isNotEmpty() } ?: listOf("latest")

    val dockerConfig = project.extensions.getByType(DockerExtension::class.java)
    val registryList = registries.orNull ?: listOf("docker.io")

    val styledOut: StyledTextOutput = outputFactory.create("docker-push")
    styledOut.withStyle(StyledTextOutput.Style.Identifier)
      .println("📤 Pushing Docker image '$imgName'")
    styledOut.withStyle(StyledTextOutput.Style.Normal)
      .println("Tags: ${tagList.joinToString(", ")}")
    styledOut.withStyle(StyledTextOutput.Style.Normal)
      .println("Registries: ${registryList.joinToString(", ")}")

    // 创建命令对象
    val pushCommand = DockerPushCommand(
      logger = logger,
      registries = registryList,
      imageName = imgName,
      tags = tagList
    ).also {
      it.dockerPath = dockerConfig.dockerPath.get()
    }

    // 执行 push 并获取输出
    val output = pushCommand.execute()

    // 渲染输出
    output.forEach { line ->
      val style = when {
        line.contains("error", ignoreCase = true) -> StyledTextOutput.Style.Failure
        line.contains("WARNING", ignoreCase = true) -> StyledTextOutput.Style.Info
        else -> StyledTextOutput.Style.Normal
      }
      styledOut.withStyle(style).println(line)
    }

    styledOut.withStyle(StyledTextOutput.Style.Success)
      .println("🎉 Docker push completed for image '$imgName'")
  }
}
