# SS05 HW03: Random LoadBalancer cho product-service


## 1. Nội dung bài làm

Dự án mô phỏng hệ thống VietMart gồm:

| Module | Vai trò | Port |
|---|---|---|
| `discovery-server` | Eureka Server | `8761` |
| `product-service` | Service được chạy nhiều instance | `8081`, `8082` |
| `api-gateway` | Gateway gọi `product-service` qua LoadBalancer | `8080` |

Mục tiêu chính là đổi chiến lược cân bằng tải mặc định của Spring Cloud LoadBalancer sang `RandomLoadBalancer` riêng cho `product-service`.

## 2. Cấu hình RandomLoadBalancer

Trong `api-gateway` có class:

```java
@Configuration
public class RandomLoadBalancerConfig {

    @Bean
    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId);
    }
}
```

Áp dụng riêng cho `product-service`:

```java
@SpringBootApplication
@LoadBalancerClient(name = "product-service", configuration = RandomLoadBalancerConfig.class)
public class ApiGatewayApplication {
}
```

Gateway route:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service-route
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
```

## 3. Cách chạy thử

Chạy Eureka:

```bash
./gradlew :discovery-server:bootRun
```

Chạy 2 instance của `product-service`:

```bash
./gradlew :product-service:bootRun --args='--server.port=8081'
./gradlew :product-service:bootRun --args='--server.port=8082'
```

Chạy API Gateway:

```bash
./gradlew :api-gateway:bootRun
```

Gửi liên tục 10 request qua gateway:

```bash
for i in {1..10}; do curl -s http://localhost:8080/api/products/instance; echo; done
```

Nếu máy đang bận port `8081`, `8082` hoặc `8080`, có thể đổi sang port khác khi chạy:

```bash
./gradlew :product-service:bootRun --args='--server.port=18081'
./gradlew :product-service:bootRun --args='--server.port=18082'
./gradlew :api-gateway:bootRun --args='--server.port=18080'
for i in {1..10}; do curl -s http://localhost:18080/api/products/instance; echo; done
```

## 4. Kết quả mong muốn

Kết quả trả về sẽ có port instance xử lý request. Vì dùng `RandomLoadBalancer`, thứ tự không bị ép kiểu `8081, 8082, 8081, 8082` như Round Robin.

Ví dụ:

```text
{"service":"product-service","port":8082}
{"service":"product-service","port":8081}
{"service":"product-service","port":8082}
{"service":"product-service","port":8082}
{"service":"product-service","port":8081}
```

Trong log của từng instance cũng có dòng:

```text
product-service instance port 8081 handled GET /api/products/instance
product-service instance port 8082 handled GET /api/products/instance
```

## 5. Minh chứng

Thư mục `screenshots/` chứa kết quả curl 10 request và ảnh tóm tắt phân phối request qua `RandomLoadBalancer`.
