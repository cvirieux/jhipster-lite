package tech.jhipster.lite.generator.setup.license.domain;

import static tech.jhipster.lite.module.domain.JHipsterModule.from;
import static tech.jhipster.lite.module.domain.JHipsterModule.moduleBuilder;
import static tech.jhipster.lite.module.domain.JHipsterModule.to;

import java.time.Year;
import java.time.ZoneId;
import tech.jhipster.lite.module.domain.JHipsterModule;
import tech.jhipster.lite.module.domain.file.JHipsterSource;
import tech.jhipster.lite.module.domain.properties.JHipsterModuleProperties;

public class LicenseModuleFactory {

  private static final JHipsterSource SOURCE = from("setup").append("license");

  public JHipsterModule buildMitModule(JHipsterModuleProperties properties) {
    // @formatter:off
    return moduleBuilder(properties)
      .context()
        .put("currentYear", Year.now(ZoneId.systemDefault()).getValue())
        .and()
      .files()
        .add(SOURCE.template("MIT.txt"), to("LICENSE.txt"))
        .and()
      .build();
    // @formatter:on
  }

  public JHipsterModule buildApacheModule(JHipsterModuleProperties properties) {
    // @formatter:off
    return moduleBuilder(properties)
      .files()
        .add(SOURCE.append("Apache.txt"), to("LICENSE.txt"))
        .and()
      .build();
    // @formatter:on
  }
}
