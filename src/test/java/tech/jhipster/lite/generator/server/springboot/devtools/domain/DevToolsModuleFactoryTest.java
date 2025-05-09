package tech.jhipster.lite.generator.server.springboot.devtools.domain;

import static tech.jhipster.lite.module.infrastructure.secondary.JHipsterModulesAssertions.assertThatModuleWithFiles;
import static tech.jhipster.lite.module.infrastructure.secondary.JHipsterModulesAssertions.pomFile;

import org.junit.jupiter.api.Test;
import tech.jhipster.lite.TestFileUtils;
import tech.jhipster.lite.UnitTest;
import tech.jhipster.lite.module.domain.JHipsterModule;
import tech.jhipster.lite.module.domain.JHipsterModulesFixture;
import tech.jhipster.lite.module.domain.properties.JHipsterModuleProperties;

@UnitTest
class DevToolsModuleFactoryTest {

  private static final DevToolsModuleFactory factory = new DevToolsModuleFactory();

  @Test
  void shouldBuildModule() {
    JHipsterModuleProperties properties = JHipsterModulesFixture.propertiesBuilder(TestFileUtils.tmpDirForTest())
      .basePackage("tech.jhipster.jhlitest")
      .projectBaseName("myapp")
      .build();

    JHipsterModule module = factory.buildModule(properties);

    assertThatModuleWithFiles(module, pomFile())
      .hasPrefixedFiles("documentation", "dev-tools.md")
      .hasFile("pom.xml")
      .containing(
        """
            <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-devtools</artifactId>
              <scope>runtime</scope>
              <optional>true</optional>
            </dependency>
        """
      )
      .and()
      .hasFile("src/main/resources/config/application.yml")
      .containing(
        """
        spring:
          devtools:
            livereload:
              enabled: false
            restart:
              enabled: false
        """
      )
      .and()
      .hasFile("src/main/resources/config/application-local.yml")
      .containing(
        """
        spring:
          devtools:
            livereload:
              enabled: true
            restart:
              enabled: true
        """
      );
  }
}
