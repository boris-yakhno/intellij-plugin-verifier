package com.jetbrains.pluginverifier.output.settings.dependencies

import com.jetbrains.plugin.structure.intellij.plugin.PluginDependency

/**
 * @author Sergey Patrikeev
 */
class SpecifiedMissingDependencyIgnoring(val ignoredMissingDependencies: Set<String>) : MissingDependencyIgnoring {
  override fun ignoreMissingOptionalDependency(pluginDependency: PluginDependency): Boolean =
      pluginDependency.id in ignoredMissingDependencies
}