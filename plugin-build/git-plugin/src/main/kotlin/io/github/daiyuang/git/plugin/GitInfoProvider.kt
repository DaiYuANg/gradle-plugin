package io.github.daiyuang.git.plugin

import org.eclipse.jgit.api.Git
import java.io.File

object GitInfoProvider {

  private fun openRepo(projectDir: File): Git {
    var dir: File? = projectDir
    while (dir != null && !File(dir, ".git").exists()) {
      dir = dir.parentFile
    }
    if (dir == null) throw IllegalStateException("Git repository not found from $projectDir")
    return Git.open(dir)
  }

  fun getCurrentBranch(projectDir: File): String {
    openRepo(projectDir).use { git ->
      return git.repository.fullBranch.removePrefix("refs/heads/")
    }
  }

  fun getLatestTag(projectDir: File, tagPattern: String): String {
    openRepo(projectDir).use { git ->
      val tags = git.tagList().call()
        .map { it.name.removePrefix("refs/tags/") }
        .filter { it.matches(Regex(tagPattern)) }
        .sortedWith(compareByDescending<String> { it }) // 简单按字符串排序，可用 SemVer 排序
      return tags.firstOrNull() ?: "0.0.0"
    }
  }

  fun getCurrentCommit(projectDir: File): String {
    openRepo(projectDir).use { git ->
      return git.repository.findRef("HEAD").objectId.name.substring(0, 7)
    }
  }

  fun isDirty(projectDir: File): Boolean {
    openRepo(projectDir).use { git ->
      val status = git.status().call()
      return status.hasUncommittedChanges()
    }
  }
}
