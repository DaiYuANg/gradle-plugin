package io.github.daiyuang.git.plugin

import org.gradle.api.Project
import java.io.File
import java.time.Instant

object BuildInfoModule {
  fun writeBuildInfo(project: Project, extension: GitPluginExtension, version: String?, gitInfo: GitInfo?) {
    val file = File(project.layout.buildDirectory.asFile.get(), "resources/main/${extension.buildInfoFile}")
    file.parentFile.mkdirs()
    file.writeText(
      buildString {
        version?.let { appendLine("version=$it") }
        gitInfo?.let {
          appendLine("branch=${it.branch}")
          appendLine("commit=${it.commit}")
          appendLine("dirty=${it.dirty}")
        }
        appendLine("buildTime=${Instant.now()}")
      }
    )
  }
}
