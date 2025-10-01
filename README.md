# @VisibleForTesting Annotation Processor

> Precision access: visible when it matters.

This supports annotation-based processing to reduce access modifiers (e.g., from public to private) or to remove classes, methods, and field signatures as needed.

This is especially useful for unit tests with JUnit.

For example, when you need to provide a test-only parameter for a feature, you can define a method like:
```java
public void setMockupInputForTest(String inputPath) { ... }
```

To ensure that this method is excluded in the release flavor, declare it as follows:

```java
@VisibleForTesting(scope = VisibleForTesting.Scope.NONE, flavor = "release")
public void setMockupInputForTest(String inputPath) { ... }
```

## 1. Supported Scopes
Annotation supports following scopes:
```
PACKAGE_PRIVATE,
PRIVATE,
PROTECTED,
PUBLIC,
NONE
```

For scope ```NONE```, this will remove the annotated signature from designated flavor.

## 2. gradle usage
To ensure this processor to modify the source during the compilation, define as follows:
```groovy
tasks.register('vftTransform', JavaExec) {
    dependsOn tasks.classes

    def vftOut = layout.buildDirectory.dir("classes-vft/main").get().asFile

    classpath = fileTree(dir: 'libs', include: 'vft-processor-1.0.jar')
    args "$buildDir/classes/java/main", vftOut.absolutePath, buildType

    doLast { sourceSets.main.output.classesDirs.setFrom(files(vftOut)) }
}

jar {
    dependsOn tasks.named('vftTransform')
    ...
}
```
