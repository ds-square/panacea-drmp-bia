package org.panacea.drmp.bia.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer guavaModuleCustomizer() {
//        return builder -> {
//            builder.modules(new GuavaModule());
//        };
//    }
}

