package com.jetbrains.plugin.structure.intellij.problems

import com.jetbrains.plugin.structure.base.plugin.PluginProblem
import com.jetbrains.plugin.structure.base.problems.InvalidDescriptorProblem
import com.jetbrains.plugin.structure.intellij.version.IdeVersion

data class PropertyWithDefaultValue(val descriptorPath: String, val propertyName: String) :
    InvalidDescriptorProblem(descriptorPath, "<$propertyName> has default value") {
  override val level = PluginProblem.Level.ERROR
}

data class InvalidDependencyBean(val descriptorPath: String) :
    InvalidDescriptorProblem(descriptorPath, "dependency id is not specified") {
  override val level = PluginProblem.Level.ERROR
}

data class InvalidModuleBean(val descriptorPath: String) :
    InvalidDescriptorProblem(descriptorPath, "module is empty. It must be specified as <module value=\"my.module\"/>") {
  override val level = PluginProblem.Level.ERROR
}

data class SinceBuildNotSpecified(val descriptorPath: String) :
    InvalidDescriptorProblem(descriptorPath, "since build is not specified") {
  override val level = PluginProblem.Level.ERROR
}

data class InvalidSinceBuild(val descriptorPath: String, val sinceBuild: String) :
    InvalidDescriptorProblem(descriptorPath, "invalid since build: $sinceBuild") {
  override val level = PluginProblem.Level.ERROR
}

data class InvalidUntilBuild(val descriptorPath: String, val untilBuild: String) :
    InvalidDescriptorProblem(descriptorPath, "invalid until build: $untilBuild") {
  override val level = PluginProblem.Level.ERROR
}

data class SinceBuildGreaterThanUntilBuild(val descriptorPath: String, val sinceBuild: IdeVersion, val untilBuild: IdeVersion) :
    InvalidDescriptorProblem(descriptorPath, "since build $sinceBuild is greater than until build $untilBuild") {
  override val level = PluginProblem.Level.ERROR
}

//todo: provide unresolved <x-include> names
data class UnresolvedXIncludeElements(val descriptorPath: String) : InvalidDescriptorProblem(descriptorPath, "unresolved xinclude elements") {
  override val level = PluginProblem.Level.ERROR
}

data class TooLongPropertyValue(val descriptorPath: String,
                                val propertyName: String,
                                val propertyValueLength: Int,
                                val maxLength: Int) : InvalidDescriptorProblem(descriptorPath, "value of property '$propertyName' is too long. Its length is $propertyValueLength which is more than maximum $maxLength characters long") {
  override val level = PluginProblem.Level.ERROR
}