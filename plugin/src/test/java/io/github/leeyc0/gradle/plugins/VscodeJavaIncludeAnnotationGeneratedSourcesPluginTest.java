package io.github.leeyc0.gradle.plugins;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testfixtures.ProjectBuilder;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class VscodeJavaIncludeAnnotationGeneratedSourcesPluginTest {
    private static final String ID = "io.github.leeyc0.gradle.plugins.vscode-java-include-annotation-generated-sources";
    private static final String BUILD_SRC_MAIN_DIR = "buildSrcMainDir";
    private static final String BUILD_SRC_TEST_DIR = "buildSrcTestDir";

    @Test
    public void setPlugin() throws IOException {
        var projectBuilder = ProjectBuilder.builder();
        Project rootProject = projectBuilder
            .build();
        Project subproject = projectBuilder
            .withParent(rootProject)
            .build();
        
        // set up root project
        PluginManager rootProjectPluginManager = rootProject.getPluginManager();
        rootProjectPluginManager.apply(ID);

        // set up subproject
        PluginManager subprojectPluginManager = subproject.getPluginManager();
        String subProjectDir = subproject.getProjectDir().getAbsolutePath();
        subprojectPluginManager.apply("java");
        Set<Project> subprojects = rootProject.getSubprojects();
        TaskContainer subprojectTaskContainer = subproject.getTasks();
        var subprojectJavaCompileMainTask = subprojectTaskContainer.named("compileJava", JavaCompile.class).get();
        var subprojectJavaCompileTestTask = subprojectTaskContainer.named("compileTestJava", JavaCompile.class).get();
        subprojectJavaCompileMainTask.getOptions()
            .getGeneratedSourceOutputDirectory()
            .set(new File(BUILD_SRC_MAIN_DIR));
        subprojectJavaCompileTestTask.getOptions()
            .getGeneratedSourceOutputDirectory()
            .set(new File(BUILD_SRC_TEST_DIR));
        assertTrue(subprojects.contains(subproject), "root project contains subproject");
        
        // triggers the evaluation of all projects
        rootProject.getTasksByName("build", true);
        assertTrue(rootProject.getState().getExecuted(), "root project is evaluated");
        assertTrue(subproject.getState().getExecuted(), "subproject is evaluated");
        assertTrue(rootProjectPluginManager.hasPlugin(ID), "root project is applied with "+ID+" plugin");
        assertTrue(subprojectPluginManager.hasPlugin("java"), "subproject is applied with java plugin");
        
        // get srcdirs
        ExtensionContainer extensionContainer = subproject.getExtensions();
        var sourceSetContainer = (SourceSetContainer) extensionContainer.getByName("sourceSets");
        var sourceSetMap = new HashMap<String,Set<File>>();
        for (SourceSet s : sourceSetContainer) {
            sourceSetMap.put(s.getName(),
                s.getJava().getSrcDirs());
        }
        var mainBuildSrcDir = (new File(subProjectDir+"/"+BUILD_SRC_MAIN_DIR)).getAbsoluteFile();
        var testBuildSrcDir = (new File(subProjectDir+"/"+BUILD_SRC_TEST_DIR)).getAbsoluteFile();
        assertTrue(sourceSetMap.get("main").contains(mainBuildSrcDir), "main srcSet is appended with generatedSourceOutputDirectory");
        assertTrue(sourceSetMap.get("test").contains(testBuildSrcDir), "test srcSet is appended with generatedSourceOutputDirectory");
    }
}
