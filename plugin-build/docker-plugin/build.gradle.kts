import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  alias(libs.plugins.pluginPublish)
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(gradleApi())
  testImplementation(libs.junit)
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
  }
}

val pluginId = "io.github.daiyuang.docker.plugin"
val pluginImplementationClass = "io.github.daiyuang.docker.plugin.DockerPlugin"
val pluginVersion = version.toString()
val pluginDescription = "A Gradle plugin to build, run and push Docker images using docker-java"
val pluginDisplayName = "Gradle Docker Plugin"
gradlePlugin {
  plugins {
    create(pluginId) {
      id = pluginId
      implementationClass = pluginImplementationClass
      version = pluginVersion
      description = pluginDescription
      displayName = pluginDisplayName
      tags.set(listOf("docker"))
    }
  }
}

gradlePlugin {
  website.set(githubUrl)
  vcsUrl.set(githubUrl)
}

// Use Detekt with type resolution for check
tasks.named("check").configure {
  this.setDependsOn(
    this.dependsOn.filterNot {
      it is TaskProvider<*> && it.name == "detekt"
    } + tasks.named("detektMain"),
  )
}

tasks.register("setupPluginUploadFromEnvironment") {
  doLast {
    val key = env.GRADLE_PUBLISH_KEY.orNull()
    val secret = env.GRADLE_PUBLISH_SECRET.orNull()

    if (key == null || secret == null) {
      throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
    }

    System.setProperty("gradle.publish.key", key)
    System.setProperty("gradle.publish.secret", secret)
  }
}
