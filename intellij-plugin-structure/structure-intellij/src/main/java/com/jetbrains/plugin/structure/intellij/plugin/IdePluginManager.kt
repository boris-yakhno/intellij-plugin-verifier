/*
 * Copyright 2000-2025 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.jetbrains.plugin.structure.intellij.plugin

import com.jetbrains.plugin.structure.base.plugin.PluginCreationResult
import com.jetbrains.plugin.structure.base.plugin.PluginManager
import com.jetbrains.plugin.structure.base.plugin.Settings
import com.jetbrains.plugin.structure.base.problems.IncorrectZipOrJarFile
import com.jetbrains.plugin.structure.base.utils.exists
import com.jetbrains.plugin.structure.base.utils.isDirectory
import com.jetbrains.plugin.structure.base.utils.isJar
import com.jetbrains.plugin.structure.base.utils.isZip
import com.jetbrains.plugin.structure.base.utils.pluginSize
import com.jetbrains.plugin.structure.base.utils.simpleName
import com.jetbrains.plugin.structure.intellij.plugin.PluginArchiveManager.Result.Extracted
import com.jetbrains.plugin.structure.intellij.plugin.PluginArchiveManager.Result.Failed
import com.jetbrains.plugin.structure.intellij.plugin.PluginCreator.Companion.createInvalidPlugin
import com.jetbrains.plugin.structure.intellij.plugin.loaders.ContentModuleLoader
import com.jetbrains.plugin.structure.intellij.plugin.loaders.JarModuleLoader
import com.jetbrains.plugin.structure.intellij.plugin.loaders.JarOrDirectoryPluginLoader
import com.jetbrains.plugin.structure.intellij.plugin.loaders.JarPluginLoader
import com.jetbrains.plugin.structure.intellij.plugin.loaders.LibDirectoryPluginLoader
import com.jetbrains.plugin.structure.intellij.plugin.loaders.ModuleFromDescriptorLoader
import com.jetbrains.plugin.structure.intellij.plugin.loaders.PluginDirectoryLoader
import com.jetbrains.plugin.structure.intellij.plugin.loaders.PluginLoaderProvider
import com.jetbrains.plugin.structure.intellij.problems.IntelliJPluginCreationResultResolver
import com.jetbrains.plugin.structure.intellij.problems.PluginCreationResultResolver
import com.jetbrains.plugin.structure.intellij.resources.DefaultResourceResolver
import com.jetbrains.plugin.structure.intellij.resources.PluginArchiveResource
import com.jetbrains.plugin.structure.intellij.resources.ResourceResolver
import com.jetbrains.plugin.structure.intellij.version.IdeVersion
import com.jetbrains.plugin.structure.jar.JarFileSystemProvider
import com.jetbrains.plugin.structure.jar.SingletonCachingJarFileSystemProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.time.Duration
import kotlin.system.measureTimeMillis

/**
 * Factory for plugin of the IntelliJ Platform.
 *
 * Handles the plugin provided in JAR, ZIP or directory.
 */
