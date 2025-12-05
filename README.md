# Gradle Docker Plugin 🐳

A Gradle plugin written in 100% Kotlin to simplify Docker image build, run, and push tasks using docker-java
.

- This plugin allows you to:

- Build Docker images from Gradle with flexible configuration.

- Support multiple tags, platforms, build args, labels, and cache options.

- Push images to a Docker registry with authentication.

- Run containers with customizable options and auto-generated container names.

- Inspect images directly after building.

## Plugin ID

```kotlin
plugins {
  id("com.daiyuang.kotlin.gradle.docker.plugin") version "0.0.2"
}
```

## Features

- Docker Build: Fully configurable dockerBuild task with support for:

  - tags (single or multiple)

  - platforms (single or multi-platform builds)

  - buildArgs, labels, cacheFrom

  - noCache, pull flags

  - Multi-stage target builds

  - Optional image inspection after build

- Docker Push: Push images to a registry with dockerPush.

- Docker Run: Run containers with dockerRun, auto-generating container names if not provided.

- Docker Info: Inspect Docker environment via dockerInfo.

- Gradle Kotlin DSL fully supported.

- Uses Gradle SharedService to reuse a Docker client across tasks.

- Minimal boilerplate, easy to extend.

## Usage

### Build a Docker image

```kotlin
tasks.dockerBuild {
  buildContext.set(project.layout.projectDirectory.asFile.absolutePath)
  dockerfile.set("${project.projectDir}/Dockerfile")
  tags.set(listOf("${project.name}:latest", "${project.name}:1.0.0"))
  platforms.set(listOf("linux/amd64", "linux/arm64"))
  buildArgs.set(mapOf("VERSION" to "1.0.0"))
  labels.set(mapOf("maintainer" to "daiyuang"))
  pull.set(true)
  noCache.set(false)
  printInspectAfterBuild.set(true)
}
```

### Push a Docker image

```kotlin
tasks.dockerPush {
  tags.set(listOf("${project.name}:latest"))
  registryUrl.set("https://index.docker.io/v1/")
  registryUsername.set("myuser")
  registryPassword.set("mypassword")
}
```

### Run a container

```kotlin
tasks.dockerRun {
  image.set("${project.name}:latest")
  containerName.set("my-container") // optional, auto-generated if not set
  detach.set(true)
  ports.set(mapOf("8080" to "8080"))
  envVars.set(mapOf("ENV" to "dev"))
  pullIfMissing.set(true) // default true
}
```

### Docker Info

```kotlin
tasks.dockerInfo {
  // Prints Docker client and server info
}
```

## Default Behavior

- Tags: If not provided, defaults to ${project.name}:latest.

- Platform: Defaults to the host architecture:

- x86_64 → linux/amd64

- aarch64 → linux/arm64

- Container Name: Randomly generated if not specified, similar to Docker’s default naming.

- Pull Base Image: Default true when running or building.

## Extension Configuration

You can optionally configure the Docker client globally via dockerConfig extension:

```kotlin
dockerConfig {
  dockerHost.set("tcp://localhost:2375")
  apiVersion.set("1.41")
  dockerTlsVerify.set(false)
  dockerCertPath.set("${project.rootDir}/.docker/certs")

  registryUrl.set("https://index.docker.io/v1/")
  registryUsername.set("myuser")
  registryPassword.set("mypassword")
  registryEmail.set("me@example.com")
}
```

## License

GPL License. See [LICENSE](LICENSE.txt) for details.

## Contributing

Feel free to open issues or submit pull requests for bug fixes and improvements.
