package io.github.leeyc0.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.HashMap;
import java.util.Map;

public class VscodeJavaIncludeAnnotationGeneratedSourcesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project p) {
        var taskToSourceSetMap = Map.of("compileJava", "main",
            "compileTestJava", "test");

        p.subprojects(subP -> {
            subP.getPluginManager().withPlugin("java", plugin -> {
                subP.afterEvaluate(innerP -> {
                    var generatedSourceOutputDirectoryMap = new HashMap<String,String>();
                    ExtensionContainer extensionContainer = innerP.getExtensions();
                    var sourceSetContainer = (SourceSetContainer) extensionContainer.getByName("sourceSets");

                    // get generatedSourceOutputDirectory
                    for (String task : taskToSourceSetMap.keySet()) {
                        var compileJavaTask = innerP.getTasks().named(task, JavaCompile.class).get();
                        CompileOptions compileOptions = compileJavaTask.getOptions();
                        String sourceSet = taskToSourceSetMap.get(task);
                        String generatedSourceOutputDirectory = compileOptions.getGeneratedSourceOutputDirectory()
                            .getAsFile().get().toString();
                        generatedSourceOutputDirectoryMap.put(sourceSet, generatedSourceOutputDirectory);
                    }
                    
                    // append generatedSourceOutputDirectory to respective SourceSet
                    for (SourceSet s : sourceSetContainer) {
                        String sourceSetName = s.getName();
                        String generatedSourceOutputDirectory = generatedSourceOutputDirectoryMap.get(sourceSetName);
                        s.java(sourceDirectorySet -> {
                            sourceDirectorySet.srcDirs(generatedSourceOutputDirectory);
                        });
                    }
                });
            });
        });
    }
}
