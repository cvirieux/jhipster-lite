Feature: Webflux

  Scenario: Should apply webflux module
    When I apply modules to default project
      | maven-java                |
      | spring-boot               |
      | spring-boot-webflux-empty |
      | spring-boot-webflux-netty |
    Then I should have "<artifactId>spring-boot-starter-webflux</artifactId>" in "pom.xml"
    Then I should have files in "src/main/java/tech/jhipster/chips/shared/error/infrastructure/primary"
      | FieldErrorDTO.java |
    Then I should have files in "src/main/java/tech/jhipster/chips/wire/jackson/infrastructure/primary"
      | JacksonConfiguration.java |
    Then I should have files in "src/test/java/tech/jhipster/chips/wire/jackson/infrastructure/primary"
      | JacksonConfigurationIT.java |
