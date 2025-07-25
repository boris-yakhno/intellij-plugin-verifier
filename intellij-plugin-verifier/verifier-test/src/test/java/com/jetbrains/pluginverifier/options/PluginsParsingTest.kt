package com.jetbrains.pluginverifier.options

import com.jetbrains.plugin.structure.base.utils.contentBuilder.ContentBuilder
import com.jetbrains.plugin.structure.base.utils.contentBuilder.buildZipFile
import com.jetbrains.plugin.structure.intellij.plugin.PluginArchiveManager
import com.jetbrains.pluginverifier.tests.mocks.MockPluginRepositoryAdapter
import com.jetbrains.pluginverifier.tests.mocks.TelemetryVerificationReportage
import com.jetbrains.pluginverifier.tests.mocks.createPluginArchiveManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path

class PluginsParsingTest {
  @Rule
  @JvmField
  val temporaryFolder = TemporaryFolder()

  private val pluginRepository = MockPluginRepositoryAdapter()

  private lateinit var pluginArchiveManager: PluginArchiveManager

  @Before
  fun setUp() {
    pluginArchiveManager = temporaryFolder.createPluginArchiveManager()
  }

  @Test
  fun `telemetry is gathered in plugin parsing`() {
    val plugins = PluginsSet()
    val reportage = TelemetryVerificationReportage()
    val pluginsParsing = PluginsParsing(pluginRepository, pluginArchiveManager, reportage, plugins)

    val pluginZip = buildPluginZipWithXml {
      """
        <idea-plugin>
          $HEADER
          <actions>
            <action class="someClass"/>
          </actions>
        </idea-plugin>
        """
    }

    pluginsParsing.addPluginFile(pluginZip, validateDescriptor = true)
    assertEquals(1, plugins.pluginsToCheck.size)

    val pluginInfo = plugins.pluginsToCheck.first()
    val telemetry = reportage[pluginInfo]
    if (telemetry != null) {
      assertTrue(telemetry.archiveFileSize > 0)
      val parsingDuration = telemetry.parsingDuration
      if (parsingDuration != null) {
        assertTrue(parsingDuration.toMillis() > 0)
      } else {
        fail("Plugin parsing duration must be set in telemetry and greater than zero")
      }

    } else {
      fail("Plugin telemetry not found for $pluginInfo")
    }
  }

  private fun buildPluginZipWithXml(pluginXmlContent: () -> String): Path {
    return buildPluginZip {
      dir("META-INF") {
        file("plugin.xml", pluginXmlContent())
      }
    }
  }

  private fun buildPluginZip(pluginContentBuilder: ContentBuilder.() -> Unit): Path {
    return buildZipFile(temporaryFolder.newFile("plugin.jar").toPath(), pluginContentBuilder)
  }

  private val HEADER = """
      <id>someId</id>
      <name>someName</name>
      <version>someVersion</version>
      ""<vendor email="vendor.com" url="url">vendor</vendor>""
      <description>this description is looooooooooong enough</description>
      <change-notes>these change-notes are looooooooooong enough</change-notes>
      <idea-version since-build="131.1"/>
    """

  @After
  fun tearDown() {
    pluginArchiveManager.close()
  }
}