package io.github.daiyuang.git.plugin

import org.gradle.api.Project

object CiModule {
  fun injectCiInfo(project: Project) {
    // 例如：GitHub Actions 环境
    val ci = System.getenv("CI") ?: "false"
    val githubWorkflow = System.getenv("GITHUB_WORKFLOW") ?: ""
    val githubRunId = System.getenv("GITHUB_RUN_ID") ?: ""
    project.extensions.extraProperties.set("ci", mapOf(
      "ci" to ci,
      "workflow" to githubWorkflow,
      "runId" to githubRunId
    ))
  }
}
