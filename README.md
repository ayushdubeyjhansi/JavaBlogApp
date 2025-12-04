Java GUI Blog Application

A robust desktop-based Blog Application built using Java Swing (Frontend) and MySQL (Backend). This project demonstrates core Object-Oriented Programming principles, Database Connectivity (JDBC), Multithreading, and resilient exception handling.

🚀 Features

User Authentication: Login system supporting 'Admin' and 'Regular' user roles.

Dynamic Feed: View posts with real-time updates.

Rich UI: Custom-drawn avatars (Gmail-style), clean CardLayout navigation, and responsive design.

CRUD Operations: Users can write, publish, and read blog posts.

Comments & Likes: Interactive features for every post.

Resilient Backend: Automatically switches to Memory Mode if the database connection fails, ensuring the app never crashes.

Multithreading: Background thread (AutoRefreshTask) mimics data synchronization without freezing the UI.

🛠️ Tech Stack

Language: Java (JDK 8+)

GUI Framework: Java Swing (AWT/Swing)

Database: MySQL

Connectivity: JDBC (MySQL Connector/J)

IDE: IntelliJ IDEA / Eclipse

📂 Project Structure

The project follows a modular design:

SimpleBlogApp.java - Main entry point and GUI Orchestration.

DatabaseService.java - Handles JDBC connections and SQL queries.

User.java (Abstract) - Base class for Polymorphism.

RegularUser.java & AdminUser.java - Concrete implementations.

BlogPost.java - Data model for posts.

AutoRefreshTask.java - Background thread implementation.

⚙️ Setup & Installation

Prerequisites

Java Development Kit (JDK) installed (Version 8 or higher).

MySQL Server installed and running.

MySQL Connector/J library added to your project dependencies.

Database Setup

Run the following SQL command in your MySQL Workbench or Command Line to create the database:

CREATE DATABASE blog_db;


Note: The application automatically creates the necessary tables (posts) upon the first successful connection.

Configuration

Open DatabaseService.java and update the credentials to match your local MySQL setup:

private static final String URL = "jdbc:mysql://localhost:3306/blog_db";
private static final String USER = "root";      // Your MySQL Username
private static final String PASS = "password";  // Your MySQL Password


▶️ How to Run

Clone the Repository:

git clone [https://github.com/your-username/java-blog-app.git](https://github.com/your-username/java-blog-app.git)


Open in IDE: Open the project folder in IntelliJ IDEA or Eclipse.

Add Dependencies: Ensure the mysql-connector-j-x.x.x.jar is added to your project's library/classpath.

Run: Execute the main method in SimpleBlogApp.java.

📸 Screenshots

<img width="671" height="670" alt="Screenshot 2025-11-22 184125" src="https://github.com/user-attachments/assets/d243616a-b788-420f-ac4b-f7649674a57b" />


📝 License

This project is for educational purposes as part of the Java Programming coursework.
