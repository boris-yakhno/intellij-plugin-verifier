package com.jetbrains.pluginverifier.tests.tasks

import com.jetbrains.plugin.structure.classes.resolvers.EmptyResolver
import com.jetbrains.plugin.structure.intellij.plugin.PluginDependencyImpl
import com.jetbrains.plugin.structure.intellij.version.IdeVersion.createIdeVersion
import com.jetbrains.pluginverifier.dependencies.DependencyNode
import com.jetbrains.pluginverifier.ide.IdeDescriptor
import com.jetbrains.pluginverifier.plugin.PluginDetails
import com.jetbrains.pluginverifier.reporting.verification.VerificationReportageImpl
import com.jetbrains.pluginverifier.repository.files.IdleFileLock
import com.jetbrains.pluginverifier.repository.local.LocalPluginInfo
import com.jetbrains.pluginverifier.repository.local.LocalPluginRepository
import com.jetbrains.pluginverifier.results.Verdict
import com.jetbrains.pluginverifier.tasks.checkTrunkApi.CheckTrunkApiParams
import com.jetbrains.pluginverifier.tasks.checkTrunkApi.CheckTrunkApiTask
import com.jetbrains.pluginverifier.tests.mocks.*
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.net.URL
import java.nio.file.Paths

/**
 * Created by Sergey.Patrikeev
 */
@Suppress("MemberVisibilityCanPrivate")
class CheckTrunkApiTaskTest {

  val someJetBrainsPluginId = "org.jetbrains.plugin"

  val someJetBrainsModule = "org.jetbrains.module"

  val someJetBrainsPluginContainingModuleId = "org.jetbrains.module.container"

  val repositoryURL: URL = URL("http://unnecessary.com")

  val someJetBrainsMockPlugin1 = MockIdePlugin(
      pluginId = someJetBrainsPluginId,
      pluginVersion = "1.0",
      originalFile = File("jetbrains.1.0")
  )

  val someJetBrainsMockPlugin2 = MockIdePlugin(
      pluginId = someJetBrainsPluginId,
      pluginVersion = "2.0",
      originalFile = File("jetbrains.2.0")
  )

  val someJetBrainsMockPluginContainingModule = MockIdePlugin(
      pluginId = someJetBrainsPluginContainingModuleId,
      pluginVersion = "1.0",
      definedModules = setOf(someJetBrainsModule),
      originalFile = File("jetbrains.module.container")
  )

  val pluginToCheck = MockIdePlugin(
      pluginId = "plugin.to.check",
      pluginVersion = "1.0",
      dependencies = listOf(
          //Dependency by id
          PluginDependencyImpl(someJetBrainsPluginId, false, false),

          //Dependency on module which is defined in [someJetBrainsPluginContainingModuleId]
          PluginDependencyImpl(someJetBrainsModule, false, true)
      ),
      originalFile = File("plugin.to.check.file")
  )

  val releaseVersion = createIdeVersion("IU-173.1")

  val trunkVersion = createIdeVersion("IU-181.1")

  val releaseIde = MockIde(releaseVersion)

  val trunkIde = MockIde(trunkVersion)

  @Test
  fun `local plugins repository is used and the local plugins are resolved both by id and by module id`() {
    val releaseLocalPluginsRepository = createReleaseLocalRepositoryForTest()
    val trunkLocalPluginsRepository = createTrunkLocalRepositoryForTest()
    val checkTrunkApiParams = createTrunkApiParamsForTest(releaseIde, trunkIde, releaseLocalPluginsRepository, trunkLocalPluginsRepository)
    val pluginDetailsProvider = createPluginDetailsProviderForTest(pluginToCheck)
    val checkTrunkApiTask = CheckTrunkApiTask(
        checkTrunkApiParams,
        EmptyPublicPluginRepository,
        pluginDetailsProvider
    )

    assertPluginsAreProperlyResolved(checkTrunkApiTask)
  }

