# Message Queue Implementation - JSON vs Protobuf

## Pregled

Implementirana je message queue funkcionalnost koja šalje poruke kada se kreira novi video. Poruke se šalju u dva formata:
- **JSON** format
- **Protobuf** format

## Arhitektura

Sistem se sastoji od **dve odvojene aplikacije**:

1. **Backend aplikacija** (`backend/`) - Glavna aplikacija koja:
   - Prima zahteve za kreiranje videa
   - Šalje poruke u RabbitMQ queue (JSON i Protobuf)

2. **Video Consumer aplikacija** (`video-consumer/`) - Odvojena aplikacija koja:
   - Prima poruke iz RabbitMQ queue-a
   - Obrađuje upload event-e
   - Može da pokrene benchmark poređenje

## Komponente

### Backend Aplikacija (`backend/`)

1. **UploadEvent Model**
   - `mq/UploadEvent.java` - Java klasa koja predstavlja event strukturu
   - `proto/upload_event.proto` - Protobuf definicija za serijalizaciju

2. **Message Producer**
   - `mq/MessageProducer.java` - Servis koji šalje poruke u oba formata kada se kreira video

3. **RabbitMQ Configuration**
   - `mq/RabbitMQConfig.java` - Konfiguracija za RabbitMQ
   - Dve queue-e: `video.upload.json` i `video.upload.protobuf`

4. **Benchmarking Service** (opciono)
   - `mq/BenchmarkingService.java` - Servis koji poredi performanse JSON vs Protobuf
   - `controller/BenchmarkController.java` - REST endpoint za pokretanje benchmark-a

### Video Consumer Aplikacija (`video-consumer/`)

1. **Message Consumer**
   - `mq/MessageConsumer.java` - Komponenta koja prima i obrađuje poruke iz queue-a
   - Prima poruke u oba formata (JSON i Protobuf)

2. **UploadEvent Model** (kopija iz backend-a)
   - `mq/UploadEvent.java`
   - `proto/upload_event.proto`

3. **RabbitMQ Configuration**
   - `mq/RabbitMQConfig.java` - Konfiguracija za RabbitMQ

4. **Benchmarking Service**
   - `mq/BenchmarkingService.java` - Servis koji poredi performanse JSON vs Protobuf
   - `controller/BenchmarkController.java` - REST endpoint za pokretanje benchmark-a
   - Poređenje se vrši na 50 poruka
   - Meri: prosečno vreme serijalizacije, deserijalizacije i veličinu poruke

## Instalacija i Pokretanje

### 1. Instalacija RabbitMQ

**Windows:**
```bash
# Preuzmite i instalirajte RabbitMQ sa https://www.rabbitmq.com/download.html
# Ili koristite Chocolatey:
choco install rabbitmq
```

**Linux/Mac:**
```bash
# Ubuntu/Debian
sudo apt-get install rabbitmq-server

# Mac
brew install rabbitmq
```

### 2. Pokretanje RabbitMQ

```bash
# Windows
rabbitmq-server

# Linux/Mac
sudo systemctl start rabbitmq-server
# ili
rabbitmq-server
```

RabbitMQ će biti dostupan na `http://localhost:15672` (default username/password: guest/guest)

### 3. Build Projekta

```bash
cd backend
mvn clean install
```

Ovo će automatski generisati Protobuf klase iz `.proto` fajla.

### 4. Konfiguracija

RabbitMQ konfiguracija se nalazi u `application.properties`:
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Korišćenje

### Automatsko Slanje Poruka

Kada se kreira novi video preko API-ja (`POST /videos`), automatski se šalju poruke u oba formata.

### Pokretanje Benchmark-a

**Backend aplikacija:**
```bash
curl -X POST http://localhost:8080/benchmark/run
```

**Video Consumer aplikacija:**
```bash
curl -X POST http://localhost:8081/benchmark/run
```

Rezultati će biti prikazani u logovima aplikacije.

## Struktura UploadEvent

```java
- videoId: Long
- title: String
- description: String
- videoSizeBytes: Long
- authorUsername: String
- authorEmail: String
- createdAt: LocalDateTime
- location: String (optional)
- tags: List<String>
```

## Queue-e

- **video.upload.json** - JSON format poruka
- **video.upload.protobuf** - Protobuf format poruka

## Benchmark Rezultati

Benchmark meri:
1. **Prosečno vreme serijalizacije** (ms)
2. **Prosečno vreme deserijalizacije** (ms)
3. **Prosečna veličina poruke** (bytes)

Rezultati se prikazuju u logovima sa poređenjem između JSON i Protobuf formata.

## Pokretanje Sistema

### 1. Pokrenite RabbitMQ
```bash
# Windows
rabbitmq-server

# Linux/Mac
sudo systemctl start rabbitmq-server
```

### 2. Pokrenite Backend Aplikaciju
```bash
cd backend
mvn spring-boot:run
```
Aplikacija će biti dostupna na `http://localhost:8080`

### 3. Pokrenite Video Consumer Aplikaciju
```bash
cd video-consumer
mvn spring-boot:run
```
Aplikacija će biti dostupna na `http://localhost:8081`

### 4. Testiranje

Kada kreirate novi video preko backend API-ja (`POST /videos`), poruke će biti poslate u queue, a video-consumer aplikacija će ih primiti i prikazati u logovima.

## Napomene

- Protobuf klase se automatski generišu tokom Maven build-a
- Ako RabbitMQ nije pokrenut, backend aplikacija će i dalje raditi, ali poruke neće biti poslate
- Video consumer aplikacija zahteva RabbitMQ da bi radila
- MessageProducer je opcionalan dependency (`@Autowired(required = false)`) tako da backend može raditi i bez RabbitMQ-a
- Obe aplikacije koriste isti RabbitMQ server ali rade na različitim portovima (8080 i 8081)
