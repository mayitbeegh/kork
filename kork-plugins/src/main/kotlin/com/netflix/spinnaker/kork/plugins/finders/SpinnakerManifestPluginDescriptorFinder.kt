/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.kork.plugins.finders

import com.netflix.spinnaker.kork.plugins.SpinnakerPluginDescriptor
import org.pf4j.ManifestPluginDescriptorFinder
import org.pf4j.PluginDescriptor
import java.util.jar.Manifest

/**
 * Decorates the default [PluginDescriptor] created from [ManifestPluginDescriptorFinder]
 * with Spinnaker-specific descriptor metadata.
 */
internal class SpinnakerManifestPluginDescriptorFinder : ManifestPluginDescriptorFinder() {
  override fun createPluginDescriptor(manifest: Manifest): PluginDescriptor =
    SpinnakerPluginDescriptor(
      super.createPluginDescriptor(manifest),
      manifest.mainAttributes.getValue(PLUGIN_NAMESPACE) ?: "undefined",
      manifest.mainAttributes.getValue(PLUGIN_UNSAFE)?.toBoolean() ?: false
    )

  companion object {
    const val PLUGIN_NAMESPACE = "Plugin-Namespace"
    const val PLUGIN_UNSAFE = "Plugin-Unsafe"
  }
}
