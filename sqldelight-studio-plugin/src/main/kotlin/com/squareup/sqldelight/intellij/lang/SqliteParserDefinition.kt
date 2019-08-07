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
package com.squareup.sqldelight.intellij.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.squareup.sqldelight.SqliteLexer
import com.squareup.sqldelight.intellij.util.moduleDirectory
import org.antlr.intellij.adaptor.lexer.ElementTypeFactory
import org.antlr.intellij.adaptor.lexer.SimpleAntlrAdapter

class SqliteParserDefinition : ParserDefinition {
  private val file = IFileElementType(SqliteLanguage.INSTANCE)

  override fun createLexer(project: Project) =
      SimpleAntlrAdapter(SqliteLanguage.INSTANCE, SqliteLexer(null))

  override fun createParser(project: Project) = SqliteParser()

  override fun getWhitespaceTokens() = ElementTypeFactory.createTokenSet(SqliteLanguage.INSTANCE,
      listOf(*SqliteLexer.tokenNames), SqliteLexer.SPACES)

  override fun getCommentTokens() = ElementTypeFactory.createTokenSet(SqliteLanguage.INSTANCE,
      listOf(*SqliteLexer.tokenNames), SqliteLexer.SINGLE_LINE_COMMENT,
      SqliteLexer.MULTILINE_COMMENT)

  override fun getStringLiteralElements() = ElementTypeFactory.createTokenSet(
      SqliteLanguage.INSTANCE, listOf(*SqliteLexer.tokenNames), SqliteLexer.SINGLE_LINE_COMMENT,
      SqliteLexer.STRING_LITERAL)

  override fun getFileNodeType() = file

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    val moduleDirectory = viewProvider.virtualFile.moduleDirectory()
    if (moduleDirectory != null) {
      return SqliteFile(viewProvider, moduleDirectory)
    }
    return object : PsiFileBase(viewProvider, SqliteLanguage.INSTANCE) {
      override fun getFileType() = SqliteFileType.INSTANCE
    }
  }

  override fun createElement(node: ASTNode) = node.asPSINode()

  override fun spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode) =
      ParserDefinition.SpaceRequirements.MAY
}
