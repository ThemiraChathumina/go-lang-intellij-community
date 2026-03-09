// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoIfStmt extends PsiElement {

  @NotNull
  List<GoBlock> getBlockList();

  @Nullable
  GoExpressionNoStruct getExpressionNoStruct();

  @Nullable
  GoIfStmt getIfStmt();

  @Nullable
  GoSimpleStmtNoStruct getSimpleStmtNoStruct();

}
