package tech.jhipster.lite.module.infrastructure.secondary.javadependency.gradle;

import static tech.jhipster.lite.module.domain.JHipsterModule.LINE_BREAK;
import static tech.jhipster.lite.module.domain.JHipsterModule.from;
import static tech.jhipster.lite.module.domain.JHipsterModule.moduleBuilder;
import static tech.jhipster.lite.module.domain.JHipsterModule.to;
import static tech.jhipster.lite.module.domain.replacement.ReplacementCondition.always;
import static tech.jhipster.lite.module.infrastructure.secondary.javadependency.gradle.VersionsCatalog.libraryAlias;
import static tech.jhipster.lite.module.infrastructure.secondary.javadependency.gradle.VersionsCatalog.pluginAlias;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.jhipster.lite.module.domain.Indentation;
import tech.jhipster.lite.module.domain.JHipsterModuleContext;
import tech.jhipster.lite.module.domain.JHipsterProjectFilePath;
import tech.jhipster.lite.module.domain.ProjectFiles;
import tech.jhipster.lite.module.domain.buildproperties.BuildProperty;
import tech.jhipster.lite.module.domain.buildproperties.PropertyKey;
import tech.jhipster.lite.module.domain.file.JHipsterDestination;
import tech.jhipster.lite.module.domain.file.JHipsterFileContent;
import tech.jhipster.lite.module.domain.file.JHipsterModuleFile;
import tech.jhipster.lite.module.domain.file.JHipsterSource;
import tech.jhipster.lite.module.domain.file.JHipsterTemplatedFile;
import tech.jhipster.lite.module.domain.file.JHipsterTemplatedFiles;
import tech.jhipster.lite.module.domain.gradleplugin.GradleCommunityPlugin;
import tech.jhipster.lite.module.domain.gradleplugin.GradleCorePlugin;
import tech.jhipster.lite.module.domain.gradleplugin.GradlePluginConfiguration;
import tech.jhipster.lite.module.domain.javabuild.DependencySlug;
import tech.jhipster.lite.module.domain.javabuild.command.AddDirectJavaDependency;
import tech.jhipster.lite.module.domain.javabuild.command.AddDirectMavenPlugin;
import tech.jhipster.lite.module.domain.javabuild.command.AddGradlePlugin;
import tech.jhipster.lite.module.domain.javabuild.command.AddJavaBuildProfile;
import tech.jhipster.lite.module.domain.javabuild.command.AddJavaDependencyManagement;
import tech.jhipster.lite.module.domain.javabuild.command.AddMavenBuildExtension;
import tech.jhipster.lite.module.domain.javabuild.command.AddMavenPluginManagement;
import tech.jhipster.lite.module.domain.javabuild.command.RemoveDirectJavaDependency;
import tech.jhipster.lite.module.domain.javabuild.command.RemoveJavaDependencyManagement;
import tech.jhipster.lite.module.domain.javabuild.command.SetBuildProperty;
import tech.jhipster.lite.module.domain.javabuild.command.SetVersion;
import tech.jhipster.lite.module.domain.javabuildprofile.BuildProfileActivation;
import tech.jhipster.lite.module.domain.javabuildprofile.BuildProfileId;
import tech.jhipster.lite.module.domain.javadependency.JavaDependency;
import tech.jhipster.lite.module.domain.javadependency.JavaDependencyScope;
import tech.jhipster.lite.module.domain.properties.JHipsterModuleProperties;
import tech.jhipster.lite.module.domain.properties.JHipsterProjectFolder;
import tech.jhipster.lite.module.domain.replacement.ContentReplacers;
import tech.jhipster.lite.module.domain.replacement.MandatoryFileReplacer;
import tech.jhipster.lite.module.domain.replacement.MandatoryReplacer;
import tech.jhipster.lite.module.domain.replacement.RegexNeedleBeforeReplacer;
import tech.jhipster.lite.module.domain.replacement.RegexReplacer;
import tech.jhipster.lite.module.infrastructure.secondary.FileSystemJHipsterModuleFiles;
import tech.jhipster.lite.module.infrastructure.secondary.FileSystemReplacer;
import tech.jhipster.lite.module.infrastructure.secondary.javadependency.JavaDependenciesCommandHandler;
import tech.jhipster.lite.shared.error.domain.Assert;
import tech.jhipster.lite.shared.error.domain.GeneratorException;
import tech.jhipster.lite.shared.generation.domain.ExcludeFromGeneratedCodeCoverage;

