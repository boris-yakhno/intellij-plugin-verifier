package com.jetbrains.pluginverifier

import com.jetbrains.plugin.structure.intellij.version.IdeVersion
import com.jetbrains.pluginverifier.misc.replaceInvalidFileNameCharacters
import com.jetbrains.pluginverifier.parameters.filtering.PluginIdAndVersion
import java.nio.file.Path

sealed class VerificationTarget {

  data class Ide(val ideVersion: IdeVersion) : VerificationTarget() {
    override fun toString() = ideVersion.toString()
  }

  data class Plugin(val plugin: PluginIdAndVersion) : VerificationTarget() {
    override fun toString() = plugin.toString()
  }

  fun getReportDirectory(baseDirectory: Path): Path = when (this) {
    is VerificationTarget.Ide -> baseDirectory.resolve(ideVersion.asString().replaceInvalidFileNameCharacters())
    is VerificationTarget.Plugin -> baseDirectory.resolve("${plugin.pluginId} ${plugin.version}".replaceInvalidFileNameCharacters())
  }

}