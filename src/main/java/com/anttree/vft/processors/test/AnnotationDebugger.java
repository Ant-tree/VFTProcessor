package com.anttree.vft.processors.test;

import com.anttree.vft.processors.annotations.VisibleForTesting;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;

public class AnnotationDebugger {

    public static void main(String[] args) throws IOException {
        // TestClass의 바이트코드를 읽어서 애노테이션 정보 출력
        String className = "com/anttree/vft/processors/test/TestClass";

        try (InputStream is = AnnotationDebugger.class.getClassLoader().getResourceAsStream(className + ".class")) {
            if (is == null) {
                System.out.println("Class file not found: " + className);
                return;
            }

            byte[] classBytes = is.readAllBytes();
            ClassReader cr = new ClassReader(classBytes);
            ClassNode classNode = new ClassNode();
            cr.accept(classNode, 0);

            System.out.println("=== Class: " + classNode.name + " ===");
            System.out.println("Visible annotations: " + classNode.visibleAnnotations);
            System.out.println("Invisible annotations: " + classNode.invisibleAnnotations);

            System.out.println("\n=== Fields ===");
            for (FieldNode field : classNode.fields) {
                System.out.println("Field: " + field.name);
                System.out.println("  Visible annotations: " + field.visibleAnnotations);
                System.out.println("  Invisible annotations: " + field.invisibleAnnotations);
                if (field.invisibleAnnotations != null) {
                    for (AnnotationNode ann : field.invisibleAnnotations) {
                        System.out.println("    Annotation desc: " + ann.desc);
                        System.out.println("    Values: " + ann.values);
                    }
                }
            }

            System.out.println("\n=== Methods ===");
            for (MethodNode method : classNode.methods) {
                if (!method.name.equals("<init>")) {
                    System.out.println("Method: " + method.name);
                    System.out.println("  Visible annotations: " + method.visibleAnnotations);
                    System.out.println("  Invisible annotations: " + method.invisibleAnnotations);
                    if (method.invisibleAnnotations != null) {
                        for (AnnotationNode ann : method.invisibleAnnotations) {
                            System.out.println("    Annotation desc: " + ann.desc);
                            System.out.println("    Values: " + ann.values);
                        }
                    }
                }
            }
        }
    }
}
