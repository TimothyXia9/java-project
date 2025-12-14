# Nutrition Tracker System

## Project Overview

This is an intelligent nutrition tracking system built with Java, using a separated frontend-backend architecture (React + Spring Boot). It helps users track their daily calorie intake by uploading food images or scanning barcodes to obtain nutritional information.

## Core Features

- **Image Recognition** - Identify food items and estimate portions using OpenAI API
- **Nutrition Analysis** - Retrieve detailed nutritional data via USDA API
- **Barcode Scanning** - Quick food information lookup
- **User Management** - Registration, login, and profile management
- **Meal Logging** - Record and manage breakfast, lunch, dinner, and snacks
- **Data Statistics** - Visualize nutrition intake trends

## Technology Stack

**Backend:** Spring Boot 3.x + MySQL

**Frontend:** React 18 + Axios

**External APIs:** OpenAI GPT-4o, USDA FoodData Central, Open Food Facts

## Java Features Implementation

### 1. RESTful API Development (Network Programming)

- Complete HTTP request handling (GET/POST/PUT/DELETE)
- Asynchronous external API calls
- Unified exception handling and CORS configuration

### 2. Multithreading and Asynchronous Processing (Concurrent Programming)

- Asynchronous API calls using @Async annotation
- Concurrent processing of nutrition data queries
- Concurrent handling of batch image uploads
- Asynchronous nutrition report generation

### 3. Database Operations

- Complete CRUD operations and queries
- JPA entity relationships management

### 4. File Upload Handling (I/O Operations)

- File type and size validation
- Image storage management

### 5. External API Integration

- API calls using RestTemplate/WebClient
- API key management
- Request retry and error handling
- Rate limiting
- JSON response parsing

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+ and npm

### Setup Instructions

1. Clone the repository
2. Configure database credentials in `application.yml` or `.env` file
3. Set up API keys for OpenAI and USDA (optional)
4. Start the backend: `mvn spring-boot:run`
5. Install frontend dependencies: `cd frontend && npm install`
6. Start the frontend: `npm run dev`
7. Access the application at http://localhost:3000

For detailed setup instructions, see [SETUP.md](SETUP.md)

## Documentation

- [SETUP.md](SETUP.md) - Detailed setup and configuration guide
- [API_KEYS_SETUP.md](API_KEYS_SETUP.md) - API keys configuration guide
- [HOW_TO_START.md](HOW_TO_START.md) - How to start the application
- [CLAUDE.md](CLAUDE.md) - Development guide for Claude Code

## Architecture

The system follows a three-tier architecture:

- **Presentation Layer:** React frontend with responsive design
- **Business Logic Layer:** Spring Boot REST API with service layer
- **Data Layer:** MySQL database with JPA/Hibernate ORM

## Security Features

- JWT-based authentication
- BCrypt password encryption
- CORS configuration
- Input validation
- File upload security checks

## License

This project is for educational purposes.