public class GradleCommandHandler implements JavaDependenciesCommandHandler {

  private static final String COMMAND = "command";
  private static final String BUILD_GRADLE_FILE = "build.gradle.kts";

  private static final Pattern GRADLE_PLUGIN_NEEDLE = Pattern.compile("^\\s+// jhipster-needle-gradle-plugins$", Pattern.MULTILINE);
  private static final Pattern GRADLE_PLUGIN_PROJECT_EXTENSION_CONFIGURATION_NEEDLE = Pattern.compile(
    "^// jhipster-needle-gradle-plugins-configurations$",
    Pattern.MULTILINE
  );
  private static final Pattern GRADLE_IMPLEMENTATION_DEPENDENCY_NEEDLE = Pattern.compile(
    "^\\s+// jhipster-needle-gradle-implementation-dependencies$",
    Pattern.MULTILINE
  );
  private static final Pattern GRADLE_COMPILE_DEPENDENCY_NEEDLE = Pattern.compile(
    "^\\s+// jhipster-needle-gradle-compile-dependencies$",
    Pattern.MULTILINE
  );
  private static final Pattern GRADLE_RUNTIME_DEPENDENCY_NEEDLE = Pattern.compile(
    "^\\s+// jhipster-needle-gradle-runtime-dependencies$",
    Pattern.MULTILINE
  );
  private static final Pattern GRADLE_TEST_DEPENDENCY_NEEDLE = Pattern.compile(
    "^\\s+// jhipster-needle-gradle-test-dependencies$",
    Pattern.MULTILINE
  );
  private static final Pattern GRADLE_PROFILE_ACTIVATION_NEEDLE = Pattern.compile(
    "^// jhipster-needle-profile-activation$",
    Pattern.MULTILINE
  );
  private static final Pattern GRADLE_PROPERTY_NEEDLE = Pattern.compile("^// jhipster-needle-gradle-properties$", Pattern.MULTILINE);
  private static final String PROFILE_CONDITIONAL_TEMPLATE =
    """
    if (profiles.contains("%s")) {
      apply(plugin = "profile-%s")
    }\
    """;
  private static final String PROFILE_DEFAULT_ACTIVATION_CONDITIONAL_TEMPLATE =
    """
    if (profiles.isEmpty() || profiles.contains("%s")) {
      apply(plugin = "profile-%s")
    }\
    """;
  private static final String BUILD_GRADLE_PROFILE_PATH_TEMPLATE = "buildSrc/src/main/kotlin/profile-%s.gradle.kts";

  private final Indentation indentation;
  private final JHipsterProjectFolder projectFolder;
  private final VersionsCatalog versionsCatalog;
  private final FileSystemReplacer fileReplacer = new FileSystemReplacer();
  private final FileSystemJHipsterModuleFiles files;

  public GradleCommandHandler(Indentation indentation, JHipsterProjectFolder projectFolder, ProjectFiles filesReader) {
    Assert.notNull("indentation", indentation);
    Assert.notNull("projectFolder", projectFolder);

    this.indentation = indentation;
    this.projectFolder = projectFolder;
    this.versionsCatalog = new VersionsCatalog(projectFolder);
    this.files = new FileSystemJHipsterModuleFiles(filesReader);
  }

  @Override
  public void handle(SetVersion command) {
    Assert.notNull(COMMAND, command);

    versionsCatalog.setVersion(command.version());
  }

  @Override
  public void handle(AddDirectJavaDependency command) {
    Assert.notNull(COMMAND, command);

    versionsCatalog.addLibrary(command.dependency());
    addDependencyToBuildGradle(command.dependency());
  }

  private void addDependencyToBuildGradle(JavaDependency dependency) {
    GradleDependencyScope gradleScope = gradleDependencyScope(dependency);

    String dependencyDeclaration = dependencyDeclaration(dependency);
    MandatoryReplacer replacer = new MandatoryReplacer(
      new RegexNeedleBeforeReplacer(
        (contentBeforeReplacement, newText) -> !contentBeforeReplacement.contains(newText),
        needleForGradleDependencyScope(gradleScope)
      ),
      dependencyDeclaration
    );
    fileReplacer.handle(
      projectFolder,
      ContentReplacers.of(new MandatoryFileReplacer(new JHipsterProjectFilePath(BUILD_GRADLE_FILE), replacer))
    );
  }

