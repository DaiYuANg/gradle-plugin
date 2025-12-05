package com.daiyuang.kotlin.gradle.docker.plugin

import com.daiyuang.kotlin.gradle.docker.plugin.model.TaskDef
import com.daiyuang.kotlin.gradle.docker.plugin.task.DockerBuildTask
import com.daiyuang.kotlin.gradle.docker.plugin.task.DockerInfoTask
import com.daiyuang.kotlin.gradle.docker.plugin.task.DockerPushTask
import com.daiyuang.kotlin.gradle.docker.plugin.task.DockerRunTask

const val DOCKER_TASK_GROUP = "docker"

val TASK_DEFS = listOf(
  TaskDef(DockerInfoTask.TASK_NAME, DockerInfoTask::class.java),
  TaskDef(DockerPushTask.TASK_NAME, DockerPushTask::class.java),
  TaskDef(DockerRunTask.TASK_NAME, DockerRunTask::class.java),
  TaskDef(DockerBuildTask.TASK_NAME, DockerBuildTask::class.java)
)
