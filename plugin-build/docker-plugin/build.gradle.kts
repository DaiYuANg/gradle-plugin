import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  alias(libs.plugins.pluginPublish)
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(gradleApi())
  implementation(libs.docker.java.core)
  implementation(libs.docker.java.transport.httpclient5)
  implementation(libs.apache.common.lang3)
  implementation(libs.jackson.core)
  implementation(libs.freemarker)
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

val pluginId = "com.daiyuang.kotlin.gradle.docker.plugin"
val pluginImplementationClass = "com.daiyuang.kotlin.gradle.docker.plugin.DockerPlugin"
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

val githubUrl = "https://github.com/DaiYuANg/gradle-docker-plugin"
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
    val key = System.getenv("GRADLE_PUBLISH_KEY")
    val secret = System.getenv("GRADLE_PUBLISH_SECRET")

    if (key == null || secret == null) {
      throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
    }

    System.setProperty("gradle.publish.key", key)
    System.setProperty("gradle.publish.secret", secret)
  }
}
