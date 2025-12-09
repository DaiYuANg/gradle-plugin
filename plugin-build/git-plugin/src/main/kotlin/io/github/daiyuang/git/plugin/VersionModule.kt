package io.github.daiyuang.git.plugin

import org.gradle.api.Project

object VersionModule {
  fun calculateVersion(project: Project, extension: GitPluginExtension): String {
    val gitDir = project.projectDir
    val branch = GitInfoProvider.getCurrentBranch(gitDir)
    val commit = GitInfoProvider.getCurrentCommit(gitDir)
    val latestTag = GitInfoProvider.getLatestTag(gitDir, extension.tagPattern)
    val dirty = GitInfoProvider.isDirty(gitDir)

    return buildString {
      append(latestTag)
      if (branch != "main" && dirty) append(extension.snapshotSuffix)
      if (extension.includeCommitHash) append("+$commit")
    }
  }
}
