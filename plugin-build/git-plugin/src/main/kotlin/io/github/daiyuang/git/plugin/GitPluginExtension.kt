package io.github.daiyuang.git.plugin

open class GitPluginExtension {
  var enableVersion: Boolean = true
  var enableGit: Boolean = true
  var enableBuildInfo: Boolean = true
  var enableCi: Boolean = true

  // version 模块
  var tagPattern: String = "v[0-9]+\\.[0-9]+\\.[0-9]+"
  var snapshotSuffix: String = "-SNAPSHOT"
  var includeCommitHash: Boolean = true

  // build info 文件名
  var buildInfoFile: String = "build-info.properties"
}
