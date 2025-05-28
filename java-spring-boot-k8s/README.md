# 쿠버네티스 환경을 위한 Java Spring Boot 보일러플레이트

이 보일러플레이트는 쿠버네티스(Kubernetes) 환경에서 실행되는 Java Spring Boot 애플리케이션의 기본적인 시작 구조를 제공합니다. 클라우드 네이티브 환경에 적합한 애플리케이션 개발을 위한 주요 고려 사항들을 포함하고 있습니다.

## 📝 주요 특징

* **로깅:** 표준화된 로깅 방식 및 구조화된 로그 형식(JSON 등) 설정 예시를 제공합니다.
* **트레이싱:** 분산 환경에서 요청 흐름을 추적하기 위한 분산 트레이싱 시스템(예: OpenTelemetry, Zipkin/Jaeger) 연동을 위한 기본 설정을 포함합니다.
* **메트릭:** Prometheus 등의 메트릭 수집 시스템과 연동하기 위해 Spring Boot Actuator 기반의 메트릭 엔드포인트 설정을 제공합니다.
* **헬스 프로브:** 쿠버네티스의 Readiness 및 Liveness 프로브가 애플리케이션 상태를 정확히 판단할 수 있도록 Actuator 헬스 엔드포인트를 구성합니다.
* **Graceful Shutdown:** 애플리케이션 종료 시 진행 중인 요청을 안전하게 완료하고 리소스를 정상적으로 해제하도록 Graceful Shutdown 설정을 포함합니다.
* **설정 관리:** 쿠버네티스 ConfigMap 및 Secret을 애플리케이션 설정으로 주입받는 방안에 대한 예시를 제공합니다. (Spring Cloud Kubernetes 등을 활용 가능)
* **효율적인 Docker 이미지:** Multi-stage 빌드를 활용하여 최종 이미지 크기를 줄이고, non-root 사용자로 애플리케이션을 실행하여 보안을 고려한 `Dockerfile` 예시를 제공합니다.

## 🛠️ 사전 요구 사항

* Java JDK 17 이상
* Maven 3.8.x 이상 또는 Gradle 7.x 이상 (프로젝트에 설정된 빌드 도구 기준)
* Docker (이미지 빌드 및 로컬 컨테이너 환경 테스트용)
* kubectl (쿠버네티스 클러스터와 상호작용용)
* (선택 사항) Minikube, kind, Docker Desktop Kubernetes 등 로컬 쿠버네티스 개발 환경

## 🚀 프로젝트 시작하기

1.  이 보일러플레이트 코드를 로컬 환경으로 복제(clone)하거나 다운로드합니다.
2.  사용하시는 IDE(IntelliJ IDEA, Eclipse, VS Code 등)에서 프로젝트를 엽니다.
3.  필요한 경우, IDE의 프로젝트 SDK 및 빌드 도구 설정을 확인하고 동기화합니다.

## ⚙️ 설정 (Configuration)

* 애플리케이션의 주요 설정은 `src/main/resources/application.yml` (또는 `application.properties`) 파일에서 관리합니다.
* 쿠버네티스 환경을 위한 프로파일(예: `k8s`)을 사용하여 환경별 설정을 분리할 수 있도록 예시를 제공합니다.
* `ConfigMap`이나 `Secret`으로부터 주입되는 외부 설정은 Spring Boot의 `@Value` 어노테이션이나 `@ConfigurationProperties`를 통해 사용할 수 있도록 구성합니다.

## 💻 로컬 개발 및 빌드

* **로컬 환경에서 실행:**
    * Maven 사용 시: `./mvnw spring-boot:run`
    * Gradle 사용 시: `./gradlew bootRun`
    * 또는 IDE의 실행 기능을 사용합니다.
* **프로젝트 빌드 (JAR 파일 생성):**
    * Maven 사용 시: `./mvnw clean package`
    * Gradle 사용 시: `./gradlew clean build` (또는 `assemble`)

## 🐳 Docker 이미지 빌드

프로젝트 루트 디렉토리에 포함된 `Dockerfile`을 사용하여 애플리케이션의 Docker 이미지를 빌드할 수 있습니다.

```bash
docker build -t your-image-name:your-tag .
```

제공되는 Dockerfile은 multi-stage 빌드를 적용하여 최종 이미지의 크기를 최적화하며, non-root 사용자로 애플리케이션을 실행하도록 구성되어 있습니다.
