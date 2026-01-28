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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.allOf

abstract class DockerPushTask : DefaultTask() {

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
    // 创建 CompletableFuture 列表
    val futures = tagList.map { tag ->
      CompletableFuture.runAsync {
        logger.lifecycle("Pushing Docker image: $tag")
        val pushCommand = DockerPushCommand(logger, tag)
          .also {
            it.dockerPath = dockerConfig.dockerPath.get()
          }
        pushCommand.execute()
      }
    }

    // 等待所有 push 完成
    allOf(*futures.toTypedArray()).join()

    logger.lifecycle("Docker push completed for all tags!")
  }
}
