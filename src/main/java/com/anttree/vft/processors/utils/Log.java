package com.anttree.vft.processors.utils;

public class Log {
    private static final boolean ENABLED = true;

    public static void descriptor(String tag, String kind, String owner, String msg) {
        String signatureFormat = "%s %s %s";
        String signature = String.format(signatureFormat
                , kind
                , owner
                , msg);
        info(tag, signature);
    }

    public static void descriptor(String tag, String kind, String owner, String name, String msg) {
        String signatureFormat = "%s %s%s %s";
        String signature = String.format(signatureFormat
                , kind
                , owner
                , (name != null ? ("#" + name) : "")
                , msg);
        info(tag, signature);
    }

    public static void info(String tag, String msg) {
        if (ENABLED) {
            System.out.println(tag + " " + msg);
        }
    }

    public static void error(String msg) {
        if (ENABLED) {
            System.out.println(msg);
        }
    }

    public static void error(String msg, Throwable e) {
        if (ENABLED) {
            System.out.println(msg + " " + e.toString());
        }
    }
}
