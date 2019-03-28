package com.jetbrains.pluginverifier.analysis

import com.jetbrains.pluginverifier.ResultHolder
import com.jetbrains.pluginverifier.results.problems.ClassNotFoundProblem
import com.jetbrains.pluginverifier.results.problems.PackageNotFoundProblem
import com.jetbrains.pluginverifier.verifiers.VerificationContext
import com.jetbrains.pluginverifier.verifiers.resolution.ClassResolver

/**
 * Returns the top-most package of the given [className] that is not available in this [ClassResolver].
 *
 * If all packages of the specified class exist, `null` is returned.
 * If the class has default (empty) package, and that default package
 * is not available, then "" is returned.
 */
private fun ClassResolver.getTopMostMissingPackage(className: String): String? {
  if ('/' !in className) {
    return if (packageExists("")) {
      null
    } else {
      ""
    }
  }
  val packageParts = className.substringBeforeLast('/', "").split('/')
  var superPackage = ""
  for (packagePart in packageParts) {
    if (superPackage.isNotEmpty()) {
      superPackage += '/'
    }
    superPackage += packagePart
    if (!packageExists(superPackage)) {
      return superPackage
    }
  }
  return null
}

/**
 * Post-processes the verification result and groups
 * many [ClassNotFoundProblem]s into [PackageNotFoundProblem]s,
 * to make the report easier to understand.
 */
fun VerificationContext.analyzeMissingClasses(resultHolder: ResultHolder) {
  val classNotFoundProblems = resultHolder.compatibilityProblems.filterIsInstance<ClassNotFoundProblem>()

  /**
   * All [ClassNotFoundProblem]s will be split into 2 parts:
   * 1) Independent [ClassNotFoundProblem]s for classes
   * that originate from found packages.
   * These classes seem to be removed, causing API breakages.
   *
   * 2) Grouped [PackageNotFoundProblem]s for several [ClassNotFoundProblem]s
   * for packages that are not found.
   * These missing packages might have been removed,
   * or the Verifier is not properly configured to find them.
   */
  val noClassProblems = hashSetOf<ClassNotFoundProblem>()
  val packageToMissingProblems = hashMapOf<String, MutableSet<ClassNotFoundProblem>>()

  for (classNotFoundProblem in classNotFoundProblems) {
    val className = classNotFoundProblem.unresolved.className
    val missingPackage = classResolver.getTopMostMissingPackage(className)
    if (missingPackage != null) {
      packageToMissingProblems
          .getOrPut(missingPackage) { hashSetOf() }
          .add(classNotFoundProblem)
    } else {
      noClassProblems.add(classNotFoundProblem)
    }
  }

  val packageNotFoundProblems = packageToMissingProblems.map { (packageName, missingClasses) ->
    PackageNotFoundProblem(packageName, missingClasses)
  }

  //Retain all individual [ClassNotFoundProblem]s.
  resultHolder.compatibilityProblems.retainAll {
    it !is ClassNotFoundProblem || it in noClassProblems
  }

  //Add grouped [PackageNotFoundProblem]s via [registerProblem]
  //to ignore the problems if needed.
  for (packageNotFoundProblem in packageNotFoundProblems) {
    registerProblem(packageNotFoundProblem)
  }

}