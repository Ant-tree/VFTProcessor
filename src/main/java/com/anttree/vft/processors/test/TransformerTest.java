package com.anttree.vft.processors.test;

import com.anttree.vft.processors.transformer.AsmVftTransformer;

import java.io.IOException;
import java.io.InputStream;

public class TransformerTest {

    public static void main(String[] args) throws IOException {
        String className = "com/anttree/vft/processors/test/TestClass";

        try (InputStream is = TransformerTest.class.getClassLoader().getResourceAsStream(className + ".class")) {
            if (is == null) {
                System.out.println("Class file not found: " + className);
                return;
            }

            byte[] originalBytes = is.readAllBytes();
            System.out.println("Original class size: " + originalBytes.length + " bytes");

            // release 빌드 타입으로 transformer 테스트 (기본 flavor와 매칭)
            AsmVftTransformer transformer = new AsmVftTransformer("release");
            byte[] transformedBytes = transformer.transform(originalBytes);

            if (transformedBytes == null) {
                System.out.println("Class was removed (NONE scope)");
            } else if (transformedBytes == originalBytes) {
                System.out.println("No changes made - annotations not found or not matching build type");
            } else {
                System.out.println("Class was transformed! New size: " + transformedBytes.length + " bytes");
            }
        }
    }
}
