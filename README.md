# Midas Core - Project Documentation

## Overview

Midas Core is a Spring Boot application developed for the JPMC Advanced Software Engineering Forage program. It is a financial transaction processing system that:

- Consumes transaction messages from Apache Kafka
- Processes money transfers between users
- Calculates incentives for transactions
- Provides a REST API to query user balances
- Persists data using H2 in-memory database

## Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2.5 |
| Language | Java 17 |
| Database | H2 (in-memory) |
| Messaging | Apache Kafka |
| Build Tool | Maven |
| Testing | JUnit 5, Spring Kafka Test, Testcontainers |

## Project Structure

```
forage-midas/
├── pom.xml                              # Maven build configuration
├── application.yml                      # Application configuration
├── README.md                            # Project documentation
├── mvnw, mvnw.cmd                       # Maven wrapper scripts
├── services/
│   └── transaction-incentive-api.jar   # External incentive API
├── src/
│   ├── main/
│   │   ├── java/com/jpmc/midascore/
│   │   │   ├── MidasCoreApplication.java          # Main Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── BalanceController.java         # REST API for balance queries
│   │   │   ├── component/
│   │   │   │   ├── KafkaListenerComponent.java    # Kafka message consumer
│   │   │   │   └── DatabaseConduit.java           # Database operations helper
│   │   │   ├── entity/
│   │   │   │   ├── UserRecord.java                # JPA entity for users
│   │   │   │   └── TransactionRecord.java         # JPA entity for transactions
│   │   │   ├── foundation/
│   │   │   │   ├── Transaction.java               # Transaction DTO
│   │   │   │   ├── Incentive.java                 # Incentive DTO
│   │   │   │   └── Balance.java                  # Balance DTO
│   │   │   └── repository/
│   │   │       ├── UserRepository.java            # User data access
│   │   │       └── TransactionRepository.java     # Transaction data access
│   │   └── resources/
│   │       └── application.properties             # App configuration
│   └── test/
│       ├── java/com/jpmc/midascore/
│       │   ├── TaskOneTests.java                  # Boot verification test
│       │   ├── TaskTwoTests.java                  # Kafka listener test
│       │   ├── TaskThreeTests.java                # Transaction processing test
│       │   ├── TaskFourTests.java                 # Balance calculation test
│       │   ├── TaskFiveTests.java                 # REST API test
│       │   ├── WaldorfBalanceTest.java            # Debug test
│       │   ├── KafkaProducer.java                 # Test Kafka producer
│       │   ├── FileLoader.java                    # Test file loader
│       │   ├── UserPopulator.java                 # Test user setup
│       │   └── BalanceQuerier.java                # Test balance query client
│       └── resources/
│           └── test_data/                         # Test data files
│               ├── lkjhgfdsa.hjkl                 # User data (name, balance)
│               ├── poiuytrewq.uiop                # Transaction data
│               ├── mnbvcxz.vbnm                   # Transaction data
│               ├── alskdjfh.fhdjsk                # Transaction data
│               └── rueiwoqp.tyruei                # Transaction data
```

## Core Components

### 1. Entities

#### UserRecord (`entity/UserRecord.java`)
JPA entity representing a user in the system.
- **Fields:**
  - `id` (Long): Auto-generated primary key
  - `name` (String): User's name
  - `balance` (float): User's account balance
- **Methods:** Getters and setters for all fields

#### TransactionRecord (`entity/TransactionRecord.java`)
JPA entity representing a completed transaction.
- **Fields:**
  - `id` (Long): Auto-generated primary key
  - `sender` (UserRecord): The sender of the transaction
  - `recipient` (UserRecord): The recipient of the transaction
  - `amount` (float): Transaction amount
  - `incentive` (float): Incentive amount added to transaction

### 2. Foundation (DTOs)

#### Transaction (`foundation/Transaction.java`)
JSON-serializable DTO for incoming transaction messages from Kafka.
- **Fields:**
  - `senderId` (long): ID of the sender
  - `recipientId` (long): ID of the recipient
  - `amount` (float): Amount to transfer

#### Incentive (`foundation/Incentive.java`)
DTO for incentive responses from external API.
- **Fields:**
  - `amount` (float): Incentive amount

#### Balance (`foundation/Balance.java`)
DTO for balance query responses.
- **Fields:**
  - `amount` (float): Account balance

### 3. Repositories

#### UserRepository (`repository/UserRepository.java`)
Spring Data CRUD repository for UserRecord.
- **Methods:**
  - `findById(long id)`: Find user by ID
  - `findByName(String name)`: Find user by name

