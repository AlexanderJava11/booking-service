# Booking Service (Villa Avougjagi / Pensionaten)

Booking Service är huvudapplikationen ("fasaden") i systemet. Den är en
Spring Boot-app som både:

- exponerar ett **webbgränssnitt** (Thymeleaf) där personalen kan hantera
  kunder, rum, bokningar och recensioner, och
- exponerar ett internt **REST-API** som de andra tjänsterna använder för
  att kontrollera om en kund har aktiva bokningar.

Den pratar i sin tur med **Customer Service** och **Review Service** över
HTTP för att hämta kund- respektive recensionsdata, eftersom den inte äger
den datan själv.

## Vad tjänsten gör

- **Rum** (`Room`) – CRUD för hotellrum (rumsnummer, rumstyp, extrasängar,
  pris/natt). Ett rum kan bara tas bort om det inte har några bokningar.
- **Bokningar** (`Booking`) – skapar/uppdaterar/tar bort bokningar, med
  kontroller för att:
  - kunden faktiskt finns (frågar Customer Service),
  - check-ut är efter check-in,
  - rummet är ledigt och rymmer antal gäster under perioden.
  - Beräknar antal nätter och totalpris utifrån rummets pris/natt.
- **Kunder** – ett tunt UI (`/customers`) ovanpå Customer Service; sidan
  hämtar, skapar, redigerar och tar bort kunder via `CustomerClient`.
- **Recensioner** – ett tunt UI (`/reviews`) ovanpå Review Service, via
  `ReviewClient`.
- **Inloggning/JWT** (`/api/auth/login`) – enkel admin-inloggning
  (användarnamn/lösenord från konfiguration) som utfärdar en JWT-token.
  Tokenet krävs för de flesta `/api/bookings/**`-endpoints
  (`JwtAuthenticationFilter` + `SecurityConfig`).
- **Internt API** – `GET /api/bookings/customer/{customerId}/active`
  (öppet, inget JWT-krav) som svarar `true`/`false` beroende på om kunden
  har en aktiv (ej avslutad) bokning. Det är detta endpoint Customer
  Service anropar innan den tillåter att en kund raderas.

## Hur tjänsterna pratar med varandra

```
                +-------------------+
                |   Booking Service |  (port 8080)
                |  (webb + API)     |
                +---------+---------+
                |                   |
   GET/POST/PUT |                   | GET/POST/PUT/DELETE
   /api/customers|                  | /api/reviews
                v                   v
      +------------------+   +------------------+
      | Customer Service |   |  Review Service  |
      |   (port 8081)    |   |   (port 8082)    |
      +--------+---------+   +------------------+
               |
               | GET /api/bookings/customer/{id}/active
               v
        +-------------------+
        |  Booking Service  |
        +-------------------+
```

- **Booking Service → Customer Service**: `CustomerClient` (RestTemplate)
  anropar `${customer.service.url}/api/customers[...]` för att lista,
  hämta, skapa/uppdatera och ta bort kunder, samt (i `BookingService`) för
  att verifiera att en kund finns innan en bokning sparas.
- **Booking Service → Review Service**: `ReviewClient` anropar
  `${review.service.url}/api/reviews[...]` för att lista, hämta,
  skapa/uppdatera och ta bort recensioner.
- **Customer Service → Booking Service**: `BookingClient` i Customer
  Service anropar tillbaka till Booking Service
  (`${booking.service.url}/api/bookings/customer/{id}/active`) för att
  kontrollera om kunden har en aktiv bokning innan kunden får raderas.
- All kommunikation sker via **REST/JSON över HTTP**, ingen
  meddelandekö/event-buss används.
- Varje tjänst har sin **egen MySQL-databas** – ingen delad databas.

## Konfiguration (miljövariabler)

| Variabel | Standardvärde (lokalt) | Beskrivning |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/Pensionatendb...` | JDBC-URL till bokningsdatabasen |
| `DB_USERNAME` | `root` | DB-användare |
| `DB_PASSWORD` | *(tomt)* | DB-lösenord |
| `CUSTOMER_SERVICE_URL` | `http://localhost:8081` | Bas-URL till Customer Service |
| `REVIEW_SERVICE_URL` | `http://localhost:8082` | Bas-URL till Review Service |
| `JWT_SECRET` | *(defaultnyckel, ändra i produktion)* | Hemlighet för att signera JWT |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` | Inloggningsuppgifter för admin-JWT |

Tjänsten körs på **port 8080**.

## Så här startar du hela systemet

Det här repot innehåller `docker-compose.yml` för **hela systemet**
(alla tre tjänster + tre MySQL-databaser). Den förväntar sig att
`booking-service`, `customer-service` och `review-service` ligger som
syskonkataloger på din maskin, t.ex.:

```
projekt/
├── booking-service/    <- docker-compose.yml ligger här
├── customer-service/
└── review-service/
```

Klona alla tre repon intill varandra:

```bash
git clone https://github.com/AlexanderJava11/booking-service.git
git clone https://github.com/AlexanderJava11/customer-service.git
git clone https://github.com/AlexanderJava11/review-service.git
```

Starta sedan allt från `booking-service`-katalogen:

```bash
cd booking-service
docker compose up --build
```

Det här startar:

| Tjänst | Port (host) | Beskrivning |
|---|---|---|
| `booking-db` | 3307 → 3306 | MySQL för Booking Service |
| `customer-db` | 3308 → 3306 | MySQL för Customer Service |
| `review-db` | 3309 → 3306 | MySQL för Review Service |
| `booking-service` | 8080 | Webb-UI + API, väntar på att `booking-db` är frisk och att `customer-service` har startat |
| `customer-service` | 8081 | REST-API mot kunder |
| `review-service` | 8082 | REST-API mot recensioner |

När allt är uppe:

- Webb-UI: <http://localhost:8080>
- Customer Service API: <http://localhost:8081/api/customers>
- Review Service API: <http://localhost:8082/api/reviews>

Stoppa allt igen med:

```bash
docker compose down
```

Lägg till `-v` (`docker compose down -v`) om du även vill radera
databasernas volymer (dvs. all data).

## Köra bara den här tjänsten lokalt (utan Docker)

```bash
./mvnw spring-boot:run
```

Kräver då en lokal MySQL på `localhost:3306` med databasen
`Pensionatendb` (se `application.properties` för standardvärden), samt
att Customer Service och Review Service körs (lokalt eller på annan
host) om du vill använda kund-/recensionsfunktionerna fullt ut.

## Testning

```bash
./mvnw test
```
