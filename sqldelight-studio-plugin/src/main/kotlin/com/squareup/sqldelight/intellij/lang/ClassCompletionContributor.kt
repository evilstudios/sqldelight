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

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtil.findReferenceOrAlphanumericPrefix
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.JavaClassNameCompletionContributor
import com.intellij.codeInsight.completion.JavaPsiClassReferenceElement
import com.intellij.psi.util.PsiTreeUtil
import com.squareup.sqldelight.intellij.psi.ClassNameElement
import com.squareup.sqldelight.intellij.psi.ImportElement
import com.squareup.sqldelight.intellij.util.parentOfType

class ClassCompletionContributor : JavaClassNameCompletionContributor() {
  private val insertHandler = AutoImportInsertionHandler()

  override fun fillCompletionVariants(parameters: CompletionParameters,
      resultSet: CompletionResultSet) {
    if (parameters.position.parentOfType<ClassNameElement>() != null) {
      val result = resultSet.withPrefixMatcher(findReferenceOrAlphanumericPrefix(parameters))
      JavaClassNameCompletionContributor.addAllClasses(parameters, parameters.invocationCount <= 1,
          result.prefixMatcher, { lookupElement ->
        if (lookupElement is JavaPsiClassReferenceElement) {
          lookupElement.setInsertHandler(insertHandler)
        }
        resultSet.addElement(lookupElement)
      })
    }
  }
}

class AutoImportInsertionHandler : InsertHandler<JavaPsiClassReferenceElement> {
  override fun handleInsert(context: InsertionContext, item: JavaPsiClassReferenceElement) {
    val qname = item.qualifiedName
    val imports = PsiTreeUtil.findChildrenOfType(context.file, ImportElement::class.java)
    val ref = imports.map { PsiTreeUtil.findChildOfType(it, ClassNameElement::class.java) }
        .filterNotNull()
        .find { it.text == qname }
    val refEnd = context.trackOffset(context.tailOffset, false)

    if (ref == null) {
      if (imports.isEmpty()) {
        context.document.insertString(0, "import $qname;\n\n")
      } else {
        context.insertAndOrganizeImports(imports, "import $qname;")
      }
    }

    context.tailOffset = context.getOffset(refEnd)

    context.commitDocument()
  }
}

private fun InsertionContext.insertAndOrganizeImports(imports: Collection<ImportElement>, newImport: String) {
  val newImports = arrayListOf(newImport)
  var endOffset = 0
  for (importElement in imports) {
    newImports.add(importElement.text)
    endOffset = Math.max(endOffset, importElement.textOffset + importElement.textLength)
  }
  document.replaceString(0, endOffset, newImports.sorted().joinToString("\n"))
}
