# Nutrition Tracker System

A modern, intelligent nutrition tracking application that leverages AI-powered image recognition and comprehensive nutrition databases to help users monitor their dietary intake effortlessly.

## Table of Contents

-   [Project Overview](#project-overview)
-   [Core Features](#core-features)
-   [Tech Stack](#tech-stack)
-   [Prerequisites](#prerequisites)
-   [Starting the Application](#starting-the-application)
-   [API Documentation](#api-documentation)
-   [Key Implementation Details](#key-implementation-details)

## Project Overview

This is an intelligent nutrition tracking system built with a modern separated frontend-backend architecture combining React and Spring Boot. The application simplifies nutrition tracking by allowing users to:

-   Upload food images for automatic identification and portion estimation
-   Scan product barcodes for instant nutritional information
-   Log meals throughout the day with automatic calorie calculations
-   Visualize nutrition trends and patterns over time
-   Set and track daily nutrition goals

The system integrates with multiple external APIs to provide accurate, comprehensive nutritional data and uses AI to analyze food images, making nutrition tracking as simple as taking a photo.

## Core Features

### 1. AI-Powered Image Recognition

-   Upload food images to automatically identify food items
-   Estimate portion sizes using computer vision
-   Powered by OpenAI GPT-4 Vision API
-   Supports multiple food items in a single image
-   Provides confidence scores for identifications

### 2. Comprehensive Nutrition Analysis

-   Retrieve detailed nutritional data from USDA FoodData Central
-   Access macronutrients (protein, carbs, fats)
-   View micronutrients (vitamins, minerals)
-   Get calorie information per serving
-   Support for thousands of food items

### 3. Barcode Scanning

-   Quick product lookup via barcode
-   Integration with Open Food Facts database
-   Instant access to packaged food nutrition facts
-   Support for international products

### 4. User Management

-   Secure user registration and authentication
-   JWT-based session management
-   User profile customization
-   Password encryption with BCrypt
-   Profile settings for dietary goals

### 5. Meal Logging

-   Log meals by category (breakfast, lunch, dinner, snacks)
-   Edit and delete meal entries
-   Add custom food items
-   Track meals over time
-   Daily calorie summaries

## Tech Stack

### Backend

-   **Framework:** Spring Boot 3.2.0
-   **Language:** Java 17
-   **Build Tool:** Maven 3.6+
-   **Database:** MySQL 8.0+
-   **ORM:** Hibernate 6.x (JPA)
-   **Security:** Spring Security + JWT
-   **API Client:** RestTemplate/WebClient
-   **Validation:** Jakarta Bean Validation
-   **Utilities:** Lombok, Jackson

### Frontend

-   **Framework:** React 18
-   **Build Tool:** Vite
-   **HTTP Client:** Axios
-   **Styling:** CSS3, Modern UI components
-   **Charts:** D3.js / Recharts
-   **State Management:** React Hooks
-   **Routing:** React Router

### External APIs

-   **OpenAI API:** GPT-4 Vision for image recognition
-   **USDA FoodData Central:** Comprehensive nutrition database
-   **Open Food Facts:** Barcode scanning and product information

### Development Tools

-   **Version Control:** Git
-   **IDE Support:** IntelliJ IDEA, VS Code
-   **Testing:** JUnit 5, Spring Test, Jest
-   **API Testing:** Postman/Thunder Client

### Key Architectural Principles

1. **Separation of Concerns:** Clear boundaries between presentation, business logic, and data layers
2. **RESTful Design:** Stateless API following REST conventions
3. **Asynchronous Processing:** Non-blocking operations for external API calls
4. **Security First:** JWT authentication, encrypted passwords, CORS configuration
5. **Scalability:** Thread pool configuration for concurrent request handling

## Prerequisites

Before you begin, ensure you have the following installed:

### Required

-   **Java Development Kit (JDK) 17 or higher**

    -   Verify: `java -version`
    -   Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

-   **Maven 3.6 or higher**

    -   Verify: `mvn -version`
    -   Download: [Apache Maven](https://maven.apache.org/download.cgi)

-   **MySQL 8.0 or higher**

    -   Verify: `mysql --version`
    -   Download: [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

-   **Node.js 18+ and npm**
    -   Verify: `node --version` and `npm --version`
    -   Download: [Node.js](https://nodejs.org/)

## Starting the Application

To install dependencies and start the application, run the following commands

### Install Backend Dependencies

```bash
./mvnw clean install
```

Or with Maven installed globally:

```bash
mvn clean install
```

### Install Frontend Dependencies

```bash
cd frontend
npm install
cd ..
```

### MySQL Database Setup

1. Start MySQL server

```bash
  mysql -u root -p
```

2. Create a database named `nutrition_tracker`

```sql
CREATE DATABASE nutrition_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Environment Variables

```bash
cp .env.example .env
nano .env
```

Update:
```
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
```
### Start the Application

For backend start with `bash ./start-backend.sh` and for frontend start with `cd frontend && bash ./start-frontend.sh`.

Openai API key and USDA API key of my own is already set in the application-local.yml file, please do not abuse using it and do not share it publicly.

## API Documentation

### Authentication Endpoints

| Method | Endpoint             | Description       | Authentication |
| ------ | -------------------- | ----------------- | -------------- |
| POST   | `/api/auth/register` | Register new user | No             |
| POST   | `/api/auth/login`    | User login        | No             |
| POST   | `/api/auth/logout`   | User logout       | Yes            |
| GET    | `/api/auth/profile`  | Get user profile  | Yes            |
| PUT    | `/api/auth/profile`  | Update profile    | Yes            |

### Meal Logging Endpoints

| Method | Endpoint           | Description       | Authentication |
| ------ | ------------------ | ----------------- | -------------- |
| POST   | `/api/meals`       | Create meal entry | Yes            |
| GET    | `/api/meals`       | Get user meals    | Yes            |
| GET    | `/api/meals/{id}`  | Get specific meal | Yes            |
| PUT    | `/api/meals/{id}`  | Update meal       | Yes            |
| DELETE | `/api/meals/{id}`  | Delete meal       | Yes            |
| GET    | `/api/meals/today` | Get today's meals | Yes            |

### Image Recognition Endpoints

| Method | Endpoint                  | Description        | Authentication |
| ------ | ------------------------- | ------------------ | -------------- |
| POST   | `/api/food/analyze-image` | Analyze food image | Yes            |
| POST   | `/api/food/barcode`       | Scan barcode       | Yes            |

### Nutrition Data Endpoints

| Method | Endpoint                       | Description           | Authentication |
| ------ | ------------------------------ | --------------------- | -------------- |
| GET    | `/api/nutrition/search`        | Search food items     | Yes            |
| GET    | `/api/nutrition/{foodId}`      | Get nutrition details | Yes            |
| GET    | `/api/nutrition/daily-summary` | Get daily summary     | Yes            |

### Request/Response Examples

**Register User:**

```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}
```

**Response:**

```json
{
	"message": "User registered successfully",
	"userId": 1
}
```

**Analyze Food Image:**

```bash
POST /api/food/analyze-image
Authorization: Bearer <jwt-token>
Content-Type: multipart/form-data

image: <file>
```

**Response:**

```json
{
	"foodItems": [
		{
			"name": "Grilled Chicken Breast",
			"portion": "150g",
			"calories": 165,
			"protein": 31,
			"carbs": 0,
			"fat": 3.6,
			"confidence": 0.95
		}
	]
}
```

## Key Implementation Details

### 1. RESTful API Development (Network Programming)

The application implements a complete RESTful API following industry best practices:

-   **HTTP Methods:** Full CRUD support using GET, POST, PUT, DELETE
-   **Status Codes:** Proper HTTP status codes (200, 201, 400, 401, 404, 500)
-   **Content Negotiation:** JSON request/response format
-   **API Versioning:** Endpoints prefixed with `/api` for version management
-   **Error Handling:** Standardized error responses with meaningful messages

**Example Controller:**

```java
@RestController
@RequestMapping("/api/meals")
public class MealController {

    @PostMapping
    public ResponseEntity<MealDTO> createMeal(@Valid @RequestBody MealDTO mealDTO) {
        MealDTO created = mealService.createMeal(mealDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealDTO> getMeal(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getMealById(id));
    }
}
```

### 2. Multithreading and Asynchronous Processing

The system uses Spring's `@Async` annotation for non-blocking operations:

**Thread Pool Configuration:**

```yaml
spring:
    task:
        execution:
            pool:
                core-size: 5
                max-size: 10
                queue-capacity: 100
```

**Async Service Example:**

```java
@Service
public class ImageRecognitionService {

    @Async
    public CompletableFuture<FoodIdentification> analyzeImage(MultipartFile image) {
        // Non-blocking API call to OpenAI
        FoodIdentification result = callOpenAIAPI(image);
        return CompletableFuture.completedFuture(result);
    }
}
```

**Benefits:**

-   Concurrent processing of multiple image uploads
-   Non-blocking external API calls
-   Improved application throughput and response times
-   Better resource utilization

### 3. Database Operations (JPA & Hibernate)

**Entity Relationships:**

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Meal> meals;
}

@Entity
@Table(name = "meals")
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
        name = "meal_foods",
        joinColumns = @JoinColumn(name = "meal_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<Food> foods;
}
```

**Repository Pattern:**

```java
@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT m FROM Meal m WHERE m.user.id = :userId AND m.date = :date")
    List<Meal> findTodaysMeals(@Param("userId") Long userId, @Param("date") LocalDate date);
}
```

### 4. File Upload Handling

**Security & Validation:**

```java
@Service
public class FileUploadService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/jpg"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String saveImage(MultipartFile file) {
        // Validate file type
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ValidationException("Invalid file type");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File too large");
        }

        // Generate unique filename to prevent overwrites
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Save file securely
        Path uploadPath = Paths.get(uploadDir);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        return filename;
    }
}
```

### 5. External API Integration

**OpenAI Vision API Integration:**

```java
@Service
public class ImageRecognitionService {

    @Value("${api.openai.key}")
    private String apiKey;

    @Value("${api.openai.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    @Async
    public CompletableFuture<List<FoodItem>> identifyFood(MultipartFile image) {
        // Convert image to base64
        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

        // Prepare request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4o",
            "messages", List.of(Map.of(
                "role", "user",
                "content", List.of(
                    Map.of("type", "text", "text", "Identify food items and estimate portions"),
                    Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image))
                )
            ))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Make API call with retry logic
        ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
            apiUrl, HttpMethod.POST, request, OpenAIResponse.class
        );

        return CompletableFuture.completedFuture(parseResponse(response.getBody()));
    }
}
```

**USDA API Integration:**

```java
@Service
public class NutritionService {

    public NutritionData getNutritionData(String foodName) {
        String url = String.format("%s/foods/search?query=%s&api_key=%s",
            usdaApiUrl, foodName, usdaApiKey);

        ResponseEntity<UsdaResponse> response = restTemplate.getForEntity(url, UsdaResponse.class);
        return mapToNutritionData(response.getBody());
    }
}
```

**Error Handling & Retry:**

```java
@Retryable(
    value = {RestClientException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000)
)
public ApiResponse callExternalApi(String endpoint) {
    // API call with automatic retry on failure
}
```

### 6. Security Implementation

**JWT Authentication:**

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
```

**Password Encryption:**

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```
