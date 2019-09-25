package com.jetbrains.pluginverifier.usages.properties

import com.jetbrains.plugin.structure.classes.resolvers.ResolutionResult
import com.jetbrains.plugin.structure.classes.utils.getBundleBaseName
import com.jetbrains.pluginverifier.results.location.MethodLocation
import com.jetbrains.pluginverifier.results.problems.MissingPropertyReferenceProblem
import com.jetbrains.pluginverifier.results.reference.MethodReference
import com.jetbrains.pluginverifier.usages.ApiUsageProcessor
import com.jetbrains.pluginverifier.verifiers.*
import com.jetbrains.pluginverifier.verifiers.resolution.Method
import org.objectweb.asm.tree.AbstractInsnNode
import java.util.*

class PropertyUsageProcessor : ApiUsageProcessor {

  override fun processMethodInvocation(
      methodReference: MethodReference,
      resolvedMethod: Method,
      instructionNode: AbstractInsnNode,
      callerMethod: Method,
      context: VerificationContext
  ) {
    val methodParameters = resolvedMethod.methodParameters
    for ((parameterIndex, methodParameter) in methodParameters.withIndex()) {
      val propertyKeyAnnotation = methodParameter.annotations.findAnnotation("org/jetbrains/annotations/PropertyKey")
          ?: continue

      val resourceBundleName = propertyKeyAnnotation.getAnnotationValue("resourceBundle") as? String ?: continue

      val instructions = callerMethod.instructions
      val instructionIndex = instructions.indexOf(instructionNode)

      val frames = analyzeMethodFrames(callerMethod).toList()
      val frame = frames.getOrNull(instructionIndex) ?: continue

      val onStackIndex = methodParameters.size - 1 - parameterIndex
      val parameterSourceValue = frame.getOnStack(onStackIndex) ?: continue
      val propertyKey = evaluateConstantString(parameterSourceValue, context.classResolver, frames, instructions)

      if (propertyKey != null) {
        checkProperty(resourceBundleName, propertyKey, context, callerMethod.location)
      }
    }
  }

  private fun checkProperty(
      resourceBundleName: String,
      propertyKey: String,
      context: VerificationContext,
      usageLocation: MethodLocation
  ) {
    if (resourceBundleName != getBundleBaseName(resourceBundleName)) {
      //In general, we can't resolve non-base bundles, like "some.Bundle_en" because we don't know the locale to use.
      return
    }

    val bundleNameSet = context.classResolver.allBundleNameSet
    val fullBundleNames = bundleNameSet[resourceBundleName]
    if (fullBundleNames.isEmpty() || fullBundleNames.size > 1 || fullBundleNames.single() != resourceBundleName) {
      /*
      In general, we don't know the locale to use when there are multiple bundles, like "some.Bundle" and "some.Bundle_en".
      If we always use the Locale.ROOT, it may lead a false positive if a property is declared only in the "_en" bundle.
      So we don't try to check such properties.
       */
      return
    }

    val resolutionResult = context.classResolver.resolveExactPropertyResourceBundle(resourceBundleName, Locale.ROOT)
    if (resolutionResult !is ResolutionResult.Found) {
      return
    }

    val resourceBundle = resolutionResult.value
    if (propertyKey !in resourceBundle.keySet()) {
      context.problemRegistrar.registerProblem(MissingPropertyReferenceProblem(propertyKey, resourceBundleName, usageLocation))
    }
  }
}