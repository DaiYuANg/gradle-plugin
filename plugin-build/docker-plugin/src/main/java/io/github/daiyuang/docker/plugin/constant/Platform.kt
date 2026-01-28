package io.github.daiyuang.docker.plugin.constant

enum class Platform(val value: String) {
  LINUX_AMD64("linux/amd64"),
  LINUX_ARM64("linux/arm64"),
  LINUX_ARM_V7("linux/arm/v7"),
  LINUX_ARM_V6("linux/arm/v6"),
  LINUX_PPC64LE("linux/ppc64le"),
  LINUX_S390X("linux/s390x");

  override fun toString(): String = value

  companion object {
    fun from(value: String): Platform? =
      entries.firstOrNull { it.value == value }
  }
}
