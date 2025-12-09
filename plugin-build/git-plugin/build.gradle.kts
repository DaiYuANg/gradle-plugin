import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  alias(libs.plugins.pluginPublish)
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(gradleApi())
  implementation(libs.apache.common.lang3)
  implementation(libs.jgit)
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

val pluginId = "io.github.daiyuang.git.plugin"
val pluginImplementationClass = "io.github.daiyuang.git.plugin.GitPlugin"
val pluginVersion = version.toString()
val pluginDescription = "A Gradle plugin to build, run and push Docker images using docker-java"
val pluginDisplayName = "Gradle Docker Plugin"

gradlePlugin {
  plugins {
    create(pluginId) {
      id = pluginId
      implementationClass = pluginImplementationClass
      description = pluginDescription
      displayName = pluginDisplayName
      tags.set(listOf("git", "version", "build-info"))
    }
  }
}

gradlePlugin {
  website.set(githubUrl)
  vcsUrl.set(githubUrl)
}
