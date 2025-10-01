package com.anttree.vft.processors.transformer;

import com.anttree.vft.processors.Constants;
import com.anttree.vft.processors.annotations.Unused;
import com.anttree.vft.processors.annotations.VisibleForTesting;
import com.anttree.vft.processors.models.Pair;
import com.anttree.vft.processors.utils.Log;
import com.anttree.vft.processors.utils.NodeUtils;
import com.anttree.vft.processors.utils.Utils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class AsmVftTransformer implements Transformer {

    private static final String ANN_VFT_DESC = Utils.getAnnotationDesc("VisibleForTesting");
    private static final String ANN_UNUSED_DESC = Utils.getAnnotationDesc("Unused");

    private final String buildTypeLower;

    public AsmVftTransformer(String buildType) {
        this.buildTypeLower = buildType == null ? "" : buildType.toLowerCase();
    }

    @Override
    public byte[] transform(byte[] inBytes) {
        if (inBytes == null || inBytes.length == 0) {
            return inBytes;
        }

        ClassNode classNode = NodeUtils.toClassNode(inBytes);

        boolean changed = false;

        Attribute classAttribute = readAttribute(
                classNode.invisibleAnnotations
        );
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
            Attribute fieldAttribute = readAttribute(
                    fieldNode.invisibleAnnotations
            );
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
            Attribute methodAttribute = readAttribute(
                    methodNode.invisibleAnnotations
            );
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

    private static Attribute readAttribute(List<AnnotationNode> annotations) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotation : annotations) {
            if (ANN_VFT_DESC.equals(annotation.desc)) {
                return parseVftAnnotation(annotation);
            }
            if (ANN_UNUSED_DESC.equals(annotation.desc)) {
                return parseUnusedAnnotation(annotation);
            }
        }
        return null;
    }

    private static Attribute parseVftAnnotation(AnnotationNode ann) {
        String scope    = Constants.SCOPE_NONE;
        String flavor   = Constants.FLAVOR_RELEASE;
        if (ann.values == null) {
            return new Attribute(scope, flavor);
        }

        for (Pair<String, Object> pair : NodeUtils.annotationValues(ann)) {
            String name = pair.getKey();
            Object value = pair.getValue();
            if (Constants.SCOPE.equals(name) && value instanceof String[]) {
                scope = ((String[]) value)[1];
            } else if (Constants.FLAVOR.equals(name) && value instanceof String) {
                flavor = (String) value;
            }
        }
        return new Attribute(scope, flavor);
    }

    private static Attribute parseUnusedAnnotation(AnnotationNode ann) {
        String scope    = Constants.SCOPE_NONE;
        String flavor   = Constants.FLAVOR_RELEASE;
        if (ann.values == null) {
            return new Attribute(scope, flavor);
        }

        for (Pair<String, Object> pair : NodeUtils.annotationValues(ann)) {
            String name = pair.getKey();
            Object value = pair.getValue();

            if (Constants.FLAVOR.equals(name) && value instanceof String) {
                flavor = (String) value;
            }
        }
        return new Attribute(scope, flavor);
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

    private static final class Attribute {
        final String scope;
        final String flavor;
        Attribute(String scope, String flavor) { this.scope = scope; this.flavor = flavor; }
    }
}
