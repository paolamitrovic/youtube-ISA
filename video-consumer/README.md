# Video Consumer Application

## Opis

Ova aplikacija je **odvojena Spring Boot aplikacija** koja prima poruke iz RabbitMQ queue-a kada se objavi novi video na Jutjubić platformi.

## Struktura

- **VideoConsumerApplication.java** - Main klasa aplikacije
- **MessageConsumer.java** - Prima poruke iz queue-a (JSON i Protobuf)
- **UploadEvent.java** - Model za upload event
- **RabbitMQConfig.java** - Konfiguracija RabbitMQ-a
- **BenchmarkingService.java** - Servis za poređenje JSON vs Protobuf
- **BenchmarkController.java** - REST endpoint za pokretanje benchmark-a

## Instalacija i Pokretanje

### 1. Build Aplikacije

```bash
cd video-consumer
mvn clean install
```

### 2. Pokretanje

```bash
mvn spring-boot:run
```

Ili:
```bash
java -jar target/video-consumer-0.0.1-SNAPSHOT.jar
```

Aplikacija će se pokrenuti na portu **8081** (različit od glavne backend aplikacije koja je na 8080).

### 3. Provera da li radi

Kada se kreira novi video u glavnoj aplikaciji, ova aplikacija će automatski primiti poruke i prikazati ih u logovima.

## Konfiguracija

Konfiguracija se nalazi u `src/main/resources/application.properties`:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
server.port=8081
```

## Benchmark

Za pokretanje benchmark poređenja:
```bash
curl -X POST http://localhost:8081/benchmark/run
```

## Napomene

- Ova aplikacija mora biti pokrenuta **pored** glavne backend aplikacije
- Obe aplikacije koriste isti RabbitMQ server
- Glavna aplikacija (backend) **šalje** poruke
- Ova aplikacija (video-consumer) **prima** poruke
