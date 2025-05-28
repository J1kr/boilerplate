package com.example.boilerplate.javaspringbootk8s.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    // SLF4J 로거 생성
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    // application.yml 또는 ConfigMap에서 spring.application.name 값을 주입받음
    // 값이 없을 경우 기본값으로 "default-app-name" 사용
    @Value("${spring.application.name:default-app-name}")
    private String applicationName;

    /**
     * 간단한 환영 메시지를 반환하는 GET 엔드포인트입니다.
     * 'name' 쿼리 파라미터를 받을 수 있습니다.
     * 
     * @param name 이름 (기본값: "World")
     * @return JSON 형태의 응답 메시지
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(@RequestParam(name = "name", defaultValue = "World") String name) {
        log.info("GET /hello endpoint called with parameter name: {}", name);

        Map<String, String> response = new HashMap<>();
        response.put("greeting", String.format("Hello, %s!", name));
        response.put("from", applicationName);
        response.put("timestamp", java.time.Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * 애플리케이션 기본 정보를 반환하는 GET 엔드포인트입니다.
     * 
     * @return JSON 형태의 애플리케이션 정보
     */
    @GetMapping("/app-info")
    public ResponseEntity<Map<String, Object>> appInfo() {
        log.info("GET /app-info endpoint called");

        Map<String, Object> response = new HashMap<>();
        response.put("applicationName", applicationName);
        response.put("status", "UP"); // 간단한 상태 표시
        response.put("message", "Application is running smoothly!");
        response.put("javaVersion", System.getProperty("java.version"));
        // 필요하다면 더 많은 정보 추가 가능

        return ResponseEntity.ok(response);
    }
}