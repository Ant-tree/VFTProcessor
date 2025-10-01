package com.anttree.vft.processors.transformer;

import com.anttree.vft.processors.annotations.Unused;
import com.anttree.vft.processors.models.Pair;
import com.anttree.vft.processors.utils.NodeUtils;
import com.anttree.vft.processors.utils.Utils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class AsmOptimizeTransformer implements Transformer {


    private final String buildTypeLower;

    public AsmOptimizeTransformer(String buildType) {
        this.buildTypeLower = buildType == null ? "" : buildType.toLowerCase();
    }

    @Override
    public byte[] transform(byte[] inBytes) {
        if (inBytes == null || inBytes.length == 0) {
            return inBytes;
        }

        ClassNode classNode = NodeUtils.toClassNode(inBytes);

        for (AnnotationNode annotation : classNode.invisibleAnnotations) {
            List<Pair<String, Object>> annotations = NodeUtils.annotationValues(
                    annotation
            );
            if (annotations.size() == 0) {
                continue;
            }
        }

        // No recalculation of stack frames or maxs needed as we only change access flags
        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        return cw.toByteArray();
    }

}
