package tech.jhipster.lite.generator.server.springboot.cucumber.domain;

import static tech.jhipster.lite.generator.server.springboot.cucumbercommon.domain.CucumbersModules.cucumberModuleBuilder;
import static tech.jhipster.lite.module.domain.JHipsterModule.JHipsterModuleBuilder;
import static tech.jhipster.lite.module.domain.JHipsterModule.documentationTitle;
import static tech.jhipster.lite.module.domain.JHipsterModule.from;
import static tech.jhipster.lite.module.domain.JHipsterModule.moduleBuilder;
import static tech.jhipster.lite.module.domain.JHipsterModule.to;
import static tech.jhipster.lite.module.domain.JHipsterModule.toSrcTestJava;

import tech.jhipster.lite.module.domain.JHipsterModule;
import tech.jhipster.lite.module.domain.file.JHipsterDestination;
import tech.jhipster.lite.module.domain.file.JHipsterSource;
import tech.jhipster.lite.module.domain.properties.JHipsterModuleProperties;
import tech.jhipster.lite.shared.error.domain.Assert;

public class CucumberModuleFactory {

  private static final JHipsterSource SOURCE = from("server/springboot/cucumber");

  public JHipsterModule buildInitializationModule(JHipsterModuleProperties properties) {
    Assert.notNull("properties", properties);

    String baseName = properties.projectBaseName().capitalized();
    JHipsterDestination destination = toSrcTestJava().append(properties.packagePath()).append("cucumber");

    // @formatter:off
    JHipsterModuleBuilder builder = cucumberModuleBuilder(properties)
    .context()
      .put("baseName", baseName)
      .and()
    .documentation(documentationTitle("Cucumber"), SOURCE.template("cucumber.md"))
    .files()
      .batch(SOURCE, destination)
        .addTemplate("CucumberConfiguration.java")
        .addTemplate("CucumberTest.java")
        .and()
      .batch(SOURCE.append("rest"), destination.append("rest"))
        .addTemplate("AsyncElementAsserter.java")
        .addTemplate("AsyncHeaderAsserter.java")
        .addTemplate("AsyncResponseAsserter.java")
        .addTemplate("Awaiter.java")
        .addTemplate("CucumberRestAssertions.java")
        .addTemplate("CucumberRestTemplate.java")
        .addTemplate("CucumberJson.java")
        .addTemplate("CucumberRestTestContext.java")
        .addTemplate("CucumberRestTestContextUnitTest.java")
        .addTemplate("ElementAsserter.java")
        .addTemplate("ElementAssertions.java")
        .addTemplate("HeaderAsserter.java")
        .addTemplate("HeaderAssertions.java")
        .addTemplate("ResponseAsserter.java")
        .addTemplate("SyncElementAsserter.java")
        .addTemplate("SyncHeaderAsserter.java")
        .addTemplate("SyncResponseAsserter.java")
        .and()
      .add(SOURCE.file("gitkeep"), to("src/test/features/.gitkeep"))
      .and();
    // @formatter:on

    return builder.build();
  }

  public JHipsterModule buildJpaResetModule(JHipsterModuleProperties properties) {
    Assert.notNull("properties", properties);

    return moduleBuilder(properties)
      .files()
      .add(
        SOURCE.template("CucumberJpaReset.java"),
        toSrcTestJava().append(properties.packagePath()).append("cucumber").append("CucumberJpaReset.java")
      )
      .and()
      .build();
  }
}
