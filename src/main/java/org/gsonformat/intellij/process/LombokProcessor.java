package org.gsonformat.intellij.process;

import com.intellij.psi.*;
import org.apache.http.util.TextUtils;
import org.gsonformat.intellij.config.Config;
import org.gsonformat.intellij.entity.ClassEntity;
import org.gsonformat.intellij.entity.FieldEntity;

import java.util.regex.Pattern;

/**
 * Created by dim on 16/11/7.
 */
public class LombokProcessor extends Processor {
    @Override
    protected void onStarProcess(ClassEntity classEntity, PsiElementFactory factory, PsiClass cls, IProcessor visitor) {
        super.onStarProcess(classEntity, factory, cls, visitor);
        injectAnnotation(factory, cls);
    }

    private void injectAnnotation(PsiElementFactory factory, PsiClass generateClass) {
        if (factory == null || generateClass == null) {
            return;
        }
        PsiModifierList modifierList = generateClass.getModifierList();
        PsiElement firstChild = modifierList.getFirstChild();
        Pattern pattern = Pattern.compile("@.*?NoArgsConstructor");
        if (firstChild != null && !pattern.matcher(firstChild.getText()).find()) {
            PsiAnnotation annotationFromText = factory.createAnnotationFromText("@lombok.NoArgsConstructor", generateClass);
            modifierList.addBefore(annotationFromText, firstChild);
        }
        Pattern pattern2 = Pattern.compile("@.*?Data");
        if (firstChild != null && !pattern2.matcher(firstChild.getText()).find()) {
            PsiAnnotation annotationFromText = factory.createAnnotationFromText("@lombok.Data", generateClass);
            modifierList.addBefore(annotationFromText, firstChild);
        }
    }

    @Override
    protected void generateField(PsiElementFactory factory, FieldEntity fieldEntity, PsiClass cls, ClassEntity classEntity) {
        if (fieldEntity.isGenerate()) {

            StringBuilder fieldSb = new StringBuilder();
            String filedName = fieldEntity.getGenerateFieldName();
            if (!TextUtils.isEmpty(classEntity.getExtra())) {
                fieldSb.append(classEntity.getExtra()).append("\n");
                classEntity.setExtra(null);
            }
            if (fieldEntity.getTargetClass() != null) {
                fieldEntity.getTargetClass().setGenerate(true);
            }

            if (Config.getInstant().isFieldPrivateMode()) {
                fieldSb.append("private  ").append(fieldEntity.getFullNameType()).append(" ").append(filedName).append(" ; ");
            } else {
                fieldSb.append("public  ").append(fieldEntity.getFullNameType()).append(" ").append(filedName).append(" ; ");
            }
            cls.add(factory.createFieldFromText(fieldSb.toString(), cls));
        }
    }

    @Override
    protected void createGetAndSetMethod(PsiElementFactory factory, PsiClass cls, FieldEntity field) {
    }

    @Override
    protected void onEndGenerateClass(PsiElementFactory factory, ClassEntity classEntity, PsiClass parentClass, PsiClass generateClass, IProcessor visitor) {
        super.onEndGenerateClass(factory, classEntity, parentClass, generateClass, visitor);
        injectAnnotation(factory, generateClass);
    }
}
