/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement())
                .components(components());
    }

    private Info apiInfo() {
        return new Info()
                .title("Carp API文档")
                .description("Carp API文档")
                .version("0.0.1-SNAPSHOT")
                .termsOfService("https://github.com/flowerfine/carp")
                .license(new License().name("Apache 2.0").url("https://github.com/flowerfine/carp/blob/dev/LICENSE"))
                .contact(contact());

    }

    private Contact contact() {
        Contact kalencaya = new Contact();
        kalencaya.setName("kalencaya");
        kalencaya.setUrl("https://github.com/kalencaya");
        kalencaya.setEmail("1942460489@qq.com");
        return kalencaya;
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("u_token");
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes("u_token", securityScheme());
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("u_token")
                .type(SecurityScheme.Type.HTTP);
    }
}
