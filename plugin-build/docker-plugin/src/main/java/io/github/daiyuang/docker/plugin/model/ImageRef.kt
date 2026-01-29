package io.github.daiyuang.docker.plugin.model

data class ImageRef(
  val registry: String,
  val imageName: String,
  val tag: String
) {
  init {
    require(!imageName.contains(":"))
    require(!registry.contains("://"))
  }

  override fun toString(): String =
    if (registry.isBlank()) "$imageName:$tag"
    else "$registry/$imageName:$tag"
}
