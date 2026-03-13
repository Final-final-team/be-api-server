package com.example.workmanagement.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "업무 관리 검토 API",
                version = "v1",
                description = "업무 검토 상신, 승인, 반려, 코멘트, 첨부, 참조자, 추가 검토자 기능을 제공하는 API 문서입니다.",
                contact = @Contact(name = "Final Team"),
                license = @License(name = "Internal")
        ),
        servers = {
                @Server(url = "/", description = "기본 서버")
        }
)
public class OpenApiConfig {
}
