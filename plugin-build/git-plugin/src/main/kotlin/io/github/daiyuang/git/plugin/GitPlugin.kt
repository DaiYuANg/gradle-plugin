package io.github.daiyuang.git.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("git", GitPluginExtension::class.java)

    project.afterEvaluate {
      val root = project.rootProject
      val extra = root.extensions.extraProperties

      // 版本号模块
      val version = if (extension.enableVersion) {
        if (extra.has("computedVersion")) {
          extra["computedVersion"] as String
        } else {
          val v = VersionModule.calculateVersion(root, extension)
          extra.set("computedVersion", v)
          v
        }
      } else null

      // Git 模块
      val gitInfo = if (extension.enableGit) GitModule.getGitInfo(root) else null

      // BuildInfo 模块
      if (extension.enableBuildInfo) {
        BuildInfoModule.writeBuildInfo(
          project = root,
          extension = extension,
          version = version,
          gitInfo = gitInfo
        )
      }

      // CI/CD 模块
      if (extension.enableCi) {
        CiModule.injectCiInfo(root)
      }

      // 将 version 设置给所有项目
      version?.let {
        root.allprojects { it.version = it }
      }
    }
  }
}
