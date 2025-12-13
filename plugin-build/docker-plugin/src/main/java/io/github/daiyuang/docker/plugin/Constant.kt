package io.github.daiyuang.docker.plugin

import io.github.daiyuang.docker.plugin.model.TaskDef
import io.github.daiyuang.docker.plugin.task.DockerInfoTask
import io.github.daiyuang.docker.plugin.task.DockerRunTask
import io.github.daiyuang.docker.plugin.task.DockerBuildTask
import io.github.daiyuang.docker.plugin.task.DockerPushTask

const val DOCKER_TASK_GROUP = "docker"

val TASK_DEFS = listOf(
  TaskDef(
    DockerInfoTask.TASK_NAME,
    DockerInfoTask::class.java
  ),
  TaskDef(
    DockerPushTask.TASK_NAME,
    DockerPushTask::class.java
  ),
  TaskDef(
    DockerRunTask.TASK_NAME,
    DockerRunTask::class.java
  ),
  TaskDef(
    DockerBuildTask.TASK_NAME,
    DockerBuildTask::class.java
  )
)
