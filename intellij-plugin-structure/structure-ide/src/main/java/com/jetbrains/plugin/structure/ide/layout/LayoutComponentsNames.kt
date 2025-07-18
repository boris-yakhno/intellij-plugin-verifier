/*
 * Copyright 2000-2025 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.jetbrains.plugin.structure.ide.layout

/**
 * Provide layout component names from type-safe layout component names in [LayoutComponents].
 */
class LayoutComponentsNames(private val layoutComponents: LayoutComponents) :
  LayoutComponentNameSource<LayoutComponents> {

  override fun getNames(): List<String> {
    return layoutComponents.layoutComponents.map { it.name }
  }
}