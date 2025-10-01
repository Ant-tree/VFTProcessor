package com.anttree.vft.processors.utils;

import com.anttree.vft.processors.models.Pair;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

public class NodeUtils {

    public static ClassNode toClassNode(byte[] classBytes) {
        if (classBytes == null || classBytes.length == 0) {
            return null;
        }
        ClassNode classNode = new ClassNode();
        try {
            org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(classBytes);
            cr.accept(classNode, 0);
            return classNode;
        } catch (Exception e) {
            Log.error("Failed to convert to ClassNode", e);
            return null;
        }
    }

    public static List<Pair<String, Object>> annotationValues(AnnotationNode annotation) {
        List<Pair<String, Object>> result = new ArrayList<>();
        if (annotation.values == null) {
            return result;
        }

        // values = [name1, value1, name2, value2, ...]
        for (int i = 0; i < annotation.values.size(); i += 2) {
            String name = (String) annotation.values.get(i);
            Object value = annotation.values.get(i + 1);
            result.add(new Pair<>(name, value));
        }
        return result;
    }
}
