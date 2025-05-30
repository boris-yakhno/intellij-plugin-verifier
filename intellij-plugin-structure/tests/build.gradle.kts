dependencies {
  testImplementation(project(":"))
  testImplementation(project(":structure-ide"))
  testImplementation(project(":structure-ide-classes"))
  testImplementation(project(":structure-intellij"))
  testImplementation(project(":structure-intellij-classes"))
  testImplementation(project(":structure-teamcity"))
  testImplementation(project(":structure-hub"))
  testImplementation(project(":structure-fleet"))
  testImplementation(project(":structure-dotnet"))
  testImplementation(project(":structure-edu"))
  testImplementation(project(":structure-toolbox"))
  testImplementation(project(":structure-youtrack"))
  testImplementation(project(":structure-teamcity-recipes"))
  testImplementation(sharedLibs.junit)
  testImplementation(sharedLibs.jackson.module.kotlin)
  testImplementation(libs.commons.compress)
  testImplementation(libs.jimfs)
  testImplementation(libs.jackson.yaml)
  testImplementation(libs.semver4j)
  testImplementation(sharedLibs.logback.core)
  testImplementation(sharedLibs.logback.classic)

  testImplementation(sharedLibs.byteBuddy)
}

val testOutput by configurations.creating {
  isCanBeConsumed = true
  isCanBeResolved = false
}

val testJar by tasks.registering(Jar::class) {
  archiveClassifier.set("tests")
  from(sourceSets["test"].output)
  dependsOn(tasks.testClasses)
}


artifacts {
  add("testOutput", testJar)
}
