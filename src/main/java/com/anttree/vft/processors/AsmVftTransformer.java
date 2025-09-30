package com.anttree.vft.processors;

import com.anttree.vft.processors.annotations.VisibleForTesting;
import com.anttree.vft.processors.utils.Log;
import com.anttree.vft.processors.utils.Utils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class AsmVftTransformer {

    private static final String ANN_DESC = Utils.getDescriptor(VisibleForTesting.class);

    private final String buildTypeLower;

    public AsmVftTransformer(String buildType) {
        this.buildTypeLower = buildType == null ? "" : buildType.toLowerCase();
    }

    public byte[] transform(byte[] inBytes) {
        ClassReader cr = new ClassReader(inBytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0); // 전체 트리 로드

        boolean changed = false;

        VisibleForTestingAttrs classAttribute = readVft(classNode.visibleAnnotations);
        if (classAttribute != null && matchesBuildType(classAttribute.flavor)) {
            int newAcc = applyScope(classNode.access, classAttribute.scope);
            if (newAcc != classNode.access) {
                if (newAcc == -1) {
                    return null;
                }
                classNode.access = newAcc;
                changed = true;
                Log.descriptor("[ASM-VFT]", "Class"
                        , classNode.name
                        , "-> " + classAttribute.scope);
            }
        }


        List<FieldNode> fields = new ArrayList<>();
        for (FieldNode fieldNode : classNode.fields) {
            VisibleForTestingAttrs fieldAttribute = readVft(fieldNode.visibleAnnotations);
            if (fieldAttribute != null && matchesBuildType(fieldAttribute.flavor)) {
                int newAcc = applyScope(fieldNode.access, fieldAttribute.scope);
                if (newAcc == fieldNode.access) {
                    fields.add(fieldNode);
                    continue;
                }
                if (newAcc != -1) {
                    fieldNode.access = newAcc;
                    fields.add(fieldNode);
                }
                changed = true;
                Log.descriptor("[ASM-VFT]", "Field"
                        , classNode.name
                        , fieldNode.name
                        , "-> " + fieldAttribute.scope);
            } else {
                fields.add(fieldNode);
            }
        }

        List<MethodNode> methods = new ArrayList<>();
        for (MethodNode methodNode : classNode.methods) {
            VisibleForTestingAttrs methodAttribute = readVft(methodNode.visibleAnnotations);
            if (methodAttribute != null && matchesBuildType(methodAttribute.flavor)) {
                int newAcc = applyScope(methodNode.access, methodAttribute.scope);
                if (newAcc == methodNode.access) {
                    methods.add(methodNode);
                    continue;
                }
                if (newAcc != -1) {
                    methodNode.access = newAcc;
                    methods.add(methodNode);
                }
                changed = true;
                Log.descriptor("[ASM-VFT]", "Method"
                        , classNode.name
                        , methodNode.name + methodNode.desc
                        , "-> " + methodAttribute.scope);
            } else {
                methods.add(methodNode);
            }
        }

        if (!changed) {
            return inBytes;
        }

        classNode.fields = fields;
        classNode.methods = methods;

        // No recalculation of stack frames or maxs needed as we only change access flags
        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private static VisibleForTestingAttrs readVft(List<AnnotationNode> annotations) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotation : annotations) {
            if (!ANN_DESC.equals(annotation.desc)) continue;
            String scope = null;
            String flavor = null;
            if (annotation.values == null) {
                continue;
            }
            // values = [name1, value1, name2, value2, ...]
            for (int index = 0; index < annotation.values.size(); index += 2) {
                String name = (String) annotation.values.get(index);
                Object value  = annotation.values.get(index + 1);
                if (Constants.SCOPE.equals(name)) {
                    // enum = String[]{desc, value}
                    if (value instanceof String[]) {
                        scope = ((String[]) value)[1]; // enum 상수명
                    } else {
                        throw new RuntimeException("Invalid VisibleForTesting.scope value: " + value);
                    }
                } else if (Constants.FLAVOR.equals(name)) {
                    if (value instanceof String) {
                        flavor = (String) value;
                    }
                }
            }
            return new VisibleForTestingAttrs(scope, flavor);
        }
        return null;
    }

    private boolean matchesBuildType(String annotationFlavor) {
        if (buildTypeLower.isEmpty() || annotationFlavor == null) {
            return false;
        }
        for (String flavor : annotationFlavor.toLowerCase()
                .replace(" ", "")
                .split(",")
        ) {
            if (buildTypeLower.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    private static int applyScope(int access, String scope) {
        access &= ~(ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE);
        if (scope == null) {
            return access;
        }
        switch (scope) {
            case "PUBLIC":          return access | ACC_PUBLIC;
            case "PROTECTED":       return access | ACC_PROTECTED;
            case "PRIVATE":         return access | ACC_PRIVATE;
            case "PACKAGE_PRIVATE": return access; // no bit
            case "NONE":            return -1;
            default:                return access;
        }
    }

    private static final class VisibleForTestingAttrs {
        final String scope;
        final String flavor;
        VisibleForTestingAttrs(String scope, String flavor) { this.scope = scope; this.flavor = flavor; }
    }
}
 