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

  @get:ServiceReference(DockerService.Companion.SERVICE_NAME)
  abstract val dockerService: Property<DockerService>

  @get:Input
  @get:Optional
  @get:Option(option = "image", description = "Docker image to push")
  abstract val image: Property<String>

  @get:Input
  @get:Optional
  @get:Option(option = "tags", description = "List of tags to push (if multiple)")
  abstract val tags: ListProperty<String>

  @TaskAction
  fun pushImages() {
    val client = dockerService.get().client()

    val imageName = image.orNull ?: throw IllegalArgumentException("image must be provided")
    val tagList = resolveTags(imageName)

    tagList.forEach { fullTag ->
      logger.lifecycle("Pushing image $fullTag ...")
      client.pushImageCmd(fullTag)
        .exec(object : ResultCallback.Adapter<PushResponseItem>() {
          override fun onNext(item: PushResponseItem?) {
            item?.progressDetail?.let { pd ->
              if (pd.total != null && pd.current != null) {
                logger.lifecycle("  ${item.status}: ${pd.current}/${pd.total}")
              } else {
                logger.lifecycle("  ${item.status}")
              }
            } ?: run {
              if (item?.status != null) logger.lifecycle("  ${item.status}")
            }
          }
        })
        .awaitCompletion()
      logger.lifecycle("Image $fullTag pushed successfully")
    }
  }

  private fun resolveTags(imageName: String): List<String> {
    val result = mutableListOf<String>()

    // 单 tag
    image.orNull?.let { result.add(it) }

    // 多 tag
    tags.orNull?.let { list ->
      list.flatMap { it.split(",") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .forEach { tag ->
          // docker-java push 需要完整 tag: image:tag
          val fullTag = if (tag.contains(":")) tag else "$imageName:$tag"
          result.add(fullTag)
        }
    }

    return result.distinct()
  }
}
