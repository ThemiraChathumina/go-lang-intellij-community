// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoRelationalExpr extends PsiElement {

  @NotNull
  List<GoAddExpr> getAddExprList();

  @NotNull
  List<GoRelOp> getRelOpList();

}