  private fun assertPluginsAreProperlyResolved(checkTrunkApiTask: CheckTrunkApiTask) {
    val (_, releaseResults, _, trunkResults, _) = checkTrunkApiTask.execute(VerificationReportageImpl(EmptyReporterSetProvider))
    val trunkVerdict = trunkResults.single().verdict
    val releaseVerdict = releaseResults.single().verdict

    assertThat(trunkVerdict, instanceOf(Verdict.OK::class.java))
    assertThat(releaseVerdict, instanceOf(Verdict.OK::class.java))

    val trunkGraph = (trunkVerdict as Verdict.OK).dependenciesGraph
    val releaseGraph = (releaseVerdict as Verdict.OK).dependenciesGraph

    assertThat(trunkGraph.vertices.drop(1), containsInAnyOrder(
        DependencyNode(someJetBrainsPluginId, "2.0", emptyList()),
        DependencyNode(someJetBrainsPluginContainingModuleId, "1.0", emptyList())
    ))

    assertThat(releaseGraph.vertices.drop(1), containsInAnyOrder(
        DependencyNode(someJetBrainsPluginId, "1.0", emptyList()),
        DependencyNode(someJetBrainsPluginContainingModuleId, "1.0", emptyList())
    ))
  }

  private fun createPluginDetailsProviderForTest(pluginToCheck: MockIdePlugin): MockPluginDetailsProvider {
    return MockPluginDetailsProvider(
        listOf(someJetBrainsMockPlugin1, someJetBrainsMockPlugin2, someJetBrainsMockPluginContainingModule, pluginToCheck)
            .associateBy({ createMockLocalPluginInfo(it) }, { PluginDetails.FoundOpenPluginWithoutClasses(it) })
    )
  }

  private fun createMockLocalPluginInfo(idePlugin: MockIdePlugin): LocalPluginInfo {
    val localPluginRepository = LocalPluginRepository(repositoryURL)
    val localPluginInfo = with(idePlugin) {
      LocalPluginInfo(
          pluginId!!,
          pluginVersion!!,
          localPluginRepository,
          pluginName,
          sinceBuild,
          untilBuild,
          vendor,
          originalFile!!.toPath(),
          definedModules
      )
    }
    localPluginRepository.plugins.add(localPluginInfo)
    return localPluginInfo
  }

  private fun createTrunkApiParamsForTest(
      releaseIde: MockIde,
      trunkIde: MockIde,
      releaseLocalPluginsRepository: LocalPluginRepository,
      trunkLocalPluginsRepository: LocalPluginRepository
  ): CheckTrunkApiParams {
    return CheckTrunkApiParams(
        IdeDescriptor(releaseIde, EmptyResolver),
        IdeDescriptor(trunkIde, EmptyResolver),
        emptyList(),
        emptyList(),
        TestJdkDescriptorProvider.getJdkDescriptorForTests(),
        listOf(someJetBrainsPluginId),
        false,
        IdleFileLock(Paths.get("release ide file")),
        releaseLocalPluginsRepository,
        trunkLocalPluginsRepository,
        listOf(createMockLocalPluginInfo(pluginToCheck))
    )
  }

  private fun createReleaseLocalRepositoryForTest(): LocalPluginRepository {
    val localPluginRepository = LocalPluginRepository(repositoryURL)
    val plugins = listOf(
        LocalPluginInfo(
            someJetBrainsPluginId,
            "1.0",
            localPluginRepository,
            "plugin name",
            releaseVersion,
            releaseVersion,
            "JetBrains",
            someJetBrainsMockPlugin1.originalFile!!.toPath(), emptySet()
        ),

        LocalPluginInfo(
            someJetBrainsPluginContainingModuleId,
            "1.0",
            localPluginRepository,
            "module container",
            releaseVersion,
            releaseVersion,
            "JetBrains",
            someJetBrainsMockPluginContainingModule.originalFile!!.toPath(), setOf(someJetBrainsModule)
        )
    )
    localPluginRepository.plugins.addAll(plugins)
    return localPluginRepository
  }

  private fun createTrunkLocalRepositoryForTest(): LocalPluginRepository {
    val localPluginRepository = LocalPluginRepository(repositoryURL)
    val plugins = listOf(
        LocalPluginInfo(
            someJetBrainsPluginId,
            "2.0",
            localPluginRepository,
            "plugin name",
            trunkVersion,
            trunkVersion,
            "JetBrains",
            someJetBrainsMockPlugin2.originalFile!!.toPath(), emptySet()
        ),

        LocalPluginInfo(
            someJetBrainsPluginContainingModuleId,
            "1.0",
            localPluginRepository,
            "module container",
            trunkVersion,
            trunkVersion,
            "JetBrains",
            someJetBrainsMockPluginContainingModule.originalFile!!.toPath(), setOf(someJetBrainsModule)
        )
    )
    localPluginRepository.plugins.addAll(plugins)
    return localPluginRepository
  }


}