Feature: LangChain4j

  Scenario: Should add Spring Boot LangChain4j Starter
    When I apply modules to default project
      | maven-java  |
      | spring-boot |
      | langchain4j |
    Then I should have entries in "src/main/resources/config/application.yml"
      | open-ai     |
      | langchain4j |

  Scenario: Should add Spring Boot LangChain4j Sample
    When I apply "spring-boot-langchain4j-sample" module to default project with maven file
      | packageName | tech.jhipster.chips |
      | baseName    | jhipster            |
    Then I should have files in "src/main/java/tech/jhipster/chips/sample/infrastructure/primary"
      | ChatResource.java |
