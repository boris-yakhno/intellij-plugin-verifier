
val platformVersion = "213.7172.25"

dependencies {
  api(project(":structure-intellij"))

  implementation("com.jetbrains.intellij.platform:util:$platformVersion")
  implementation("com.jetbrains.intellij.platform:jps-model:$platformVersion")
  implementation("com.jetbrains.intellij.platform:jps-model-impl:$platformVersion")
  implementation("com.jetbrains.intellij.platform:jps-model-serialization:$platformVersion")
}