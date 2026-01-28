plugins {
  java
  id("io.github.daiyuang.docker.plugin")
  id("io.github.daiyuang.git.plugin")
}

tasks.dockerBuild {
  tags.set(listOf("example:latest", "example:20251205"))
  dockerfileDsl.set {
    from("redis:latest")
  }
}

dockerConfig{
}

println(project.version)
tasks.dockerRun {
  image = "redis:latest"
}

tasks.dockerPush{
  image = "example:latest"
}

git {
  enableVersion = true
  snapshotSuffix = "-SNAPSHOT"
}
