package io.github.daiyuang.docker.plugin.model

data class ImageCoordinates(
  val registries: List<String>,
  val imageName: String,
  val tags: List<String>
) {

  init {
    require(imageName.isNotBlank()) {
      "imageName must not be blank"
    }
    require(!imageName.contains(":")) {
      "imageName must not contain ':'"
    }
    require(!imageName.startsWith("/")) {
      "imageName must not start with '/'"
    }

    registries.forEach {
      require(!it.contains("://")) {
        "registry must not contain scheme: $it"
      }
    }

    tags.forEach {
      require(it.isNotBlank()) {
        "tag must not be blank"
      }
    }
  }

  /**
   * 展开为完整 image refs
   * e.g. ghcr.io/org/app:1.0.0
   */
  fun toImageRefs(): List<String> {
    val rs = if (registries.isEmpty()) listOf(DEFAULT_REGISTRY) else registries
    val ts = if (tags.isEmpty()) listOf(DEFAULT_TAG) else tags

    return rs.flatMap { registry ->
      val prefix = if (registry.isBlank()) "" else "$registry/"
      ts.map { tag ->
        "$prefix$imageName:$tag"
      }
    }
  }

  companion object {
    const val DEFAULT_REGISTRY = "docker.io"
    const val DEFAULT_TAG = "latest"
  }
}

fun ImageCoordinates.refs(): List<ImageRef> =
  toImageRefs().map { ref ->
    val (name, tag) = ref.substringBeforeLast(":") to ref.substringAfterLast(":")
    val registry = name.substringBefore("/")
    ImageRef(registry, imageName, tag)
  }
