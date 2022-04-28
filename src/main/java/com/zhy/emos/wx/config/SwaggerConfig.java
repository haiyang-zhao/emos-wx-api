package com.zhy.emos.wx.config;


import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title("EMOS在线办公系统");

        ApiInfo apiInfo = builder.build();
        docket.apiInfo(apiInfo);

        //那些类哪些方法添加到Swagger
        ApiSelectorBuilder selectorBuilder = docket.select();
        //所有包
        selectorBuilder.paths(PathSelectors.any());
        //ApiOperation注解的方法
        selectorBuilder.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));

        docket = selectorBuilder.build();

        //让Swagger支持JWT

        //header中的token
        ApiKey apiKey = new ApiKey("token", "token", "header");
        List<ApiKey> keys = new ArrayList<>();
        keys.add(apiKey);
        docket.securitySchemes(keys);

        //作用域
        AuthorizationScope scope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] scopes = {scope};
        SecurityReference reference = new SecurityReference("token", scopes);

        List<SecurityReference> refLists = new ArrayList<>();
        refLists.add(reference);

        SecurityContext context = SecurityContext.builder().securityReferences(refLists).build();
        List<SecurityContext> ctxLists = new ArrayList<>();
        ctxLists.add(context);
        docket.securityContexts(ctxLists);

        return docket;
    }
}
