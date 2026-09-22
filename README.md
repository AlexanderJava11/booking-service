# 🏨 Villa Avougjagi – Microservices Booking System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5)
![JWT](https://img.shields.io/badge/Security-JWT-black)

Villa Avougjagi är ett bokningssystem för ett pensionat, utvecklat i **Java och Spring Boot**.

Projektet började som en monolit i Backend 1 och har i Backend 2 byggts om till en **microservice-arkitektur** där ansvar, data och databaser är separerade mellan flera självständiga tjänster.

Systemet består av:

- **Booking Service** – bokningar, rum och webbgränssnitt
- **Customer Service** – kundhantering och JWT-login
- **Review Service** – recensioner
- **Tre separata MySQL-databaser**
- **REST-kommunikation mellan tjänsterna**
- **JWT-autentisering för skyddade API-endpoints**
- **Docker & Docker Compose**
- **Kubernetes**
- **Railway deployment**

---

# 🧩 Arkitektur

```text
                         ┌─────────────────────────┐
                         │        Användare        │
                         │        Webbläsare       │
                         └────────────┬────────────┘
                                      │
                                      │ HTTP
                                      ▼
                         ┌─────────────────────────┐
                         │     Booking Service     │
                         │        Port 8080        │
                         │                         │
                         │ Thymeleaf / Rooms       │
                         │ Bookings / JWT / UI     │
                         └───────┬─────────┬───────┘
                                 │         │
                       REST/JSON │         │ REST/JSON
                                 │         │
                     ┌───────────▼────┐   ┌▼────────────────┐
                     │ Customer       │   │ Review Service   │
                     │ Service        │   │ Port 8082        │
                     │ Port 8081      │   │ Reviews CRUD     │
                     └───────┬────────┘   └────────┬─────────┘
                             │                     │
                             ▼                     ▼
                     ┌───────────────┐     ┌───────────────┐
                     │ customer-db   │     │ review-db     │
                     │    MySQL      │     │    MySQL      │
                     └───────────────┘     └───────────────┘

                              Booking Service
                                     │
                                     ▼
                              ┌───────────────┐
                              │  booking-db   │
                              │     MySQL     │
                              └───────────────┘
```

Varje microservice **äger sin egen data**.

En tjänst får därför inte läsa direkt från en annan tjänsts databas. När information behövs från en annan tjänst sker kommunikationen via **REST/JSON över HTTP**.

Det gör tjänsterna mer fristående och minskar kopplingen mellan deras interna implementationer.

---

# 🚀 Microservices

## 🏨 Booking Service

Booking Service är systemets huvudapplikation och fungerar även som fasad för webbgränssnittet.

**Port:** `8080`

Booking Service ansvarar för:

- rum
- bokningar
- kontroll av lediga rum
- antal gäster
- pris per natt
- antal nätter
- totalpris
- webbgränssnitt med Thymeleaf
- kommunikation med Customer Service
- kommunikation med Review Service
- verifiering av JWT för skyddade API-endpoints

### Rum

Rum innehåller bland annat:

- rumsnummer
- rumstyp
- antal extrasängar
- pris per natt
- kapacitet

Systemet kontrollerar tillgänglighet innan ett rum bokas.

### Bokningar

En bokning innehåller bland annat:

- `customerId`
- rum
- incheckningsdatum
- utcheckningsdatum
- antal gäster

Booking Service lagrar **inte en Customer-entitet**.

Istället lagras kundens ID. När kundinformation behövs hämtas den från Customer Service via REST.

Vid skapande eller ändring av en bokning kontrolleras bland annat att:

1. kunden finns i Customer Service,
2. check-out ligger efter check-in,
3. rummet är tillgängligt under perioden,
4. rummet har kapacitet för antalet gäster.

Booking Service beräknar även antal nätter och bokningens totalpris.

---

## 👤 Customer Service

Customer Service är en separat Spring Boot-applikation.

**Port:** `8081`

Den ansvarar för kunddata och erbjuder ett REST-API för CRUD-operationer.

Exempel:

```http
GET    /api/customers
GET    /api/customers/{id}
POST   /api/customers
PUT    /api/customers/{id}
DELETE /api/customers/{id}
```

Customer Service har en **egen MySQL-databas** och Booking Service har ingen direkt åtkomst till den.

Customer Service ansvarar även för systemets JWT-login.

```http
POST /api/auth/login
```

Vid korrekt inloggning skapas en signerad JWT-token som kan användas mot skyddade endpoints i Booking Service.

### Radering av kund

En kund ska inte kunna tas bort om kunden fortfarande har en aktiv bokning.

Därför frågar Customer Service Booking Service:

```http
GET /api/bookings/customer/{customerId}/active
```

Booking Service svarar:

```text
true
```

eller:

```text
false
```

Om kunden har en aktiv bokning stoppas raderingen.

Detta är ett exempel på kommunikation mellan två microservices utan att tjänsterna läser direkt från varandras databaser.

---

## ⭐ Review Service

Review Service är systemets tredje microservice och är implementerad som en del av VG-funktionaliteten.

**Port:** `8082`

Den ansvarar för recensioner och har:

- egen Spring Boot-applikation
- Controller
- Service
- Repository
- Entity
- REST-API
- egen MySQL-databas
- Dockerfile
- Kubernetes Deployment
- Kubernetes Service

Exempel på endpoints:

```http
GET    /api/reviews
GET    /api/reviews/{id}
POST   /api/reviews
PUT    /api/reviews/{id}
DELETE /api/reviews/{id}
```

Booking Service kommunicerar med Review Service genom `ReviewClient`.

Webbgränssnittet för recensioner nås via:

```text
/reviews
```

---

# 🔄 Kommunikation mellan tjänsterna

Systemet använder synkron **REST-kommunikation över HTTP**.

```text
Booking Service
      │
      ├──── REST + Bearer JWT ────> Customer Service
      │
      └──── REST ─────────────────> Review Service


Customer Service
      │
      └──── REST + Bearer JWT ────> Booking Service
                                     kontroll av aktiva bokningar
```

### Booking → Customer

Booking Service använder `CustomerClient` för att bland annat:

- hämta kunder
- hämta en specifik kund
- skapa kunder från webbgränssnittet
- uppdatera kunder
- radera kunder
- kontrollera att en kund finns innan en bokning sparas

Vid service-to-service-kommunikation skickas JWT i HTTP-headern:

```http
Authorization: Bearer <JWT_TOKEN>
```

### Booking → Review

Booking Service använder `ReviewClient` för att:

- lista recensioner
- hämta recension
- skapa recension
- uppdatera recension
- radera recension

### Customer → Booking

Customer Service använder `BookingClient` för att kontrollera om kunden har en aktiv bokning innan kunden får raderas.

Även detta service-to-service-anrop skickar JWT som Bearer-token.

---

# 🛡️ Felhantering mellan microservices

Eftersom microservices kan vara tillgängliga eller otillgängliga oberoende av varandra måste systemet kunna hantera kommunikationsfel.

REST-klienterna använder därför timeouts och felhantering.

Ett viktigt exempel är radering av kunder.

Om Customer Service inte kan kontakta Booking Service går det inte att säkert avgöra om kunden har en aktiv bokning.

Systemet ska därför inte bara anta att kunden kan tas bort.

Vid ett sådant kommunikationsproblem stoppas operationen och ett fel returneras istället.

Det skyddar systemets data från att exempelvis en kund med en aktiv bokning raderas bara för att en annan microservice tillfälligt ligger nere.

---

# 🔐 JWT Authentication

Som en del av VG-funktionaliteten används **JSON Web Token (JWT)**.

JWT används för att skydda de endpoints i Booking Service som ändrar bokningsdata.

## Login i Customer Service

Login hanteras av **Customer Service**:

```http
POST /api/auth/login
```

Användaren skickar användarnamn och lösenord till Customer Service.

Om uppgifterna är korrekta skapar `JwtService` en signerad JWT-token som returneras till klienten.

Token har en begränsad giltighetstid.

## Bearer-token

Token skickas därefter i HTTP-headern:

```http
Authorization: Bearer <JWT_TOKEN>
```

Booking Services `JwtAuthenticationFilter` läser token från requesten och verifierar den med hjälp av `JwtService`.

Customer Service och Booking Service använder samma JWT-secret så att en token som skapas av Customer Service kan verifieras av Booking Service.

## Skyddade Booking-endpoints

Endpoints som ändrar bokningsdata kräver giltig JWT.

Exempel:

```http
POST   /api/bookings
PUT    /api/bookings/{id}
DELETE /api/bookings/{id}
```

Om en sådan request skickas utan giltig JWT returneras `403 Forbidden`.

Med en giltig token från Customer Service tillåts operationen.

JWT-flödet har verifierats både lokalt med Docker Compose och i Railway:

```text
Customer Service
      │
      │ POST /api/auth/login
      ▼
   JWT-token
      │
      │ Authorization: Bearer <JWT_TOKEN>
      ▼
Booking Service
      │
      ▼
Skyddad bokningsoperation
```

## JWT mellan tjänster

När Booking Service anropar Customer Service skickas en Bearer-token.

När Customer Service anropar Booking Service skickas också en Bearer-token.

Det innebär att JWT används både för skyddade bokningsoperationer och vid service-to-service-kommunikation.

---

# 🗄️ Databaser

Varje microservice har sin egen databas.

| Service | Databas |
|---|---|
| Booking Service | `pensionatendb` |
| Customer Service | `customerdb` |
| Review Service | `reviewdb` |

```text
Booking Service  ────> booking-db
Customer Service ────> customer-db
Review Service   ────> review-db
```

Ingen tjänst läser direkt från en annan tjänsts databas.

Detta gör att varje microservice äger sin egen data och kommunikation mellan tjänster sker genom deras API:er.

---

# 🐳 Docker

Alla tre microservices har Docker-stöd.

Systemet kan startas tillsammans med **Docker Compose**.

`docker-compose.yml` ligger i Booking Service-repot och startar:

```text
booking-service
customer-service
review-service
booking-db
customer-db
review-db
```

Det innebär totalt:

**3 Spring Boot-applikationer + 3 MySQL-databaser**

---

# 📁 Projektstruktur

Docker Compose förutsätter att de tre repona ligger bredvid varandra:

```text
projekt/
├── booking-service/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── k8s/
│   └── ...
│
├── customer-service/
│   ├── Dockerfile
│   └── ...
│
└── review-service/
    ├── Dockerfile
    └── ...
```

Klona repona:

```bash
git clone https://github.com/AlexanderJava11/booking-service.git
git clone https://github.com/AlexanderJava11/customer-service.git
git clone https://github.com/AlexanderJava11/review-service.git
```

---

# 🐳 Starta med Docker Compose

Gå till Booking Service:

```bash
cd booking-service
```

Starta hela systemet:

```bash
docker compose up --build
```

När systemet har startat:

| Tjänst | Port |
|---|---:|
| Booking Service | `8080` |
| Customer Service | `8081` |
| Review Service | `8082` |
| Booking MySQL | `3307 → 3306` |
| Customer MySQL | `3308 → 3306` |
| Review MySQL | `3309 → 3306` |

Webbgränssnitt:

```text
http://localhost:8080
```

Customer API:

```text
http://localhost:8081/api/customers
```

Review API:

```text
http://localhost:8082/api/reviews
```

Stoppa systemet:

```bash
docker compose down
```

För att även radera Docker-volymer:

```bash
docker compose down -v
```

---

# ☸️ Kubernetes

Systemet kan även köras lokalt i ett Kubernetes-kluster.

Kubernetes-konfigurationen ligger under:

```text
k8s/
```

Strukturen är uppdelad efter microservice:

```text
k8s/
├── booking/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── database.yaml
│   └── secret.yaml
│
├── customer/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── database.yaml
│   └── secret.yaml
│
└── review/
    ├── deployment.yaml
    ├── service.yaml
    ├── database.yaml
    └── secret.yaml
```

Varje microservice har:

- en Kubernetes `Deployment`
- en Kubernetes `Service`
- en egen MySQL Deployment/Service
- konfiguration via environment variables
- Kubernetes `Secret` för känslig konfiguration

## Deploya Kubernetes-resurser

Från Booking Service-repot:

```bash
kubectl apply -R -f k8s
```

Kontrollera Pods:

```bash
kubectl get pods
```

När systemet fungerar ska sex applikations-/databaspods vara igång:

```text
booking-service
booking-db

customer-service
customer-db

review-service
review-db
```

Kontrollera Services:

```bash
kubectl get services
```

---

# 🌐 Öppna webbapplikationen från Kubernetes

Booking Service kan exponeras lokalt genom port-forwarding:

```bash
kubectl port-forward service/booking-service 8080:8080
```

Öppna därefter:

```text
http://localhost:8080
```

Följande delar har verifierats via Kubernetes:

```text
/
├── /customers
├── /bookings
└── /reviews
```

Booking Service kommunicerar samtidigt med Customer Service och Review Service via Kubernetes interna Services.

---

# 🔑 Kubernetes Secrets

Kubernetes `Secret` används för konfiguration som exempelvis:

- databaslösenord
- JWT secret
- admin-användarnamn
- admin-lösenord

Applikationerna hämtar värdena som environment variables.

Exempel:

```yaml
valueFrom:
  secretKeyRef:
    name: booking-secret
    key: jwt-secret
```

Customer Service och Booking Service konfigureras med samma JWT-secret så att tokens kan skapas och verifieras mellan tjänsterna.

Värden som finns i projektet är endast avsedda för **lokal utveckling/demonstration**.

Riktiga produktionshemligheter ska inte sparas i Git.

---

# ☁️ Deployment

Microservices har även deployats separat i Railway.

Det innebär att tjänsterna kan köras som separata applikationer även utanför den lokala Docker/Kubernetes-miljön.

Kommunikationen konfigureras genom environment variables, exempelvis:

```text
CUSTOMER_SERVICE_URL
REVIEW_SERVICE_URL
BOOKING_SERVICE_URL
```

Databasuppgifter, JWT-secret och inloggningsuppgifter konfigureras också via environment variables och ska inte hårdkodas som produktionshemligheter i källkoden.

JWT-flödet mellan Customer Service och Booking Service har verifierats i Railway.

---

# ⚙️ Environment Variables

## Booking Service

| Variabel | Beskrivning |
|---|---|
| `DB_URL` | JDBC-URL till Booking-databasen |
| `DB_USERNAME` | Databasanvändare |
| `DB_PASSWORD` | Databaslösenord |
| `CUSTOMER_SERVICE_URL` | URL till Customer Service |
| `REVIEW_SERVICE_URL` | URL till Review Service |
| `JWT_SECRET` | Nyckel för signering/verifiering av JWT |
| `ADMIN_USERNAME` | Lokalt admin-användarnamn |
| `ADMIN_PASSWORD` | Lokalt admin-lösenord |

## Customer Service

| Variabel | Beskrivning |
|---|---|
| `CUSTOMER_DB_URL` | JDBC-URL till Customer-databasen |
| `CUSTOMER_DB_USERNAME` | Databasanvändare |
| `CUSTOMER_DB_PASSWORD` | Databaslösenord |
| `BOOKING_SERVICE_URL` | URL till Booking Service |
| `JWT_SECRET` | Nyckel för signering/verifiering av JWT |
| `ADMIN_USERNAME` | Användarnamn för JWT-login |
| `ADMIN_PASSWORD` | Lösenord för JWT-login |

Customer Service och Booking Service måste använda samma `JWT_SECRET` för att Booking Service ska kunna verifiera tokens som skapats av Customer Service.

## Review Service

Review Service använder sina databasinställningar via environment variables.

> Lokala standardvärden är endast avsedda för utveckling. Produktionsuppgifter och riktiga secrets ska sättas genom miljövariabler och ska inte lagras i Git.

---

# 🧪 Testning

Booking Service använder automatiserade tester för bland annat boknings- och rumslogik.

Kör:

```bash
./mvnw test
```

På Windows:

```powershell
.\mvnw.cmd test
```

Testmiljön använder H2 där det är lämpligt för att testerna ska kunna köras isolerat från produktionsdatabasen.

Customer Service innehåller även integrationstester för REST/API-funktionalitet.

JWT har dessutom testats genom att:

1. logga in via Customer Service,
2. hämta en JWT-token,
3. försöka skapa en bokning utan token och få `403 Forbidden`,
4. skicka samma request med `Authorization: Bearer <JWT_TOKEN>`,
5. verifiera att bokningen skapas med giltig token.

---

# 🖥️ Köra tjänsten utan Docker

Booking Service kan även startas direkt med Maven:

### Linux / macOS

```bash
./mvnw spring-boot:run
```

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Vid lokal körning krävs rätt databaskonfiguration samt att de microservices som används är tillgängliga.

---

# 🛠️ Tekniker

Projektet använder bland annat:

| Teknik | Användning |
|---|---|
| Java 17 | Programmeringsspråk |
| Spring Boot | Backend |
| Spring MVC | Controllers / webb |
| Spring Data JPA | Databasåtkomst |
| Spring Security | Säkerhet |
| JWT | API-autentisering |
| Thymeleaf | Webbgränssnitt |
| MySQL | Produktionsdatabaser |
| H2 | Testdatabas |
| REST / JSON | Kommunikation mellan microservices |
| Maven | Build / dependencies |
| Docker | Containerisering |
| Docker Compose | Lokal orkestrering |
| Kubernetes | Containerorkestrering |
| Railway | Deployment |
| Git / GitHub | Versionshantering |

---

# 📌 Viktiga designbeslut

## Varför microservices?

Backend 1 var en monolit där flera ansvarsområden låg i samma applikation.

I Backend 2 separerades systemet så att:

```text
Booking → bokningar och rum
Customer → kunder
Review → recensioner
```

Det gör att tjänsterna får tydligare ansvar och kan utvecklas och deployas mer självständigt.

Microservices innebär samtidigt mer komplexitet eftersom tjänsterna behöver kommunicera över nätverket och fel kan uppstå när en annan tjänst inte är tillgänglig.

---

## Varför separata databaser?

Varje tjänst äger sin egen data.

Det innebär exempelvis att Booking Service **inte** går direkt till Customer Services databas för att kontrollera en kund.

Istället:

```text
Booking Service
      │
      │ HTTP REST
      ▼
Customer Service
      │
      ▼
customer-db
```

Det minskar kopplingen mellan tjänsterna och gör att Customer Service själv ansvarar för hur dess data hanteras.

---

## Vad händer om en tjänst går ner?

Microservices innebär att en tjänst kan ligga nere medan andra fortfarande kör.

Systemet hanterar därför kommunikationsfel och timeouts.

Ett viktigt exempel är:

```text
DELETE Customer
       │
       ▼
Customer Service
       │
       │ kontrollera aktiva bokningar
       ▼
Booking Service
```

Om Booking Service inte går att nå kan Customer Service inte säkert avgöra om kunden har en bokning.

Därför stoppas raderingen istället för att riskera inkonsistent data.

---

# 🎯 Sammanfattning

Villa Avougjagi demonstrerar övergången från en traditionell Spring Boot-monolit till ett system byggt kring flera självständiga tjänster.

Projektet innehåller:

- ✅ Tre Spring Boot microservices
- ✅ Tre separata MySQL-databaser
- ✅ REST-kommunikation mellan tjänster
- ✅ CRUD-funktionalitet
- ✅ Validering och felhantering
- ✅ Skydd mot radering av kund med aktiv bokning
- ✅ Hantering när annan microservice inte är tillgänglig
- ✅ JWT-login via Customer Service
- ✅ JWT-skydd för ändrande Booking-endpoints
- ✅ Bearer-token vid service-to-service-kommunikation
- ✅ Automatiserade tester
- ✅ Docker
- ✅ Docker Compose
- ✅ Kubernetes
- ✅ Kubernetes Secrets
- ✅ Railway deployment
- ✅ Git & GitHub

Målet har varit att skapa ett system där varje tjänst har ett tydligt ansvar, äger sin egen data och kommunicerar med övriga delar genom väldefinierade API:er.

---

## 👨‍💻 Projekt

**Backend 2 – Microservices**

Utvecklat som en del av utbildningen **Javautvecklare på Nackademin**.
