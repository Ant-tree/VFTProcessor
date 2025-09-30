package com.anttree.vft.processors.utils;

public class Utils {
    public static String getDescriptor(Class<?> target) {
        return "L" + target.getTypeName().replace(".", "/") + ";";
    }
}
