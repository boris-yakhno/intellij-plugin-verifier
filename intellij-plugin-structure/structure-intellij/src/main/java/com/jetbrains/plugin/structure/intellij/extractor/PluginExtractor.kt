/*
 * Copyright 2000-2025 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.jetbrains.plugin.structure.intellij.extractor

import java.nio.file.Path

interface PluginExtractor {
  fun extractPlugin(pluginFile: Path, extractDirectory: Path): ExtractorResult
}