# Bazarak – Second-Hand Marketplace

A full-stack client-server application for buying and selling second-hand goods, developed as a university project. The system provides a JavaFX desktop client and a Spring Boot backend connected to a Microsoft SQL Server database.

**Repository:**
https://github.com/Arphist/Bazarak-ap-project

---

# Team Members

* Ali Razmjoo
* Shahram Rajaei

---

# Contribution of Each Member

## Ali Razmjoo

* Set up the overall project structure and repository configuration.
* Fully implemented the **Image**, **Chat**, and **Specification** modules, including backend logic, WebSocket integration, and frontend components.
* Developed approximately half of the backend entities along with their corresponding service and repository layers.
* Implemented most of the frontend utility classes, service layers, and data models.
* Played a major role in debugging, troubleshooting, and resolving technical issues throughout the development process.
* Defining the plan and next steps, and making decisions on the overall execution approach and vision.

## Shahram Rajaei

* Designed the application's visual interface and overall user experience.
* Implemented the complete CSS stylesheet and defined the project's dark theme.
* Designed the UI layout and arranged components across all application pages.
* Wrote and maintained the project documentation, including this README.
* Developed the remaining backend entities together with their corresponding service and repository layers.
* Implemented most of the frontend FXML controllers and view-related components.

## Shared Responsibilities

Both team members actively collaborated on:

* Debugging and testing the application.
* Fixing bugs and improving stability.
* Integrating frontend and backend components.
* Reviewing and refining implemented features.
* Delivering a stable and fully functional second-hand marketplace application.

---

# Project Description

Bazarak is a desktop marketplace application that enables users to buy and sell second-hand products. Users can register, publish advertisements, communicate with sellers through real-time chat, rate other users, and manage their profiles. An administrator can manage users, categories, cities, specifications, and moderate advertisements before they become publicly visible.

---

# Features

## User Features

* User registration, login, and logout
* Profile management
* Change password
* Upload profile picture
* Assign, delete or mark ads as sold

## Marketplace

* Create advertisements
* Edit advertisements
* Delete advertisements
* Mark advertisements as sold
* Upload advertisement images
* View advertisement details
* Search advertisements
* Filter advertisements by:

  * Keyword
  * Category
  * City
  * Price range
  * Sorting options
* Add advertisements to Favorites

## Communication

* Real-time chat between buyers and sellers using WebSocket
* Rate sellers (1–5 stars)
* Optional review/comment for ratings

## Administration

* Approve, delete or reject advertisements
* Manage users
* Manage cities
* Manage categories
* Manage specifications

---

# Technologies Used

## Backend

* Java 25
* Spring Boot 4.1.0
* Spring Web MVC (REST API)
* Spring Data JPA (Hibernate)
* Spring WebSocket (STOMP)
* Spring Validation
* Spring Boot DevTools
* Microsoft SQL Server
* Microsoft JDBC Driver
* Maven

## Frontend

* Java 25
* JavaFX 21
* Java-WebSocket
* Jackson
* SLF4J
* Maven

---

# Project Architecture

```text
JavaFX Desktop Client
          │
      REST API
          │
Spring Boot Backend
          │
 Hibernate (JPA)
          │
 Microsoft SQL Server
```

---

# Project Structure

```text
bazarak-ap-project
│
├── backend
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   ├── config
│   ├── exception
│   └── resources
│
├── frontend
│   ├── controller
│   ├── service
│   ├── model
│   ├── websocket
│   ├── util
│   ├── view
│   ├── resources
│   └── css
│
└── README.md
```

---

# Prerequisites

Before running the project, make sure the following software is installed:

* Java JDK 25
* Maven 3.9+
* Microsoft SQL Server
* Git
* IntelliJ IDEA (recommended)

---

# Backend Setup

Clone the repository:

```bash
git clone https://github.com/Arphist/Bazarak-ap-project.git
cd Bazarak-ap-project
```

Go to the backend folder:

```bash
cd backend
```

Configure the database connection inside:

```text
src/main/resources/application.properties
```

Then build and run:

```bash
mvn clean install
mvn spring-boot:run
```

The backend will be available at:

```text
http://localhost:8080/api
```

---

# Frontend Setup

Navigate to the frontend folder:

```bash
cd frontend
```

Build and run:

```bash
mvn clean install
mvn javafx:run
```

If the backend address changes, update:

```text
frontend/src/main/java/com/bazarak/util/Config.java
```

including:

* BASE_URL
* BASE_IMAGE_URL
* WS_URL

---

# Database Setup

The application uses **Microsoft SQL Server**.

Create a database named:

```text
bazarak
```

Update the database configuration inside:

```text
backend/src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:sqlserver://localhost;databaseName=bazarak;encrypt=true;trustServerCertificate=true
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

Profile images are stored on the server filesystem inside the upload directory.

---

# Test Data

The project does not include predefined login credentials.

To prepare demonstration data:

* Create one administrator account.
* Add initial Cities.
* Add Categories.
* Add Specifications.
* Create several normal user accounts.
* Complete user profiles.
* Create multiple advertisements.
* Approve some advertisements and reject others using the administrator account.
* Rate different sellers using other user accounts.
* Chat with other user accounts.

This dataset demonstrates all major system functionalities.

---

# Configuration Files

| File                   | Purpose                                                    |
| ---------------------- | ---------------------------------------------------------- |
| application.properties | Database configuration, server settings, JPA configuration |
| Config.java            | Backend URL, image URL, WebSocket URL                      |
| SessionManager.java    | Session and logged-in user management                      |

---

# Running the Application

1. Start Microsoft SQL Server.
2. Configure the database connection.
3. Run the backend.

```bash
cd backend
mvn spring-boot:run
```

4. Run the frontend.

```bash
cd frontend
mvn javafx:run
```

5. Register a new account or use the prepared demonstration data.

---

# Screenshots

Screenshots are added at: docs\screenshots\.

---

# Notes

* The application uses session-based authentication.
* Real-time messaging is implemented using WebSocket.
* Advertisement and profile images are stored on the server filesystem.
* The backend runs on port **8080** with the context path **/api**.
* The frontend is designed with a modern dark theme.
