package tech.jhipster.lite.module.infrastructure.secondary.javadependency;

import org.springframework.stereotype.Service;
import tech.jhipster.lite.module.domain.Indentation;
import tech.jhipster.lite.module.domain.ProjectFiles;
import tech.jhipster.lite.module.domain.javabuild.JavaBuildTool;
import tech.jhipster.lite.module.domain.javabuild.ProjectJavaBuildToolRepository;
import tech.jhipster.lite.module.domain.javabuild.command.*;
import tech.jhipster.lite.module.domain.properties.JHipsterProjectFolder;
import tech.jhipster.lite.module.infrastructure.secondary.javadependency.gradle.GradleCommandHandler;
import tech.jhipster.lite.module.infrastructure.secondary.javadependency.maven.MavenCommandHandler;
import tech.jhipster.lite.shared.error.domain.Assert;
import tech.jhipster.lite.shared.generation.domain.ExcludeFromGeneratedCodeCoverage;

@Service
public class FileSystemJavaBuildCommandsHandler {

  private final ProjectJavaBuildToolRepository javaBuildTools;
  private final ProjectFiles filesReader;

  public FileSystemJavaBuildCommandsHandler(ProjectJavaBuildToolRepository javaBuildTools, ProjectFiles filesReader) {
    this.javaBuildTools = javaBuildTools;
    this.filesReader = filesReader;
  }

  public void handle(Indentation indentation, JHipsterProjectFolder projectFolder, JavaBuildCommands commands) {
    Assert.notNull("indentation", indentation);
    Assert.notNull("projectFolder", projectFolder);
    Assert.notNull("commands", commands);

    if (commands.isEmpty()) {
      return;
    }

    JavaDependenciesCommandHandler handler = buildCommandHandler(indentation, projectFolder);

    commands.get().forEach(command -> handle(handler, command));
  }

  private JavaDependenciesCommandHandler buildCommandHandler(Indentation indentation, JHipsterProjectFolder projectFolder) {
    JavaBuildTool javaBuildTool = javaBuildTools
      .detect(projectFolder)
      .orElseThrow(() -> new MissingJavaBuildConfigurationException(projectFolder));
    return switch (javaBuildTool) {
      case MAVEN -> new MavenCommandHandler(indentation, projectFolder.filePath("pom.xml"));
      case GRADLE -> new GradleCommandHandler(indentation, projectFolder, filesReader);
    };
  }

  @ExcludeFromGeneratedCodeCoverage(reason = "Jacoco thinks there is a missed branch")
  private void handle(JavaDependenciesCommandHandler handler, JavaBuildCommand command) {
    switch (command) {
      case SetVersion setVersion -> handler.handle(setVersion);
      case SetBuildProperty setBuildProperty -> handler.handle(setBuildProperty);
      case RemoveJavaDependencyManagement removeJavaDependencyManagement -> handler.handle(removeJavaDependencyManagement);
      case AddJavaDependencyManagement addJavaDependencyManagement -> handler.handle(addJavaDependencyManagement);
      case RemoveDirectJavaDependency removeDirectJavaDependency -> handler.handle(removeDirectJavaDependency);
      case AddDirectJavaDependency addDirectJavaDependency -> handler.handle(addDirectJavaDependency);
      case AddDirectMavenPlugin addDirectMavenPlugin -> handler.handle(addDirectMavenPlugin);
      case AddMavenPluginManagement addMavenPluginManagement -> handler.handle(addMavenPluginManagement);
      case AddMavenBuildExtension addMavenBuildExtension -> handler.handle(addMavenBuildExtension);
      case AddJavaBuildProfile addJavaBuildProfile -> handler.handle(addJavaBuildProfile);
      case AddGradlePlugin addGradlePlugin -> handler.handle(addGradlePlugin);
      case AddGradleConfiguration addGradleConfiguration -> handler.handle(addGradleConfiguration);
    }
  }
}