@Suppress("UNCHECKED_CAST")
class IdePluginManager private constructor(
  private val myResourceResolver: ResourceResolver,
  private val pluginArchiveManager: PluginArchiveManager,
  private val fileSystemProvider: JarFileSystemProvider = SingletonCachingJarFileSystemProvider
) : PluginManager<IdePlugin> {

  private val pluginLoaderRegistry = PluginLoaderProvider().apply {
    register(JarPluginLoader.Context::class.java, JarPluginLoader(fileSystemProvider))
    register(JarModuleLoader.Context::class.java, JarModuleLoader(fileSystemProvider))
    register(PluginDirectoryLoader.Context::class.java, PluginDirectoryLoader(pluginLoaderRegistry = this))
    register(LibDirectoryPluginLoader.Context::class.java,
      LibDirectoryPluginLoader(pluginLoaderRegistry = this, fileSystemProvider)
    )
    register(ModuleFromDescriptorLoader.Context::class.java, ModuleFromDescriptorLoader())
    register(JarOrDirectoryPluginLoader.Context::class.java, JarOrDirectoryPluginLoader(pluginLoaderRegistry = this))
  }

  private val moduleFromDescriptorLoader = pluginLoaderRegistry.get<ModuleFromDescriptorLoader.Context, ModuleFromDescriptorLoader>()
  private val jarModuleLoader = pluginLoaderRegistry.get<JarModuleLoader.Context, JarModuleLoader>()
  private val jarOrDirLoader = pluginLoaderRegistry.get<JarOrDirectoryPluginLoader.Context, JarOrDirectoryPluginLoader>()

  private val contentModuleLoader = ContentModuleLoader(jarOrDirLoader, moduleFromDescriptorLoader)

  private val optionalDependencyResolver = OptionalDependencyResolver(object : PluginLoader {
    override fun load(
      pluginFile: Path,
      descriptorPath: String,
      validateDescriptor: Boolean,
      resourceResolver: ResourceResolver,
      parentPlugin: PluginCreator?,
      problemResolver: PluginCreationResultResolver
    ): PluginCreator {
      return jarOrDirLoader.loadPlugin(
        JarOrDirectoryPluginLoader.Context(
          pluginFile,
          descriptorPath,
          validateDescriptor,
          resourceResolver,
          parentPlugin,
          problemResolver
        )
      )
    }
  })

  private fun resolveOptionalDependencies(pluginFile: Path, pluginCreator: PluginCreator, resourceResolver: ResourceResolver, problemResolver: PluginCreationResultResolver) {
    if (pluginCreator.isSuccess) {
      optionalDependencyResolver.resolveOptionalDependencies(pluginCreator, pluginFile, resourceResolver, problemResolver)
    }
  }

  private fun resolveContentModules(
    pluginFile: Path,
    contentModulesOwner: PluginCreator,
    resourceResolver: ResourceResolver,
    problemResolver: PluginCreationResultResolver
  ) {
    with(contentModuleLoader.resolveContentModules(pluginFile, contentModulesOwner, resourceResolver, problemResolver)) {
      contentModules.forEach {
        contentModulesOwner.addContentModule(it.contentModule, it.descriptor)
      }
      problems.forEach {
        contentModulesOwner.registerProblem(it)
      }
    }
  }

  private fun PluginCreator.addContentModule(resolvedContentModule: IdePlugin, moduleDescriptor: ModuleDescriptor) {
    plugin.modulesDescriptors.add(moduleDescriptor)
    plugin.definedModules.add(moduleDescriptor.name)

    mergeContent(resolvedContentModule)
  }


  private fun extractPluginCreator(
    pluginFile: Path,
    descriptorPath: String,
    validateDescriptor: Boolean,
    resourceResolver: ResourceResolver,
    problemResolver: PluginCreationResultResolver,
  ): PluginCreator {
    return when (val archive = pluginArchiveManager.extractArchive(pluginFile)) {
      is Extracted -> {
        getPluginCreator(
          archive.extractedPath,
          descriptorPath,
          validateDescriptor,
          resourceResolver,
          problemResolver
        ).apply {
          resources += PluginArchiveResource.of(pluginFile, archive.extractedPath, plugin)
        }
      }
      is Failed -> createInvalidPlugin(
        pluginFile.simpleName,
        descriptorPath,
        archive.problem
      )
    }
  }

  override fun createPlugin(pluginFile: Path) = createPlugin(pluginFile, true)

  @Throws(PluginFileNotFoundException::class)
  fun createPlugin(
    pluginFile: Path,
    validateDescriptor: Boolean,
    descriptorPath: String = PLUGIN_XML,
    problemResolver: PluginCreationResultResolver = IntelliJPluginCreationResultResolver(),
  ): PluginCreationResult<IdePlugin> {
    val pluginCreator = getPluginCreatorWithResult(
      pluginFile,
      validateDescriptor,
      descriptorPath,
      problemResolver
    )
    return pluginCreator.pluginCreationResult
  }

  @Throws(PluginFileNotFoundException::class)
  fun createBundledPlugin(
    pluginFile: Path,
    ideVersion: IdeVersion,
    descriptorPath: String,
    problemResolver: PluginCreationResultResolver = IntelliJPluginCreationResultResolver(),
    fallbackPluginId: String? = null,
  ): PluginCreationResult<IdePlugin> {
    val pluginCreator = getPluginCreatorWithResult(
      pluginFile, false, descriptorPath, problemResolver,
    )
    pluginCreator.setPluginVersion(ideVersion.asStringWithoutProductCode())
    fallbackPluginId?.let { pluginCreator.setPluginIdIfNull(it) }
    return pluginCreator.pluginCreationResult
  }

  fun createBundledModule(
    pluginFile: Path,
    ideVersion: IdeVersion,
    descriptorPath: String,
    problemResolver: PluginCreationResultResolver = IntelliJPluginCreationResultResolver()
  ): PluginCreationResult<IdePlugin> {
    return jarModuleLoader
      .loadPlugin(JarModuleLoader.Context(pluginFile, descriptorPath, myResourceResolver, problemResolver))
      .apply {
        setPluginVersion(ideVersion.asStringWithoutProductCode())
        setOriginalFile(pluginFile)
      }.pluginCreationResult
  }

  @Throws(PluginFileNotFoundException::class)
  private fun getPluginCreatorWithResult(
    pluginFile: Path,
    validateDescriptor: Boolean,
    descriptorPath: String,
    problemResolver: PluginCreationResultResolver
  ): PluginCreator {
    if (!pluginFile.exists()) { throw PluginFileNotFoundException(pluginFile) }
    val pluginCreator: PluginCreator
    measureTimeMillis {
      if (pluginFile.isZip()) {
        pluginCreator = extractPluginCreator(pluginFile, descriptorPath, validateDescriptor, myResourceResolver, problemResolver)
      } else if (pluginFile.isJar() || pluginFile.isDirectory) {
        pluginCreator = getPluginCreator(pluginFile, descriptorPath, validateDescriptor, myResourceResolver, problemResolver)
      } else {
        pluginCreator = getInvalidPluginFileCreator(pluginFile.simpleName, descriptorPath)
      }
      pluginCreator.setOriginalFile(pluginFile)
    }.let { pluginCreationDuration -> pluginCreator.setTelemetry(pluginFile, pluginCreationDuration)}
    return pluginCreator
  }

  private fun getPluginCreator(
    pluginFile: Path,
    descriptorPath: String,
    validateDescriptor: Boolean,
    resourceResolver: ResourceResolver,
    problemResolver: PluginCreationResultResolver
  ): PluginCreator {
    val pluginCreator = jarOrDirLoader.loadPlugin(JarOrDirectoryPluginLoader.Context(pluginFile, descriptorPath, validateDescriptor, resourceResolver, null, problemResolver))
    resolveOptionalDependencies(pluginFile, pluginCreator, myResourceResolver, problemResolver)
    resolveContentModules(pluginFile, pluginCreator, myResourceResolver, problemResolver)

    return pluginCreator
  }

  private fun getInvalidPluginFileCreator(pluginFileName: String, descriptorPath: String): PluginCreator {
    return createInvalidPlugin(pluginFileName, descriptorPath, IncorrectZipOrJarFile(pluginFileName))
  }

  private fun PluginCreator.setTelemetry(pluginFile: Path, pluginCreationDurationInMillis: Long) {
    with(telemetry) {
      parsingDuration = Duration.ofMillis(pluginCreationDurationInMillis)
      archiveFileSize = pluginFile.pluginSize
    }
  }

  companion object {
    private val LOG = LoggerFactory.getLogger(IdePluginManager::class.java)
    const val PLUGIN_XML = "plugin.xml"
    const val META_INF = "META-INF"

    @JvmStatic
    fun createManager(): IdePluginManager =
      createManager(DefaultResourceResolver, Settings.EXTRACT_DIRECTORY.getAsPath())

    @JvmStatic
    fun createManager(resourceResolver: ResourceResolver): IdePluginManager =
      createManager(resourceResolver, Settings.EXTRACT_DIRECTORY.getAsPath())

    @JvmStatic
    fun createManager(extractDirectory: Path): IdePluginManager =
      createManager(DefaultResourceResolver, extractDirectory)

    @JvmStatic
    fun createManager(resourceResolver: ResourceResolver, extractDirectory: Path): IdePluginManager =
      IdePluginManager(resourceResolver, PluginArchiveManager(extractDirectory))

    @JvmStatic
    fun createManager(resourceResolver: ResourceResolver, extractDirectory: Path, fileSystemProvider: JarFileSystemProvider): IdePluginManager =
      IdePluginManager(resourceResolver, PluginArchiveManager(extractDirectory), fileSystemProvider)

    @JvmStatic
    fun createManager(
      resourceResolver: ResourceResolver,
      pluginArchiveManager: PluginArchiveManager,
      fileSystemProvider: JarFileSystemProvider
    ): IdePluginManager =
      IdePluginManager(resourceResolver, pluginArchiveManager, fileSystemProvider)

    @Deprecated(
      message = "Use factory method with java.nio.Path",
      replaceWith = ReplaceWith("createManager(extractDirectory.toPath())")
    )
    @JvmStatic
    fun createManager(extractDirectory: File): IdePluginManager =
      createManager(DefaultResourceResolver, extractDirectory.toPath())

    @Deprecated(
      message = "Use factory method with java.nio.Path",
      replaceWith = ReplaceWith("createManager(resourceResolver, extractDirectory.toPath())")
    )
    @JvmStatic
    fun createManager(resourceResolver: ResourceResolver, extractDirectory: File): IdePluginManager =
      createManager(resourceResolver, extractDirectory.toPath())
  }
}