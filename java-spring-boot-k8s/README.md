# 쿠버네티스 환경을 위한 Java Spring Boot 보일러플레이트

## 1. 개요

이 보일러플레이트는 쿠버네티스(Kubernetes) 환경에 배포될 Java Spring Boot 애플리케이션의 시작점으로 사용될 수 있도록 기본적인 구조와 설정을 제공합니다. 주요 목표는 다음과 같습니다:

* 쿠버네티스 환경에서의 설정 관리 용이성 제공 (Spring Cloud Kubernetes 활용)
* 옵저버빌리티(관찰 가능성) 확보를 위한 기반 설정 (메트릭, 로깅, 트레이싱)
* 클라우드 네이티브 애플리케이션의 일반적인 고려 사항 포함

**주요 기술 스택:**

* Spring Boot 3.4.0
* Java 17 (프로젝트 설정 목표, 현재 로컬 실행 시에는 시스템 JDK 사용 가능성 있음)
* Gradle

## 2. 주요 특징

* **쿠버네티스 설정 관리:** Spring Cloud Kubernetes를 사용하여 `ConfigMap` 및 `Secret`으로부터 애플리케이션 설정을 동적으로 로드합니다.
* **옵저버빌리티 (관찰 가능성) 구성:**
    * **메트릭:** Prometheus가 수집할 수 있도록 Spring Boot Actuator의 `/actuator/prometheus` 엔드포인트를 제공합니다.
    * **로깅:** `logstash-logback-encoder`를 사용하여 구조화된 JSON 형식으로 로그를 표준 출력(stdout)하도록 설정되어 있습니다. 이를 통해 Loki, Promtail 등 로그 수집 시스템과의 연동이 용이합니다.
        * 로컬/개발 프로파일(`local`, `dev`)에서는 가독성을 위해 일반 텍스트 로그를 사용하고, 그 외 환경(예: `k8s` 또는 프로파일 미지정 시)에서는 JSON 로그를 출력합니다.
        * MDC를 통해 로그에 `traceId`, `spanId`가 자동으로 포함됩니다.
    * **트레이싱:** OpenTelemetry를 통해 분산 트레이싱을 지원합니다. OTLP/HTTP 프로토콜을 사용하여 OpenTelemetry Collector로 트레이스 데이터를 전송하도록 설정되어 있으며, 정상 작동이 확인되었습니다.
* **Graceful Shutdown:** 애플리케이션 종료 시 안전하게 요청을 처리합니다.
* **REST 컨트롤러 예시:** 간단한 API 엔드포인트(`HelloController`)를 포함하여 애플리케이션 동작을 테스트할 수 있습니다.

## 3. 사전 요구 사항

* Java 17 이상 (프로젝트 빌드 및 실행용)
* Gradle (프로젝트에 포함된 Wrapper 사용 권장)
* Docker (컨테이너 이미지 빌드 및 로컬 컨테이너 테스트용)
* kubectl (쿠버네티스 클러스터와 상호작용용)
* **로컬 테스트 환경 (선택 사항):**
    * k3d 또는 유사한 로컬 쿠버네티스 환경
    * OpenTelemetry Collector (k3d 내 배포, OTLP/HTTP `4318` 포트 및 OTLP/gRPC `4317` 포트 리스너 설정 확인)
    * Prometheus, Grafana, Loki, Tempo (옵저버빌리티 스택 확인용)

## 4. 핵심 설정

### 4.1. `build.gradle` (주요 의존성)

* Spring Boot 버전: `3.4.0`
* Spring Cloud 버전: `ext { springCloudVersion = '2024.0.1' }` (Spring Boot 3.4.0과 호환되는 버전으로 사용자가 설정, 공식 문서 확인 권장)
* 주요 라이브러리:
    * `org.springframework.boot:spring-boot-starter-web`
    * `org.springframework.boot:spring-boot-starter-actuator`
    * `org.projectlombok:lombok`
    * **메트릭:** `io.micrometer:micrometer-registry-prometheus`
    * **트레이싱:** `io.micrometer:micrometer-tracing-bridge-otel`, `io.opentelemetry:opentelemetry-exporter-otlp`
    * **로깅:** `net.logstash.logback:logstash-logback-encoder:7.4`
    * **쿠버네티스 연동:** `org.springframework.cloud:spring-cloud-starter-kubernetes-client-config`

### 4.2. `src/main/resources/application.yml` (주요 설정)

