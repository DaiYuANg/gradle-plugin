package io.github.daiyuang.git.plugin

import org.gradle.api.Project

data class GitInfo(val branch: String, val commit: String, val latestTag: String, val dirty: Boolean)

object GitModule {
  fun getGitInfo(project: Project): GitInfo {
    val gitDir = project.projectDir
    val branch = GitInfoProvider.getCurrentBranch(gitDir)
    val commit = GitInfoProvider.getCurrentCommit(gitDir)
    val latestTag = GitInfoProvider.getLatestTag(gitDir, "v[0-9]+\\.[0-9]+\\.[0-9]+")
    val dirty = GitInfoProvider.isDirty(gitDir)
    return GitInfo(branch, commit, latestTag, dirty)
  }
}
