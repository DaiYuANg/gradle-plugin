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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.allOf
import javax.inject.Inject

abstract class DockerPushTask
@Inject constructor(
  private val outputFactory: StyledTextOutputFactory
) : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerPush"
  }

  init {
    description = "Push Docker image to registry"
    group = "DOCKER"
  }

  @get:Input
  @get:Optional
  @get:Option(option = "image", description = "Docker image to push")
  abstract val image: Property<String>

  @get:Input
  @get:Optional
  @get:Option(option = "tags", description = "List of tags to push")
  abstract val tags: ListProperty<String>

  @TaskAction
  fun pushImages() {
    val img = image.orNull ?: throw IllegalArgumentException("Property 'image' must be set")
    val tagList = tags.orNull?.takeIf { it.isNotEmpty() } ?: listOf(img)

    val dockerConfig = project.extensions.getByType(DockerExtension::class.java)

    // 创建 StyledTextOutput
    val styledOut: StyledTextOutput = outputFactory.create("docker-push")
    styledOut.withStyle(StyledTextOutput.Style.Identifier)
      .println("📤 Pushing Docker image(s): ${tagList.joinToString(", ")}")

    // 异步推送
    val futures = tagList.map { tag ->
      CompletableFuture.runAsync {
        val pushCommand = DockerPushCommand(logger, tag).also {
          it.dockerPath = dockerConfig.dockerPath.get()
        }

        // 执行命令并获取输出
        val output = pushCommand.execute()

        // 渲染输出
        output.forEach { line ->
          val style = when {
            line.contains("error", ignoreCase = true) -> StyledTextOutput.Style.Failure
            line.contains("WARNING", ignoreCase = true) -> StyledTextOutput.Style.Info
            else -> StyledTextOutput.Style.Normal
          }
          styledOut.withStyle(style).println("[$tag] $line")
        }

        styledOut.withStyle(StyledTextOutput.Style.Success)
          .println("✅ Pushed Docker image: $tag")
      }
    }

    // 等待所有推送完成
    allOf(*futures.toTypedArray()).join()

    styledOut.withStyle(StyledTextOutput.Style.Success)
      .println("🎉 All Docker images pushed successfully!")
  }
}