#### TransactionRepository (`repository/TransactionRepository.java`)
Spring Data CRUD repository for TransactionRecord.

### 4. Components

#### KafkaListenerComponent (`component/KafkaListenerComponent.java`)
Main business logic component that processes Kafka messages.
- Listens to `midas-transactions-topic` Kafka topic
- **Processing Logic:**
  1. Validates sender and recipient exist
  2. Validates sender has sufficient balance
  3. Calls external incentive API at `http://localhost:8080/incentive`
  4. Deducts amount from sender's balance
  5. Adds amount + incentive to recipient's balance
  6. Saves transaction record to database
- Uses `@Transactional` for atomic operations

#### DatabaseConduit (`component/DatabaseConduit.java`)
Simple wrapper for database operations.

### 5. Controller

#### BalanceController (`controller/BalanceController.java`)
REST API controller for balance queries.
- **Endpoint:** `GET /balance?userId={userId}`
- **Response:** `Balance` object with amount
- **Returns:** 404 if user not found, 200 with balance if found

## Application Configuration

### application.properties

```properties
# Kafka topic configuration
general.kafka-topic=midas-transactions-topic

# Kafka producer serialization
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# H2 Database configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# JPA/Hibernate configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console enabled for debugging
spring.h2.console.enabled=true

# REST API server port
server.port=33400
```

## Test Data Files

### User Data (`lkjhgfdsa.hjkl`)
Format: `name, balance`
```
bernie, 1200.23
grommit, 2215.37
maria, 2774.14
mario, 12.34
waldorf, 444.55
whosit, 888.90
whatsit, 777.60
howsit, 68.70
wilbur, 3476.21
antonio, 2121.54
calypso, 779421.33
```

### Transaction Data Files
Format: `senderId, recipientId, amount`
Each file contains 22 transactions to process.

## Test Cases

### TaskOneTests
Verifies the Spring Boot application starts successfully.

### TaskTwoTests
Tests Kafka message consumption with embedded Kafka broker.

### TaskThreeTests
Tests complete transaction processing flow with user population.

### TaskFourTests
Tests balance calculations after transaction processing (queries Wilbur's balance).

### TaskFiveTests
Tests the REST API endpoint by querying balances for multiple users.

### WaldorfBalanceTest
Utility test for debugging Waldorf's balance.

## Build and Run

### Prerequisites
- Java 17
- Maven

### Build
```bash
./mvnw clean package
```

### Run Tests
```bash
./mvnw test
```

### Run Application
```bash
./mvnw spring-boot:run
```

The application will start on port 33400.

## External Dependencies

### Transaction Incentive API
The application relies on an external incentive API running at `http://localhost:8080/incentive`. This API receives a Transaction object and returns an Incentive object with the incentive amount.

## Database Schema

The H2 database automatically creates tables based on JPA entities:

### USER_RECORD
| Column | Type | Constraints |
|--------|------|-------------|
| ID | BIGINT | PRIMARY KEY |
| NAME | VARCHAR(255) | NOT NULL |
| BALANCE | FLOAT | NOT NULL |

### TRANSACTION_RECORD
| Column | Type | Constraints |
|--------|------|-------------|
| ID | BIGINT | PRIMARY KEY |
| SENDER_ID | BIGINT | FOREIGN KEY -> USER_RECORD |
| RECIPIENT_ID | BIGINT | FOREIGN KEY -> USER_RECORD |
| AMOUNT | FLOAT | NOT NULL |
| INCENTIVE | FLOAT | NOT NULL |

## Flow Diagram

```
[Kafka Producer] --> [Kafka Topic: midas-transactions-topic]
                                    |
                                    v
                    [KafkaListenerComponent]
                                    |
                    +---------------+---------------+
                    |               |               |
                    v               v               v
            [Validate]    [Incentive API]    [Database]
             Users          (localhost:8080)    (H2)
                    |               |               |
                    +---------------+---------------+
                                    |
                                    v
                        [Update Balances]
                        [Save Transaction]
```

## Key Features

1. **Kafka Integration**: Consumes transactions from Kafka topic
2. **Transaction Processing**: Validates and processes money transfers
3. **Incentive System**: Integrates with external API for transaction incentives
4. **REST API**: Provides HTTP endpoint for balance queries
5. **Data Persistence**: Stores users and transactions in H2 database
6. **Testing Support**: Includes embedded Kafka and comprehensive test cases