* **`spring.application.name`**: 애플리케이션 이름을 지정합니다 (예: `java-spring-boot-k8s`).
* **`spring.config.import: "optional:kubernetes:"`**: Spring Cloud Kubernetes를 통한 외부 설정 로드를 활성화합니다.
* **`spring.cloud.kubernetes.config.sources`**: 로드할 `ConfigMap` 이름을 지정합니다 (애플리케이션 이름 기반).
* **`server.port: 8080`**, **`server.shutdown: graceful`**: 서버 설정.
* **`management.endpoints.web.exposure.include`**: `health`, `info`, `prometheus`, `metrics` Actuator 엔드포인트를 노출합니다.
* **`management.endpoint.health.probes.enabled: true`**: 쿠버네티스 프로브 지원.
* **`management.metrics.tags.application`**: 모든 메트릭에 애플리케이션 이름 태그를 추가합니다.
* **`management.otlp.metrics.export.enabled: false`**: OTLP를 통한 메트릭 푸시는 비활성화합니다 (Prometheus scrape 방식 주력).
* **`management.otlp.tracing.endpoint`**: OTLP 트레이스 전송 엔드포인트.
    * 쿠버네티스 환경: `ConfigMap` (키 예: `OTEL_COLLECTOR_ENDPOINT`)을 통해 실제 Collector의 OTLP/HTTP 주소(예: `http://otel-collector-opentelemetry-collector.tracing.svc.cluster.local:4318/v1/traces`)를 주입받도록 설정합니다.
    * 로컬 테스트용 기본값: `http://localhost:4318/v1/traces` (OTLP/HTTP, Collector가 해당 경로로 리스닝해야 함).
* **`management.tracing.sampling.probability: 1.0`**: 트레이스 샘플링 비율을 100%로 설정합니다.

### 4.3. `src/main/resources/logback-spring.xml`

* `logstash-logback-encoder`를 사용하여 구조화된 JSON 로그 출력을 설정합니다.
* Spring Profile (`local`, `dev` 등)에 따라 텍스트 또는 JSON 형식으로 로그 출력을 전환하여 로컬 개발 편의성과 운영 환경 로그 분석 용이성을 모두 제공합니다.
* MDC (`traceId`, `spanId`)를 통해 분산 트레이싱 정보를 로그에 포함합니다.

### 4.4. `k8s/configmap.yml` (예시)

쿠버네티스 환경에서 애플리케이션에 제공될 `ConfigMap` 예시입니다.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: java-spring-boot-k8s # application.yml의 spring.application.name과 일치
data:
  OTEL_COLLECTOR_ENDPOINT: "[http://otel-collector-opentelemetry-collector.tracing.svc.cluster.local:4318/v1/traces](http://otel-collector-opentelemetry-collector.tracing.svc.cluster.local:4318/v1/traces)"
  # 다른 필요한 설정들 (예: LOGGING_LEVEL_COM_EXAMPLE_BOILERPLATE: "DEBUG")
```

## 5. 로컬 실행 및 테스트

1.  **k3d 및 OpenTelemetry Collector 준비 (로컬 테스트 시):**
    * k3d 클러스터를 실행하고 OpenTelemetry Collector를 배포합니다.
    * Collector가 OTLP/HTTP 요청을 `4318` 포트에서 수신하고, `/v1/traces` 경로를 처리하도록 설정되어 있는지 확인합니다.
    * `kubectl port-forward service/otel-collector-opentelemetry-collector -n tracing 4318:4318` (실제 서비스명과 네임스페이스에 맞게 수정) 명령으로 로컬 `4318` 포트를 Collector 서비스로 포워딩합니다.
2.  **애플리케이션 실행:**
    * `./gradlew bootRun`
    * 로컬에서 가독성 있는 텍스트 로그를 보려면: `./gradlew bootRun --args='--spring.profiles.active=local'`
3.  **엔드포인트 테스트:** `/hello`, `/app-info`, `/actuator/prometheus`, `/actuator/health` 등.
4.  **로그 및 트레이스 확인:** 애플리케이션 콘솔 로그 및 Tempo/Jaeger 등 트레이싱 백엔드 UI에서 확인합니다.

## 6. (추후 진행) Dockerfile 및 Kubernetes 매니페스트

* **`Dockerfile`**: 애플리케이션 컨테이너화를 위한 Dockerfile 작성 예정.
* **`k8s/` 디렉토리**: `Deployment.yml`, `Service.yml` 등 표준 쿠버네티스 배포 매니페스트 예시 포함 예정.

## 7. 개발 노트 및 현재 상태

* **OTLP 트레이싱:** 현재 OTLP/HTTP 프로토콜 (`4318` 포트, `/v1/traces` 경로 사용)을 통해 OpenTelemetry Collector와 안정적으로 연동되는 것을 확인했습니다. (초기 gRPC 방식 연동 시도 중에는 Spring Boot 자동 설정 관련 이슈로 HTTP 방식으로 우선 안정화했습니다.)
* **Java Toolchain:** `build.gradle`에 Java 17 사용이 명시되어 있으나, 로컬 실행 시 JDK 환경 문제로 주석 처리될 수 있습니다. 일관된 빌드/실행 환경을 위해서는 로컬에 JDK 17을 정확히 설정하거나 Gradle toolchain 자동 다운로드 설정을 구성하는 것이 최종적으로 권장됩니다.
* **로깅 `app_name`:** 현재 JSON 로그의 `app_name` 필드가 HOSTNAME으로 나올 수 있습니다. `application.yml`의 `spring.application.name`을 더 일관되게 반영하려면 Logback 설정 추가 조정 또는 시스템 프로퍼티 사용을 고려할 수 있습니다.

---