  private Pattern needleForGradleDependencyScope(GradleDependencyScope gradleScope) {
    return switch (gradleScope) {
      case GradleDependencyScope.IMPLEMENTATION -> GRADLE_IMPLEMENTATION_DEPENDENCY_NEEDLE;
      case GradleDependencyScope.COMPILE_ONLY -> GRADLE_COMPILE_DEPENDENCY_NEEDLE;
      case GradleDependencyScope.RUNTIME_ONLY -> GRADLE_RUNTIME_DEPENDENCY_NEEDLE;
      case GradleDependencyScope.TEST_IMPLEMENTATION -> GRADLE_TEST_DEPENDENCY_NEEDLE;
    };
  }

  private String dependencyDeclaration(JavaDependency dependency) {
    StringBuilder dependencyDeclaration = new StringBuilder()
      .append(indentation.times(1))
      .append(gradleDependencyScope(dependency).command())
      .append("(");
    if (dependency.scope() == JavaDependencyScope.IMPORT) {
      dependencyDeclaration.append("platform(%s)".formatted(versionCatalogReference(dependency)));
    } else {
      dependencyDeclaration.append(versionCatalogReference(dependency));
    }
    dependencyDeclaration.append(")");

    if (!dependency.exclusions().isEmpty()) {
      dependencyDeclaration.append(" {");
      for (var exclusion : dependency.exclusions()) {
        dependencyDeclaration.append(LINE_BREAK);
        dependencyDeclaration
          .append(indentation.times(2))
          .append("exclude(group = \"%s\", module = \"%s\")".formatted(exclusion.groupId(), exclusion.artifactId()));
      }
      dependencyDeclaration.append(LINE_BREAK);
      dependencyDeclaration.append(indentation.times(1)).append("}");
    }

    return dependencyDeclaration.toString();
  }

  private static String versionCatalogReference(JavaDependency dependency) {
    return "libs.%s".formatted(applyVersionCatalogReferenceConvention(libraryAlias(dependency)));
  }

  private static String applyVersionCatalogReferenceConvention(String rawVersionCatalogReference) {
    return rawVersionCatalogReference.replace("-", ".");
  }

  private static GradleDependencyScope gradleDependencyScope(JavaDependency dependency) {
    return switch (dependency.scope()) {
      case TEST -> GradleDependencyScope.TEST_IMPLEMENTATION;
      case PROVIDED -> GradleDependencyScope.COMPILE_ONLY;
      case RUNTIME -> GradleDependencyScope.RUNTIME_ONLY;
      default -> GradleDependencyScope.IMPLEMENTATION;
    };
  }

  @Override
  public void handle(RemoveDirectJavaDependency command) {
    versionsCatalog.retrieveDependencySlugsFrom(command.dependency()).forEach(this::removeDependencyFromBuildGradle);
    versionsCatalog.removeLibrary(command.dependency());
  }

  private void removeDependencyFromBuildGradle(DependencySlug dependencySlug) {
    String scopePattern = Stream.of(GradleDependencyScope.values())
      .map(GradleDependencyScope::command)
      .collect(Collectors.joining("|", "(?:", ")"));
    Pattern dependencyLinePattern = Pattern.compile(
      "^\\s+%s\\((?:platform\\()?libs\\.%s\\)?\\)(?:\\s+\\{(?:\\s+exclude\\([^)]*\\))+\\s+\\})?$".formatted(
          scopePattern,
          dependencySlug.slug().replace("-", "\\.")
        ),
      Pattern.MULTILINE
    );
    MandatoryReplacer replacer = new MandatoryReplacer(new RegexReplacer(always(), dependencyLinePattern), "");
    fileReplacer.handle(
      projectFolder,
      ContentReplacers.of(new MandatoryFileReplacer(new JHipsterProjectFilePath(BUILD_GRADLE_FILE), replacer))
    );
  }

