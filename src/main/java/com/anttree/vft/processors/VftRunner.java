package com.anttree.vft.processors;
 
import java.io.*;
import java.nio.file.*;
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
            transformVftASM(files, inDir, outDir, buildType);
        }
    }

    private static void transformVftASM(Stream<Path> files, Path inDir, Path outDir, String buildType) {
        AsmVftTransformer transformer = new AsmVftTransformer(buildType);
        files.forEach(src -> {
            try {
                Path rel = inDir.relativize(src);
                Path dst = outDir.resolve(rel);
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dst);
                } else if (src.toString().endsWith(".class")) {
                    byte[] inBytes = Files.readAllBytes(src);
                    byte[] outBytes = transformer.transform(inBytes);
                    if (outBytes == null) {
                        return;
                    }
                    Files.createDirectories(dst.getParent());
                    Files.write(dst, outBytes);
                } else {
                    Files.createDirectories(dst.getParent());
                    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        System.out.println("[ASM-VFT] Done for buildType=" + buildType);
    }
}