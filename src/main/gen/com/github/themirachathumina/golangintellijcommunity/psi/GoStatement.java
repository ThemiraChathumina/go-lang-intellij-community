// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoStatement extends PsiElement {

  @Nullable
  GoBlock getBlock();

  @Nullable
  GoBreakStmt getBreakStmt();

  @Nullable
  GoContinueStmt getContinueStmt();

  @Nullable
  GoDeclaration getDeclaration();

  @Nullable
  GoDeferStmt getDeferStmt();

  @Nullable
  GoFallthroughStmt getFallthroughStmt();

  @Nullable
  GoForStmt getForStmt();

  @Nullable
  GoGoStmt getGoStmt();

  @Nullable
  GoGotoStmt getGotoStmt();

  @Nullable
  GoIfStmt getIfStmt();

  @Nullable
  GoLabeledStmt getLabeledStmt();

  @Nullable
  GoReturnStmt getReturnStmt();

  @Nullable
  GoSelectStmt getSelectStmt();

  @Nullable
  GoSimpleStmt getSimpleStmt();

  @Nullable
  GoSwitchStmt getSwitchStmt();

}
