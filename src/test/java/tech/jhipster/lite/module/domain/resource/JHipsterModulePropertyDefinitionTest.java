package tech.jhipster.lite.module.domain.resource;

import static org.assertj.core.api.Assertions.*;
import static tech.jhipster.lite.module.domain.resource.JHipsterModulePropertyDefinition.*;

import org.junit.jupiter.api.Test;
import tech.jhipster.lite.UnitTest;

@UnitTest
class JHipsterModulePropertyDefinitionTest {

  @Test
  void shouldHaveMeaningfullToString() {
    assertThat(projectNameProperty()).hasToString(
      """
      JHipsterModulePropertyDefinition[type=STRING,key=projectName,mandatory=true,description=Project full name,defaultValue=JHipster Sample Application,order=-200]\
      """
    );
  }
}
