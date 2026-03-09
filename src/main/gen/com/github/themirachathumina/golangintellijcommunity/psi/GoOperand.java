// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoOperand extends PsiElement {

  @Nullable
  GoExpression getExpression();

  @Nullable
  GoLiteral getLiteral();

  @Nullable
  GoOperandName getOperandName();

  @Nullable
  GoTypeArgs getTypeArgs();

}
