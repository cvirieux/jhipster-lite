package tech.jhipster.lite.generator.server.springboot.apidocumentation.springdocoauth2.infrastructure.primary;

import static tech.jhipster.lite.shared.slug.domain.JHLiteFeatureSlug.AUTHENTICATION_SPRINGDOC;
import static tech.jhipster.lite.shared.slug.domain.JHLiteFeatureSlug.SPRINGDOC;
import static tech.jhipster.lite.shared.slug.domain.JHLiteModuleSlug.SPRINGDOC_OAUTH_2;
import static tech.jhipster.lite.shared.slug.domain.JHLiteModuleSlug.SPRING_BOOT_OAUTH_2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.lite.generator.server.springboot.apidocumentation.springdocoauth2.application.SpringdocOauth2ApplicationService;
import tech.jhipster.lite.module.domain.resource.JHipsterModuleOrganization;
import tech.jhipster.lite.module.domain.resource.JHipsterModulePropertiesDefinition;
import tech.jhipster.lite.module.domain.resource.JHipsterModuleResource;

@Configuration
class SpringdocOauth2ModuleConfiguration {

  @Bean
  JHipsterModuleResource springdocOAuth2Module(SpringdocOauth2ApplicationService springdocOauth2) {
    return JHipsterModuleResource.builder()
      .slug(SPRINGDOC_OAUTH_2)
      .propertiesDefinition(
        JHipsterModulePropertiesDefinition.builder().addBasePackage().addIndentation().addSpringConfigurationFormat().build()
      )
      .apiDoc("Spring Boot - API Documentation", "Add OAuth2 authentication for springdoc")
      .organization(
        JHipsterModuleOrganization.builder()
          .feature(AUTHENTICATION_SPRINGDOC)
          .addDependency(SPRINGDOC)
          .addDependency(SPRING_BOOT_OAUTH_2)
          .build()
      )
      .tags("server", "swagger", "springdoc")
      .factory(springdocOauth2::buildModule);
  }
}
