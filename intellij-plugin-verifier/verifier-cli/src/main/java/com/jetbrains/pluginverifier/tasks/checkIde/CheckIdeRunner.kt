/*
 * Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.jetbrains.pluginverifier.tasks.checkIde

import com.jetbrains.plugin.structure.intellij.plugin.PluginArchiveManager
import com.jetbrains.pluginverifier.plugin.PluginDetailsCache
import com.jetbrains.pluginverifier.reporting.PluginVerificationReportage
import com.jetbrains.pluginverifier.repository.PluginRepository
import com.jetbrains.pluginverifier.tasks.CommandRunner

/**
 * Runner of the ['check-ide'] [CheckIdeTask] command.
 */
class CheckIdeRunner : CommandRunner {
  override val commandName: String = "check-ide"

  override fun getParametersBuilder(
    pluginRepository: PluginRepository,
    pluginDetailsCache: PluginDetailsCache,
    archiveManager: PluginArchiveManager,
    reportage: PluginVerificationReportage
  ) = CheckIdeParamsBuilder(pluginRepository, pluginDetailsCache, archiveManager, reportage)

}