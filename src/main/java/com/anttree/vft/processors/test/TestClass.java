package com.anttree.vft.processors.test;

import com.anttree.vft.processors.annotations.VisibleForTesting;

public class TestClass {

    @VisibleForTesting(scope = VisibleForTesting.Scope.PRIVATE)
    private String testField = "test";

    @VisibleForTesting(scope = VisibleForTesting.Scope.PACKAGE_PRIVATE)
    public void testMethod() {
        System.out.println("test");
    }
}
