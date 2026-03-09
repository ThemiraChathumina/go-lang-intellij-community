// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoSimpleStmt extends PsiElement {

  @Nullable
  GoAssignment getAssignment();

  @Nullable
  GoExpressionStmt getExpressionStmt();

  @Nullable
  GoIncDecStmt getIncDecStmt();

  @Nullable
  GoSendStmt getSendStmt();

  @Nullable
  GoShortVarDecl getShortVarDecl();

}
