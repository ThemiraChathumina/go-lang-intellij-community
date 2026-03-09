// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoMethodDecl extends PsiElement {

  @Nullable
  GoFunctionBody getFunctionBody();

  @Nullable
  GoMethodName getMethodName();

  @NotNull
  GoReceiver getReceiver();

  @Nullable
  GoSignature getSignature();

}