  @Override
  public void handle(RemoveJavaDependencyManagement command) {
    versionsCatalog.retrieveDependencySlugsFrom(command.dependency()).forEach(this::removeDependencyFromBuildGradle);
    versionsCatalog.removeLibrary(command.dependency());
  }

  @Override
  public void handle(AddJavaDependencyManagement command) {
    versionsCatalog.addLibrary(command.dependency());
    addDependencyToBuildGradle(command.dependency());
  }

  @Override
  public void handle(AddDirectMavenPlugin command) {
    // Maven specific commands are ignored
  }

  @Override
  public void handle(AddMavenPluginManagement command) {
    // Maven commands are ignored
  }

  @Override
  public void handle(SetBuildProperty command) {
    Assert.notNull(COMMAND, command);

    command
      .buildProfile()
      .ifPresent(buildProfile -> {
        File scriptPlugin = scriptPluginForProfile(buildProfile);
        if (!scriptPlugin.exists()) {
          throw new MissingGradleProfileException(buildProfile);
        }
        addPropertyTo(command.property(), scriptPlugin);
      });
  }

  private void addPropertyTo(BuildProperty property, File buildGradleFile) {
    String gradlePropertyDeclaration = convertToKotlinFormat(property);

    MandatoryReplacer replacer;
    if (propertyExistsFrom(toCamelCasedKotlinVariable(property.key()), buildGradleFile.toPath())) {
      replacer = existingPropertyReplacer(property, gradlePropertyDeclaration);
    } else {
      replacer = addNewPropertyReplacer(gradlePropertyDeclaration);
    }

    fileReplacer.handle(
      projectFolder,
      ContentReplacers.of(
        new MandatoryFileReplacer(
          new JHipsterProjectFilePath(Path.of(projectFolder.folder()).relativize(buildGradleFile.toPath()).toString()),
          replacer
        )
      )
    );
  }

  private MandatoryReplacer existingPropertyReplacer(BuildProperty property, String gradlePropertyDeclaration) {
    Pattern propertyLinePattern = Pattern.compile(
      "val %s by extra\\(\"(.*?)\"\\)".formatted(toCamelCasedKotlinVariable(property.key())),
      Pattern.MULTILINE
    );
    return new MandatoryReplacer(
      new RegexReplacer(
        (contentBeforeReplacement, replacement) -> propertyLinePattern.matcher(contentBeforeReplacement).find(),
        propertyLinePattern
      ),
      gradlePropertyDeclaration
    );
  }

  private static MandatoryReplacer addNewPropertyReplacer(String gradlePropertyDeclaration) {
    return new MandatoryReplacer(new RegexNeedleBeforeReplacer(always(), GRADLE_PROPERTY_NEEDLE), gradlePropertyDeclaration);
  }

  private String convertToKotlinFormat(BuildProperty property) {
    return "val %s by extra(\"%s\")".formatted(toCamelCasedKotlinVariable(property.key()), property.value().get());
  }

  private String toCamelCasedKotlinVariable(PropertyKey key) {
    return Arrays.stream(key.get().split("\\."))
      .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
      .collect(Collectors.collectingAndThen(Collectors.joining(), str -> str.substring(0, 1).toLowerCase() + str.substring(1)));
  }

  @ExcludeFromGeneratedCodeCoverage(reason = "The exception handling is hard to test and an implementation detail")
  private Boolean propertyExistsFrom(String gradlePropertyFormatted, Path buildGradleProfileFile) {
    try {
      String content = Files.readString(buildGradleProfileFile);

      return content.contains(gradlePropertyFormatted);
    } catch (IOException e) {
      throw GeneratorException.technicalError("Error writing pom: " + e.getMessage(), e);
    }
  }

  @Override
  public void handle(AddMavenBuildExtension command) {
    // Maven commands are ignored
  }

  @Override
  public void handle(AddJavaBuildProfile command) {
    Assert.notNull(COMMAND, command);

    enablePrecompiledScriptPlugins();

    File scriptPlugin = scriptPluginForProfile(command.buildProfileId());
    if (!scriptPlugin.exists()) {
      addProfileActivation(command);
      addScriptPluginForProfile(command.buildProfileId());
    }
  }

  private void enablePrecompiledScriptPlugins() {
    addFileToProject(from("buildtool/gradle/buildSrc/build.gradle.kts.template"), to("buildSrc/build.gradle.kts"));
  }

