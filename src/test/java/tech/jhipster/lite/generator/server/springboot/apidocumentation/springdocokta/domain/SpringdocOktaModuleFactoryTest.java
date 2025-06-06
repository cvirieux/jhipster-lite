package tech.jhipster.lite.generator.server.springboot.apidocumentation.springdocokta.domain;

import static tech.jhipster.lite.module.infrastructure.secondary.JHipsterModulesAssertions.assertThatModuleWithFiles;

import org.junit.jupiter.api.Test;
import tech.jhipster.lite.TestFileUtils;
import tech.jhipster.lite.UnitTest;
import tech.jhipster.lite.module.domain.JHipsterModule;
import tech.jhipster.lite.module.domain.JHipsterModulesFixture;
import tech.jhipster.lite.module.domain.properties.JHipsterModuleProperties;

@UnitTest
class SpringdocOktaModuleFactoryTest {

  private static final SpringdocOktaModuleFactory factory = new SpringdocOktaModuleFactory();

  @Test
  void shouldBuildModule() {
    JHipsterModuleProperties properties = JHipsterModulesFixture.propertiesBuilder(TestFileUtils.tmpDirForTest())
      .basePackage("tech.jhipster.jhlitest")
      .put("oktaDomain", "dev-123456.okta.com")
      .put("oktaClientId", "my-client-id")
      .build();

    JHipsterModule module = factory.buildModule(properties);

    // @formatter:off
    assertThatModuleWithFiles(module)
      .hasFile("src/main/resources/config/application-okta.yml")
      .containing(
        """
        springdoc:
          oauth2:
            authorization-url: https://dev-123456.okta.com/oauth2/default/v1/authorize?nonce="jhipster"
          swagger-ui:
            oauth:
              client-id: my-client-id
              realm: jhipster
              scopes:
              - openid
              - profile
              - email
        """
      );
    // @formatter:on
  }
}
