# Echonymous Project Local Setup Guide

This README will guide you through the steps to clone and set up the **Echonymous Spring Boot** project locally, including all necessary dependencies and configurations.

## Prerequisites

Before you begin, ensure that you have the following software installed:

1. **Git**: Ensure that Git is installed on your local machine.
2. **JDK 17+**: The project uses Java 17 (or higher). Ensure that JDK 17 or above is installed.
3. **IntelliJ IDEA**: This guide assumes you're using IntelliJ IDEA as your IDE.
4. **PostgreSQL**: Make sure PostgreSQL is installed and running locally for database setup.

## Step 1: Clone the Repository

Start by cloning the repository to your local machine using the following commands:

1. Open your terminal and run:

    ```bash
    git clone https://github.com/echonymous/echonymous.git
    ```

2. Navigate into the project directory:

    ```bash
    cd echonymous
    ```

## Step 2: Import the Project into IntelliJ IDEA

  1. Open **IntelliJ IDEA**.
     
  2. Click on **Open** and select the folder where you cloned the repository.
     
  3. IntelliJ IDEA will automatically detect and import the project as a Maven project and download necessary dependencies.

## Step 3: Environment-Specific Configuration

This project uses different property files for different environments (development and production). To set up the local development environment, follow these steps:

1. In the `src/main/resources` folder, create a new file named `application-dev.properties`.

2. Add the following properties to `application-dev.properties`:

   ```properties
   # ===============================
   # = DATA SOURCE
   # ===============================
   spring.datasource.url=jdbc:postgresql://localhost:5432/echonymous
   spring.datasource.username=postgres
   spring.datasource.password=your_password_here

   # ===============================
   # = Only for development purposes
   # ===============================
   spring.security.user.name=your_username_here
   spring.security.user.password=your_password_here

## Step 4: Configure `.gitignore`

To ensure sensitive data isn't pushed to GitHub, add the below entry to the `.gitignore` file:
```gitignore
/src/main/resources/application-*.properties
````

## Step 5: Install Lombok Plugin in IntelliJ IDEA

**Lombok** is used for generating getters, setters, and other boilerplate code. If Lombok annotations are not working in your IntelliJ IDEA, follow these steps:

---

### 1. Install the Lombok Plugin

   &nbsp;&nbsp;• Open **IntelliJ IDEA**.  
   &nbsp;&nbsp;• Go to **File -> Settings** (or **IntelliJ IDEA -> Preferences** on macOS).  
   &nbsp;&nbsp;• In the search bar, type **Plugins**.  
   &nbsp;&nbsp;• Click on **Installed**, and search for **Lombok**.  
   &nbsp;&nbsp;• If Lombok is not installed:  
         &nbsp;&nbsp;&nbsp;&nbsp;- Click **Browse Repositories**.  
         &nbsp;&nbsp;&nbsp;&nbsp;- Search for **Lombok**.  
         &nbsp;&nbsp;&nbsp;&nbsp;- Click **Install**.  
   &nbsp;&nbsp;• Restart IntelliJ IDEA after the plugin installation.  
   
---

### 2. Enable Annotation Processing

   &nbsp;&nbsp;• Go to **Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors**.  
   &nbsp;&nbsp;• Check the box for **Enable Annotation Processing**.  
   &nbsp;&nbsp;• Click **Apply**.  

---

### 3. Obtain Processor from Project Classpath

   &nbsp;&nbsp;• In the **Annotation Processors** settings, ensure you select **Obtain processors from project classpath**.  
   &nbsp;&nbsp;• Click **Apply**.  

## Step 6: Database Setup

To set up the database, follow these steps:

---

### 1. Ensure PostgreSQL is Running
   • Make sure PostgreSQL is installed and running on your local machine.

---

### 2. Open PostgreSQL Client
   • Open your PostgreSQL client (e.g., pgAdmin or psql).

---

### 3. Create the Database
   • Run the following SQL query to create the database:

     CREATE DATABASE echonymous;

---

### 4. Verify Database Credentials
   • Ensure the database URL, username, and password in the `application-dev.properties` file match your local PostgreSQL credentials.

## Step 7: Build and Run the Application

Once the application properties are set up and the environment is configured, you can build and run the application:

---

### 1. Build the Application

1. Open your terminal and navigate to the project folder.
2. Run the following command:

   ```bash
   mvn clean install
3. Run the application

   ```bash
   mvn spring-boot:run

### Step 8: Verify the Application

Open your browser and visit **http://localhost:8080/** to check if the application is running.
