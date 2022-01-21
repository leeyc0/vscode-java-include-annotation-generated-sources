# Gradle plugin to let VS Code recognized generated .java source files

This plugin adds generatedSourceOutputDirectory path to SrcDir of all SourceSets in all subprojects, so that VS Code will resolve AutoValue classes correctly. Using this plugin saves you from manually editing sourceSets in every subprojects.

reference: https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-gradle
> There are cases where Gradle tasks will generate Java classes. To ensure these Java classes are indexed correctly by the Java language server, you need to ensure the paths are added to the .classpath, and this is typically achieved using Gradle sourceSets.
