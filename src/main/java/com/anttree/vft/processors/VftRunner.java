package com.anttree.vft.processors;
 
import com.anttree.vft.processors.transformer.AsmVftTransformer;
import com.anttree.vft.processors.transformer.Transformer;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VftRunner {
 
    /**
     * args:
     *   0: inputClassesDir  (예: build/classes/java/main)
     *   1: outputClassesDir (예: build/classes-vft/main)
     *   2: buildType        (예: "release" 또는 "debug")
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: VftRunner <inDir> <outDir> <buildType>");
            System.exit(2);
        }
        Path inDir  = Paths.get(args[0]);
        Path outDir = Paths.get(args[1]);
        String buildType = args[2];
 
        if (!Files.isDirectory(inDir)) {
            throw new IllegalArgumentException("Invalid input dir: " + inDir);
        }
 
        Files.createDirectories(outDir);

        try (Stream<Path> files = Files.walk(inDir)) {
            transform(files, inDir, outDir, buildType);
        }
        System.out.println("[ASM-VFT] Done for buildType=" + buildType);
    }

    private static void transform(Stream<Path> files, Path inDir, Path outDir, String buildType) {
        List<Transformer> transformers = new ArrayList<>() {{
            add(new AsmVftTransformer(buildType));
        }};

        files.forEach(src -> {
            try {
                Path rel = inDir.relativize(src);
                Path dst = outDir.resolve(rel);
                // If directory, create it
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dst);
                    return;
                }
                // If not a .class file, just copy
                if (!src.toString().endsWith(".class")) {
                    Files.createDirectories(dst.getParent());
                    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
                // It's a .class file - apply transformations
                applyTransformation(transformers, src, dst);

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static void applyTransformation(List<Transformer> transformers, Path src, Path dst) throws IOException {
        byte[] outBytes = null;
        for (Transformer transformer : transformers) {
            outBytes = transformer.transform(outBytes == null
                    ? Files.readAllBytes(src)
                    : outBytes
            );
            if (outBytes == null) {
                return;
            }
        }
        if (outBytes == null) {
            return;
        }
        Files.createDirectories(dst.getParent());
        Files.write(dst, outBytes);
    }
}