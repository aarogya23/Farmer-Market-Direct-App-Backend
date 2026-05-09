# Farmer Market Direct App - Backend

## Description
This is the Spring Boot backend for the Farmer Market Direct App.

It provides:
- JWT-based authentication
- role-based access for `ADMIN`, `FARMER`, and `BUYER`
- product creation and approval workflows
- image upload support
- simple real-time chat using REST + WebSocket

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- JWT
- MySQL
- Maven
- WebSocket

## Current Features
- User signup and login
- Google login
- Authenticated profile lookup with `/api/auth/me`
- Farmer product creation
- Public product listing and search
- Farmer-specific product listing
- Admin product approval and rejection
- Product image upload
- Real-time chat delivery through WebSocket
- Online status check for chat users

## Roles
- `ADMIN`: reviews and updates product approval status
- `FARMER`: adds and manages products
- `BUYER`: browses products and can participate in chat

## Authentication
This project uses JWT for protected APIs.

After login, include the token in the request header:

```http
Authorization: Bearer <your_token>
```

## Project Structure
The source code lives under `FarmerDirectMarkert/src/main/java/com/commerce/FarmerDirectMarkert`.

```text
FarmerDirectMarkert/src/main/java/com/commerce/FarmerDirectMarkert
├── Controller   # REST controllers
├── config       # Spring MVC, security, and websocket configuration
├── dto          # Request and response payloads
├── model        # JPA entities and enums
├── repository   # Spring Data repositories
├── security     # JWT request filter
├── service      # Business logic
└── websocket    # WebSocket handler
```

Resources are under `FarmerDirectMarkert/src/main/resources`.

## Setup

### 1. Clone the repository
```bash
git clone https://github.com/aarogya23/Farmer-Market-Direct-App.git
```

### 2. Navigate to the backend project
```bash
cd Farmer-Market-Direct-App-Backend
```

### 3. Configure the database
Update `FarmerDirectMarkert/src/main/resources/application.properties` if needed:

```properties
server.port=8082
spring.datasource.url=jdbc:mysql://localhost:3306/farmer_direct_market?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

### 4. Run the application
If Maven is installed:

```bash
mvn spring-boot:run
```

On Windows with the Maven wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

## Main API Endpoints

### Authentication
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/google`
- `GET /api/auth/me`

### Products
- `POST /api/products`
- `GET /api/products/test`
- `GET /api/products/allProducts`
- `GET /api/products/my-products`

### Product Images
- `POST /api/products/upload-image`
- `GET /uploads/{filename}`

### Admin Product Review
- `GET /api/admin/products/pending`
- `PATCH /api/admin/products/{id}/status`

### Chat
- `POST /api/chat/send`
- `GET /api/chat/online/{email}`

## Product Search
`GET /api/products/test` supports optional query parameters:

- `q`
- `category`
- `minPrice`
- `maxPrice`
- `sort`

Example:

```http
GET /api/products/test?q=tomato&category=VEGETABLES&minPrice=10&maxPrice=100&sort=newest
```

## Chat and WebSocket
This backend uses a simple chat flow:

- REST API sends the message
- WebSocket pushes the live update to connected users

### WebSocket endpoint
```text
ws://localhost:8082/ws/chat?token=<jwt>
```

### Send chat message
```http
POST /api/chat/send
Authorization: Bearer <jwt>
Content-Type: application/json
```

```json
{
  "recipientEmail": "buyer@example.com",
  "content": "Hello"
}
```

When both users are connected to the WebSocket endpoint, the sender and recipient both receive the same live message payload.

## File Upload Notes
- Uploaded images are stored in the local `uploads/` folder
- Uploaded files are served through `/uploads/**`
- Multipart upload size is limited to `10MB`

## Important Notes
- The application currently runs on port `8082`
- The database name in the current configuration is `farmer_direct_market`
- Google OAuth client values are read from `application.properties`
- The image upload controller currently builds image URLs with a hardcoded host, so that should be adjusted before deployment

## Future Improvements
- Persist chat history in the database
- Improve websocket presence and delivery acknowledgements
- Replace hardcoded upload host with configurable base URL
- Add global exception handling for chat APIs
- Add Docker support and deployment configuration

## Contributing
Feel free to fork the repository and contribute improvements.
