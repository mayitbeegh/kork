/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.kork.plugins.config

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import io.mockk.every
import io.mockk.mockk
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.MutablePropertySources
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class SpringEnvironmentExtensionConfigResolverTest : JUnit5Minutests {

  fun tests() = rootContext<Fixture> {
    fixture {
      Fixture()
    }

    test("plugin extension with shortened config path") {
      every { environment.getProperty(any(), eq(TestExtensionConfig::class.java)) } returns TestExtensionConfig()

      expectThat(subject.resolve(
        PluginConfigCoordinates("netflix", "sweet-plugin", "netflix", "foo"),
        TestExtensionConfig::class.java
      ))
        .isA<TestExtensionConfig>()
        .and {
          get { somestring }.isEqualTo("overridden default")
          get { someint }.isEqualTo(10)
          get { optional }.isNull()
          get { somelist }.hasSize(1)
            .get { first().hello }.isEqualTo("Future Rob")
        }
    }

    test("plugin extension with expanded path") {
      every { environment.getProperty(any(), eq(TestExtensionConfig::class.java)) } returns TestExtensionConfig()

      expectThat(subject.resolve(
        PluginConfigCoordinates("netflix", "very-important", "orca", "stage"),
        TestExtensionConfig::class.java
      ))
        .isA<TestExtensionConfig>()
        .and {
          get { somestring }.isEqualTo("default")
          get { someint }.isEqualTo(15)
          get { optional }.isEqualTo("some new value")
          get { somelist }.isEmpty()
        }
    }

    test("system extension config path") {
      every { environment.getProperty(any(), eq(TestExtensionConfig::class.java)) } returns TestExtensionConfig()

      expectThat(subject.resolve(
        SystemExtensionConfigCoordinates("netflix", "bar"),
        TestExtensionConfig::class.java
      ))
        .isA<TestExtensionConfig>()
        .and {
          get { somestring }.isEqualTo("default")
          get { someint }.isEqualTo(15)
          get { optional }.isNull()
          get { somelist }.hasSize(2)
            .and {
              get { get(0).hello }.isEqualTo("one")
              get { get(1).hello }.isEqualTo("two")
            }
        }
    }
  }

  private inner class Fixture {
    val environment: ConfigurableEnvironment = mockk(relaxed = true)
    val subject = SpringEnvironmentExtensionConfigResolver(environment)

    init {
      every { environment.propertySources } returns MutablePropertySources().apply {
        addFirst(MapPropertySource("test", properties))
      }
    }
  }

  internal class TestExtensionConfig(
    var somestring: String = "default",
    var someint: Int = 15,
    var optional: String? = null,
    var somelist: List<NestedConfig> = listOf()
  )

  internal class NestedConfig(
    var hello: String
  )

  private val properties = mapOf<String, Any?>(
    "spinnaker.plugins.netflix.sweet-plugin.enabled" to "true",
    "spinnaker.plugins.netflix.sweet-plugin.extensions.netflix.foo.config.somestring" to "overridden default",
    "spinnaker.plugins.netflix.sweet-plugin.extensions.netflix.foo.config.someint" to 10,
    "spinnaker.plugins.netflix.sweet-plugin.extensions.netflix.foo.config.somelist[0].hello" to "Future Rob",
    "spinnaker.plugins.netflix.very-important.extensions.orca.stage.config.optional" to "some new value",
    "spinnaker.extensions.netflix.bar.config.somelist[0].hello" to "one",
    "spinnaker.extensions.netflix.bar.config.somelist[1].hello" to "two"
  )
}
