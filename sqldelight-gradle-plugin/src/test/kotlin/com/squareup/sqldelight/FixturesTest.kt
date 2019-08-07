/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sqldelight

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

@RunWith(Parameterized::class)
class FixturesTest(val fixtureRoot: File, val name: String) {
  @Suppress("unused") // Used by JUnit reflectively.
  @get:Rule val buildFilesRule = BuildFilesRule(fixtureRoot)

  @Test fun execute() {
    val androidHome = androidHome()
    File(fixtureRoot, "local.properties").writeText("sdk.dir=$androidHome\n")

    val runner = GradleRunner.create()
        .withProjectDir(fixtureRoot)
        .withPluginClasspath()
        .withArguments("clean", "compileReleaseSources", "--stacktrace", "-Dsqldelight.skip.runtime=true")

    if (File(fixtureRoot, "ignored.txt").exists()) {
      println("Skipping ignored test $name.")
      return
    }

    val expectedFailure = File(fixtureRoot, "failure.txt")
    if (expectedFailure.exists()) {
      val result = runner.buildAndFail()
      for (chunk in expectedFailure.readText().split("\n\n")) {
        assertThat(result.output).contains(chunk)
      }
    } else {
      val result = runner.build()
      assertThat(result.output).contains("BUILD SUCCESSFUL")

      val expectedDir = File(fixtureRoot, "expected/").toPath()
      val outputDir = File(fixtureRoot, "build/generated/source/sqldelight/").toPath()
      Files.walkFileTree(expectedDir, object : SimpleFileVisitor<Path>() {
        override fun visitFile(expectedFile: Path, attrs: BasicFileAttributes): FileVisitResult {
          val relative = expectedDir.relativize(expectedFile).toString()
          val actualFile = outputDir.resolve(relative)
          if (!Files.exists(actualFile)) {
            throw AssertionError("Expected file not found: $actualFile")
          }

          val expected = String(Files.readAllBytes(expectedFile), StandardCharsets.UTF_8)
          val actual = String(Files.readAllBytes(actualFile), StandardCharsets.UTF_8)
          assertThat(actual).named(relative).isEqualTo(expected)

          return CONTINUE
        }
      })
    }
  }

  companion object {
    @Suppress("unused") // Used by Parameterized JUnit runner reflectively.
    @Parameters(name = "{1}")
    @JvmStatic fun parameters() = File("src/test/fixtures").listFiles()
        .filter { it.isDirectory }
        .map { arrayOf(it, it.name) }
  }
}
