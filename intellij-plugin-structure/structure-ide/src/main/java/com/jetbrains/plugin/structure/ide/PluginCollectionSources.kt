/*
 * Copyright 2000-2025 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.jetbrains.plugin.structure.ide

import com.jetbrains.plugin.structure.ide.layout.LayoutComponents
import com.jetbrains.plugin.structure.intellij.platform.ProductInfo
import com.jetbrains.plugin.structure.intellij.version.IdeVersion
import java.nio.file.Path

interface PluginCollectionSource<S, R> {
  val source: S
  val resource: R
}

data class ProductInfoPluginCollectionSource(val idePath: Path, val ideVersion: IdeVersion, val productInfo: ProductInfo): PluginCollectionSource<Path, ProductInfo> {
  override val source: Path = idePath

  override val resource: ProductInfo = productInfo
}

data class ProductInfoLayoutComponentsPluginCollectionSource(val idePath: Path, val ideVersion: IdeVersion, val layoutComponents: LayoutComponents): PluginCollectionSource<Path, LayoutComponents> {
  override val source: Path = idePath

  override val resource: LayoutComponents = layoutComponents
}
