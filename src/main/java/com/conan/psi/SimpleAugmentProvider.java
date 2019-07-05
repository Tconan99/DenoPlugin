package com.conan.psi;

import com.conan.ConanData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleAugmentProvider extends PsiAugmentProvider {
    @NotNull
    @Override
    protected <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {

        if (!(element instanceof PsiClass) || !element.isValid() || !element.isPhysical()) {
            return Collections.emptyList();
        }

        final List<Psi> result = new ArrayList<>();
        final PsiClass psiClass = (PsiClass) element;
        // System.out.println("Called for class: " + psiClass.getQualifiedName() + " type: " + type.getName());

        if (type.isAssignableFrom(PsiMethod.class)) {
            // System.out.println("collect methods of class: " + psiClass.getQualifiedName());
            processPsiClassAnnotations(result, psiClass, type);
            // processPsiClassFieldAnnotation(result, psiClass, type);
        }

        return result;
    }

    private <Psi extends PsiElement> void processPsiClassAnnotations(@NotNull List<Psi> result, @NotNull PsiClass psiClass, @NotNull Class<Psi> type) {
        // System.out.println("Processing class annotations BEGINN: " + psiClass.getQualifiedName());

        final PsiModifierList modifierList = psiClass.getModifierList();
        if (modifierList != null) {
            for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                processClassAnnotation(psiAnnotation, psiClass, result, type);
            }
        }
        // System.out.println("Processing class annotations END: " + psiClass.getQualifiedName());
    }

    private <Psi extends PsiElement> void processClassAnnotation(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull List<Psi> result, @NotNull Class<Psi> type) {
        final String annotationName = StringUtil.notNullize(psiAnnotation.getQualifiedName()).trim();
        final String supportedAnnotation = ConanData.class.getName();
        if ((supportedAnnotation.equals(annotationName) || supportedAnnotation.endsWith(annotationName)) &&
                (type.isAssignableFrom(PsiMethod.class))) {
            process(psiClass, psiAnnotation, result);
        }
    }

    private <Psi extends PsiElement> void process(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<Psi> target) {
        for (PsiField psiField : psiClass.getFields()) {
//            boolean createSetter = true;
//            PsiModifierList modifierList = psiField.getModifierList();
//            if (null != modifierList) {
//                createSetter = !modifierList.hasModifierProperty(PsiModifier.STATIC); // 处理static
//                createSetter &= !hasFieldProcessorAnnotation(modifierList); // 处理 属性也添加注解的情况防止重复执行，我猜的 ^-^
//            }

            process(psiField, psiAnnotation, target);
        }
    }

    public <Psi extends PsiElement> void process(@NotNull PsiField psiField, @NotNull PsiAnnotation psiAnnotation, @NotNull List<Psi> target) {
        boolean result = false;

        final String visibility = LombokProcessorUtil.getMethodVisibity(psiAnnotation);
        if (null != visibility) {
            try {
                Project project = psiField.getProject();

                PsiClass psiClass = psiField.getContainingClass();
                PsiManager manager = psiField.getContainingFile().getManager();
                PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();

                String fieldName = psiField.getName();
                PsiType psiType = psiField.getType();
                String typeName = psiType.getCanonicalText();
                // String getterName = TransformationsUtil.toGetterName(fieldName, psiType.equalsToText("boolean"));
                String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());

                final PsiMethod valuesMethod = elementFactory.createMethodFromText(
                        visibility + typeName + " " + getterName + "() { return this." + fieldName + ";}",
                        psiClass);
                target.add((Psi) new MyLightMethod(manager, valuesMethod, psiClass));
                // result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // psiField.putUserData(READ_KEY, result);
    }

//    private static final String LOMBOK_HAS_IMPLICIT_USAGE_PROPERTY = "lombok.hasImplicitUsage";
//    private static final String LOMBOK_HAS_IMPLICIT_READ_PROPERTY = "lombok.hasImplicitRead";
//    private static final String LOMBOK_HAS_IMPLICIT_WRITE_PROPERTY = "lombok.hasImplicitWrite";
//
//    public static final Key<Boolean> USAGE_KEY = Key.create(LOMBOK_HAS_IMPLICIT_USAGE_PROPERTY);
//    public static final Key<Boolean> READ_KEY = Key.create(LOMBOK_HAS_IMPLICIT_READ_PROPERTY);
//    public static final Key<Boolean> WRITE_KEY = Key.create(LOMBOK_HAS_IMPLICIT_WRITE_PROPERTY);

}
