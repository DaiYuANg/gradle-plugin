package io.github.daiyuang.docker.plugin

import io.github.daiyuang.docker.plugin.model.TaskDef
import io.github.daiyuang.docker.plugin.task.DockerInfoTask
import io.github.daiyuang.docker.plugin.task.DockerRunTask
import io.github.daiyuang.docker.plugin.task.DockerBuildTask
import io.github.daiyuang.docker.plugin.task.DockerPushTask

const val DOCKER_TASK_GROUP = "docker"

val TASK_DEFS = listOf(
  TaskDef(
    DockerInfoTask.Companion.TASK_NAME,
    DockerInfoTask::class.java
  ),
  TaskDef(
    DockerPushTask.Companion.TASK_NAME,
    DockerPushTask::class.java
  ),
  TaskDef(
    DockerRunTask.Companion.TASK_NAME,
    DockerRunTask::class.java
  ),
  TaskDef(
    DockerBuildTask.Companion.TASK_NAME,
    DockerBuildTask::class.java
  )
)
