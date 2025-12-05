plugins {
  java
  id("com.daiyuang.kotlin.gradle.docker.plugin")
}

tasks.dockerBuild {
  tags.set(listOf<String>("example:latest", "example:20251205"))
  dockerfileDsl.set {
    from("redis:latest")
  }
}

tasks.dockerRun {
  image = "redis:latest"
}
