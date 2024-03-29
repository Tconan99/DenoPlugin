package com.conan.psi;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationParameterList;
import com.intellij.psi.PsiNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Plushnikov Michail
 */
public class LombokProcessorUtil {

  @Nullable
  public static String getMethodVisibity(@NotNull PsiAnnotation psiAnnotation) {
    return convertAcessLevelToJavaString(findAnnotationPropertyValue(psiAnnotation, "value", true));
  }

  @Nullable
  public static String getAccessVisibity(@NotNull PsiAnnotation psiAnnotation) {
    return convertAcessLevelToJavaString(findAnnotationPropertyValue(psiAnnotation, "access", false));
  }

  public static String findAnnotationPropertyValue(@NotNull PsiAnnotation psiAnnotation, String propertyName, boolean isDefaultProperty) {
    PsiAnnotationParameterList annotationParameterList = psiAnnotation.getParameterList();
    String value = "";
    for (PsiNameValuePair pair : annotationParameterList.getAttributes()) {
      if (propertyName.equals(pair.getName()) || (isDefaultProperty && null == pair.getName())) {
        value = pair.getText();
        break;
      }
    }
    return value;
  }

  @Nullable
  public static String convertAcessLevelToJavaString(String value) {
    if (null == value || value.isEmpty() || value.endsWith("AccessLevel.PUBLIC"))
      return "public ";
    if (value.endsWith("AccessLevel.MODULE"))
      return "";
    if (value.endsWith("AccessLevel.PROTECTED"))
      return "protected ";
    if (value.endsWith("AccessLevel.PACKAGE"))
      return "";
    if (value.endsWith("AccessLevel.PRIVATE"))
      return "private ";
    if (value.endsWith("AccessLevel.NONE"))
      return null;
    else
      return null;
  }
}
