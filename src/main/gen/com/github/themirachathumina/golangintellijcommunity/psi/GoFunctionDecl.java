// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoFunctionDecl extends PsiElement {

  @Nullable
  GoFunctionBody getFunctionBody();

  @NotNull
  GoFunctionName getFunctionName();

  @Nullable
  GoSignature getSignature();

  @Nullable
  GoTypeParameters getTypeParameters();

}
