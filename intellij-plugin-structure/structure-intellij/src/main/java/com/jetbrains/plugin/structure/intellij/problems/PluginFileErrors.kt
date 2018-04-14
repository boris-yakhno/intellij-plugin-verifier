package com.jetbrains.plugin.structure.intellij.problems

import com.jetbrains.plugin.structure.base.plugin.PluginProblem

class PluginZipIsEmpty : PluginProblem() {

  override val level = PluginProblem.Level.ERROR

  override val message = "Plugin file is empty"

}

class PluginZipContainsUnknownFile(fileName: String) : PluginProblem() {

  override val level = PluginProblem.Level.ERROR

  override val message = "Plugin .zip file contains an unexpected file '$fileName'"

}

class PluginZipContainsMultipleFiles(fileNames: List<String>) : PluginProblem() {

  override val level = PluginProblem.Level.ERROR

  override val message = "Plugin root directory must not contain multiple files: ${fileNames.joinToString()}"

}

class UnableToReadJarFile : PluginProblem() {

  override val level = PluginProblem.Level.ERROR

  override val message = "Invalid jar file"

}

class PluginLibDirectoryIsEmpty : PluginProblem() {

  override val level = PluginProblem.Level.ERROR

  override val message = "Directory 'lib' must not be empty"

}