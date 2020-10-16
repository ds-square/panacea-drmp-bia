package org.panacea.drmp.bia.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI ageOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("BIA REST API")
						.description("REST API for Business Impact Analysis Engine (BIA)")
						.version("1.0.0")
						.license(new License().name("License: LGPL v3.0").url("https://www.gnu.org/licenses/lgpl-3.0.html")));

	}

}
