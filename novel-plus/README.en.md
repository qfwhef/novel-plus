# NovelPlus Project Documentation

## Project Overview

NovelPlus is a novel reading platform that provides functionalities such as user registration, login, novel browsing, commenting, and searching. Meanwhile, it also supports professional features for authors, including novel publishing, chapter management, and AI-assisted writing. The project is built with the Spring Boot framework, combined with technologies such as MyBatis Plus, Redis, and RabbitMQ, offering excellent scalability and high performance.

## Functional Modules

### User Module
- User registration and login
- User profile management
- Comment posting and comment management
- Novel browsing and searching

### Author Module
- Author registration and status inquiry
- Novel publishing, updating, and deletion
- Chapter management (publishing, updating, deletion)
- AI-assisted writing (expansion, continuation, polishing, abridgment)

### Novel Module
- Category-based novel browsing
- Novel detail viewing
- Chapter listing and content reading
- Novel rankings (views ranking, new releases ranking, updates ranking)
- Novel recommendations and search

### News Module
- News browsing and detail viewing

### Resource Module
- Image CAPTCHA generation
- Image upload management

## Technical Architecture

- **Backend Framework**: Spring Boot + MyBatis Plus
- **Database**: MySQL
- **Caching**: Redis + Caffeine
- **Message Queue**: RabbitMQ
- **File Storage**: R2 Object Storage
- **Security**: Spring Security + JWT
- **API Documentation**: OpenAPI 3
- **Task Scheduling**: XXL-JOB

## Core Functionality Description

### AI-Assisted Writing
By integrating AI models, authors are provided with functionalities such as expansion, continuation, polishing, and abridgment, improving writing efficiency.

### Cache Management
Multi-level caching is implemented using Redis and Caffeine, enhancing system performance and reducing database load.

### Comment Review
An asynchronous comment review mechanism is implemented via RabbitMQ, supporting sensitive word filtering and dead-letter queue handling.

### API Documentation
Interface documentation is generated using OpenAPI 3, facilitating collaborative development between frontend and backend.

### Task Scheduling
Scheduled tasks such as cache warming and ranking updates are implemented using XXL-JOB.

## Installation and Deployment

### Environment Requirements
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- XXL-JOB Admin

### Build Steps
1. Clone the project:
   ```bash
   git clone https://gitee.com/wcola/novel-plus.git
   ```
2. Import the database:
   ```bash
   mysql -u<username> -p<password> < doc/sql/*.sql
   ```
3. Modify the configuration file:
   ```bash
   vim src/main/resources/application.yml
   ```
4. Build the project:
   ```bash
   mvn clean package
   ```
5. Start the project:
   ```bash
   java -jar target/novel-plus.jar
   ```

## Usage Instructions

### API Documentation
Visit `/swagger-ui.html` to view the API documentation (development environment only).

### Admin Backend
Access the admin backend via the `/admin` path to perform management operations such as novel, chapter, and comment management.

### Author Backend
Access the author backend via the `/author` path to perform operations such as novel publishing, chapter management, and AI-assisted writing.

### Frontend Portal
Access the frontend portal via the `/front` path to browse novels, post comments, search, etc.

## Contribution Guide

Code contributions are welcome! Please follow these steps:
1. Fork the project
2. Create a new branch
3. Submit a Pull Request

## License

This project is licensed under the Apache 2.0 License. For details, please refer to the LICENSE file.