  private File scriptPluginForProfile(BuildProfileId buildProfileId) {
    return projectFolder.filePath(BUILD_GRADLE_PROFILE_PATH_TEMPLATE.formatted(buildProfileId.value())).toFile();
  }

  private void addProfileActivation(AddJavaBuildProfile command) {
    MandatoryReplacer replacer = new MandatoryReplacer(
      new RegexNeedleBeforeReplacer(always(), GRADLE_PROFILE_ACTIVATION_NEEDLE),
      fillProfileActivationTemplate(command)
    );
    fileReplacer.handle(
      projectFolder,
      ContentReplacers.of(new MandatoryFileReplacer(new JHipsterProjectFilePath(BUILD_GRADLE_FILE), replacer))
    );
  }

  private static String fillProfileActivationTemplate(AddJavaBuildProfile command) {
    Optional<Boolean> isActiveByDefault = command.activation().flatMap(BuildProfileActivation::activeByDefault);

    return (isActiveByDefault.orElse(false) ? PROFILE_DEFAULT_ACTIVATION_CONDITIONAL_TEMPLATE : PROFILE_CONDITIONAL_TEMPLATE).formatted(
        command.buildProfileId(),
        command.buildProfileId()
      );
  }

  private void addScriptPluginForProfile(BuildProfileId buildProfileId) {
    addFileToProject(
      from("buildtool/gradle/buildSrc/src/main/kotlin/profile.gradle.kts.template"),
      to("buildSrc/src/main/kotlin/profile-%s.gradle.kts".formatted(buildProfileId))
    );
  }

  private void addFileToProject(JHipsterSource source, JHipsterDestination destination) {
    if (projectFolder.fileExists(destination.get())) {
      return;
    }
    files.create(
      projectFolder,
      new JHipsterTemplatedFiles(
        List.of(
          JHipsterTemplatedFile.builder()
            .file(new JHipsterModuleFile(new JHipsterFileContent(source), destination, false))
            .context(context())
            .build()
        )
      )
    );
  }

  private JHipsterModuleContext context() {
    JHipsterModuleProperties properties = new JHipsterModuleProperties(projectFolder.get(), false, null);
    return JHipsterModuleContext.builder(moduleBuilder(properties)).build();
  }

  @Override
  public void handle(AddGradlePlugin command) {
    Assert.notNull(COMMAND, command);

    switch (command.plugin()) {
      case GradleCorePlugin plugin -> declarePlugin(plugin.id().get());
      case GradleCommunityPlugin plugin -> {
        declarePlugin("alias(libs.plugins.%s)".formatted(applyVersionCatalogReferenceConvention(pluginAlias(plugin))));
        versionsCatalog.addPlugin(plugin);
      }
    }
    command.plugin().configuration().ifPresent(this::addPluginConfiguration);
    command.toolVersion().ifPresent(version -> handle(new SetVersion(version)));
    command.pluginVersion().ifPresent(version -> handle(new SetVersion(version)));
  }

  private void declarePlugin(String pluginDeclaration) {
    MandatoryReplacer replacer = new MandatoryReplacer(
      new RegexNeedleBeforeReplacer(
        (contentBeforeReplacement, newText) -> !contentBeforeReplacement.contains(newText),
        GRADLE_PLUGIN_NEEDLE
      ),
      indentation.times(1) + pluginDeclaration
    );
    fileReplacer.handle(
      projectFolder,
      ContentReplacers.of(new MandatoryFileReplacer(new JHipsterProjectFilePath(BUILD_GRADLE_FILE), replacer))
    );
  }

  private void addPluginConfiguration(GradlePluginConfiguration pluginConfiguration) {
    MandatoryReplacer replacer = new MandatoryReplacer(
      new RegexNeedleBeforeReplacer(
        (contentBeforeReplacement, newText) -> !contentBeforeReplacement.contains(newText),
        GRADLE_PLUGIN_PROJECT_EXTENSION_CONFIGURATION_NEEDLE
      ),
      LINE_BREAK + pluginConfiguration.get()
    );
    fileReplacer.handle(
      projectFolder,
      ContentReplacers.of(new MandatoryFileReplacer(new JHipsterProjectFilePath(BUILD_GRADLE_FILE), replacer))
    );
  }
}
