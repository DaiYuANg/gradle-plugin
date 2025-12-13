package io.github.daiyuang.docker.plugin.task

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.PushResponseItem
import io.github.daiyuang.docker.plugin.service.DockerService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class DockerPushTask : DefaultTask() {

  companion object {
    const val TASK_NAME = "dockerPush"
  }

  init {
    description = "Push Docker image to registry"
    group = "DOCKER"
  }

  @get:ServiceReference(DockerService.SERVICE_NAME)
  abstract val dockerService: Property<DockerService>

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
    val docker = dockerService.get()
    val client = docker.client()
    val auth = docker.requireAuthConfig()

    val imageName = image.orNull
      ?: throw IllegalArgumentException("image must be provided")

    val tagList = resolveTags(imageName)

    tagList.forEach { fullTag ->
      logger.lifecycle("Pushing image $fullTag ...")

      client.pushImageCmd(fullTag)
        .apply {
          withAuthConfig(auth)
        }
        .exec(object : ResultCallback.Adapter<PushResponseItem>() {
          override fun onNext(item: PushResponseItem?) {
            when {
              item?.errorDetail != null -> {
                item.errorDetail?.let {
                  throw RuntimeException(
                    "Docker push failed: ${it.message}"
                  )
                }
              }

              item?.progressDetail?.current != null ->
                logger.lifecycle("  ${item.status}")

              item?.status != null ->
                logger.lifecycle("  ${item.status}")
            }
          }
        })
        .awaitCompletion()

      logger.lifecycle("Image $fullTag pushed successfully")
    }
  }

  private fun resolveTags(imageName: String): List<String> {
    val result = mutableListOf<String>()

    // 单 image（你当前用的就是这个）
    image.orNull?.let { result.add(it) }

    // 多 tag（可选）
    tags.orNull?.forEach { raw ->
      raw.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .forEach { tag ->
          val full = if (tag.contains(":")) tag else "$imageName:$tag"
          result.add(full)
        }
    }

    return result.distinct()
  }
}
