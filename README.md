# Ethereum Transaction Exporter (`eth.tx.exporter`)

## 📌 Overview
`eth.tx.exporter` is a **Spring Boot-based microservice** that fetches and exports transaction data for a given Ethereum wallet address.  
It integrates with blockchain APIs, processes transactions, and exposes them via REST endpoints in a clean, structured format.  

**Key Features:**
- Fetch Ethereum transactions for a given wallet address  
- Supports filtering by blockchain type (currently Ethereum)  
- REST API for triggering export  
- Spring Boot Java backend — easy to run locally or in Docker  

## 🚀 Getting Started

### Prerequisites
Make sure you have:
- **Java 21+** (check with `java -version`)
- **Gradle 7+** (check with `gradle -v`) — optional if using Gradle Wrapper  
- Internet access to fetch blockchain data
  
---

### Clone the Repository
Edit src/main/resources/application.properties with your blockchain API keys if required:
```
git clone https://github.com/<your-username>/eth.tx.exporter.git
cd eth.tx.exporter
```

### Configure Application Properties
Edit application.properties with your blockchain API keys if required:

spring.application.name=eth.tx.exporter
alchemy.api.base-url=https://eth-mainnet.alchemyapi.io/v2/<Your-Alchemy-key>

### Build the Project
Using Gradle Wrapper (recommended):

Use Application.java and run it locally the springboot server will start
gradle clean build

### Run Locally (Spring Boot)
Using Gradle Wrapper:

./gradlew bootRun
Or run the compiled JAR:

java -jar build/libs/eth.tx.exporter-0.0.1-SNAPSHOT.jar
Spring Boot will start on http://localhost:8080 by default.

### 📡 API Usage
Export Transactions (Ethereum)
Endpoint:

GET /api/transactions/export
Query Parameters:

Name	Required	Description
walletAddress	✅	Ethereum wallet address to fetch transactions for
blockchain	✅	Blockchain name (ETHEREUM)

Example curl:
curl --location 'http://localhost:8080/api/transactions/export?walletAddress=0xd620AADaBaA20d2af700853C4504028cba7C3333&blockchain=ETHEREUM'

### 🛠 Development Notes
This project uses Spring Boot — Application.java is the main entry point.

Hot reload is possible with Spring DevTools.

To run on a different port, update server.port in application.yml.



Assumptions
Only Ethereum Mainnet is supported in this version.

Requires valid API key for external blockchain provider (Alchemy).

All transactions are fetched live from the provider — no internal cache for now.

Architecture Decisions
Spring Boot chosen for quick REST API setup and production-readiness.

Gradle build for flexible dependency management.

Externalized API keys for security and portability.

-----------------------------------------------------------

### 💡 Bonus: Scalability & Complex Transaction Handling
### Large-Scale Transaction Storage

Architecture

Ingestion: Kafka / AWS Kinesis for asynchronous fetch

Storage:
Hot: MongoDB / DynamoDB (indexed queries)
Cold: AWS S3 in Parquet (cheap archival)
Indexes: (walletAddress, blockchain, timestamp DESC) and txHash


### Trade-Offs for Complex Transactions (e.g., Uniswap Add Liquidity)
Challenges

Multiple internal calls across smart contracts
Complex state changes and token movements

Approaches
Raw logs -> only	Full fidelity -> Requires heavy client parsing
Parsed summaries ->	Easy to consume	-> Loses raw detail
Hybrid ✅	-> Best of both	 -> Higher storage & processing cost
