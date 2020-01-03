package io.javabrains.moviecatalogservice.config;

import io.javabrains.commonservice.MicroservicesGlobalSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class GlobalSecurity extends MicroservicesGlobalSecurity {


}
