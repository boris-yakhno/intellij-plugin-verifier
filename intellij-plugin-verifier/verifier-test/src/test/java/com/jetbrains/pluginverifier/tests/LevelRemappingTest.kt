package com.jetbrains.pluginverifier.tests

import com.jetbrains.plugin.structure.base.problems.PluginProblem
import com.jetbrains.plugin.structure.intellij.plugin.IdePluginImpl
import com.jetbrains.plugin.structure.intellij.problems.LevelRemappingPluginCreationResultResolver
import com.jetbrains.plugin.structure.intellij.problems.ReleaseVersionAndPluginVersionMismatch
import com.jetbrains.plugin.structure.intellij.problems.ignore
import com.jetbrains.plugin.structure.intellij.problems.remapping.JsonUrlProblemLevelRemappingManager
import com.jetbrains.plugin.structure.intellij.problems.remapping.RemappingSet
import com.jetbrains.plugin.structure.intellij.version.ProductReleaseVersion
import com.jetbrains.plugin.structure.jar.PLUGIN_XML
import org.junit.Assert.fail
import org.junit.Test

class LevelRemappingTest {
  @Test
  fun `problem is remapped according to JSON rules and then explicitly ignored`() {
    val existingPluginResolver = JsonUrlProblemLevelRemappingManager
      .fromClassPathJson()
      .newDefaultResolver(RemappingSet.EXISTING_PLUGIN_REMAPPING_SET)
    val ignoringProblemResolver = LevelRemappingPluginCreationResultResolver(existingPluginResolver, ignore<ReleaseVersionAndPluginVersionMismatch>())

    val problems = listOf(
      ReleaseVersionAndPluginVersionMismatch(PLUGIN_XML, ProductReleaseVersion.parse("10"), "1.0")
    )

    val remappedProblems = ignoringProblemResolver.classify(IdePluginImpl(), problems)
    assertEmpty(remappedProblems)
  }

  private fun assertEmpty(problems: List<PluginProblem>) {
    if (problems.isNotEmpty()) {
      fail("No problems were expected, but found ${problems.size}: " + problems.joinToString { it.message })
    }
  }
}