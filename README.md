# Secure Banking Application with 2FA

This is a comprehensive banking application designed to provide users with a secure and intuitive platform for managing their finances. The application incorporates robust security features, including Two-Factor Authentication (2FA) for sensitive operations, alongside essential banking functionalities like account management, transaction tracking, and scheduled bill payments.

## Features

- **User Authentication & Authorization**: Secure user registration, login, and access control using Spring Security.
- **Two-Factor Authentication (2FA)**: Implements TOTP (Time-Based One-Time Password) for enhanced security during critical transactions (e.g., withdrawals over a certain threshold) and initial registration.
- **Account Management**: Users can create and manage various account types (Savings, Checking, Business), and perform deposits, withdrawals, and transfers between their accounts.
- **Transaction History**: Detailed logging and viewing of all transactions, with filtering capabilities by date range and transaction type.
- **Bill Payments**:
  - **Add Payee**: Users can add and manage billers with details like bill type, account number, payment frequency, and next payment date.
  - **Manual Bill Payment**: Ability to make one-time bill payments.
  - **Scheduled Payments**: Automated processing of recurring bill payments based on configured frequency.
  - **Payment Reminders**: Email notifications for upcoming bill payments.
- **Suspicious Activity Alerts**: Automated email alerts for large withdrawals and bill payments to enhance security and fraud detection.
- **Email Notifications**: Utilizes JavaMailSender for various alerts and reminders.

## Technologies Used

The project leverages a modern Spring Boot stack for a robust and scalable architecture:

- **Backend**:
  - Spring Boot: Framework for building standalone, production-grade Spring applications.
  - Spring Security: Provides comprehensive security services for Java EE-based enterprise software applications.
  - Spring Data JPA: Simplifies data access with JPA repositories.
  - Hibernate: ORM framework for mapping Java objects to relational database tables.
  - Google Authenticator (TOTP): For generating and verifying time-based one-time passwords for 2FA.
  - JavaMailSender: For sending email notifications.
  - Lombok: Reduces boilerplate code (e.g., getters, setters, constructors).

- **Database**:
  - MySQL: The primary relational database used for storing all application data.
  - H2 Database (optional/in-memory): While the provided `application.properties` specifies MySQL, H2 can be easily configured for local development and testing if desired.

- **Build Tool**:
  - Maven: For project dependency management and build automation.

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- Java 17 or higher
- Maven
- MySQL Database (running locally or accessible)
- An IDE (e.g., IntelliJ IDEA, Visual Studio Code, Eclipse)

### Installation

1. **Clone the repository**:
    ```bash
    git clone https://github.com/felipekei/BankingApp-2FA.git
    cd BankingApp-2FA
    ```

2. **Set up your MySQL Database**:
    - Create a new database named `BankingAppFB` (or any name of your choice, just update `application.properties` accordingly).
    - Create a user (e.g., `admin` with password `admin`) that has full access to this database.
    - Note: The application uses `spring.jpa.hibernate.ddl-auto=update`, which means Hibernate will automatically create or update the database schema based on your JPA entities when the application starts.

3. **Configure `application.properties`**:
    - Open `src/main/resources/application.properties`.
    
    - **Database Configuration**:
      ```properties
      # MySQL Configuration
      spring.datasource.url=jdbc:mysql://localhost:3306/BankingAppFB?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      spring.datasource.username=admin
      spring.datasource.password=admin
      ```

    - **Email Service Configuration**:
      ```properties
      # Spring Mail Configuration - Needs to be updated with your credentials
      spring.mail.host=smtp.gmail.com
      spring.mail.port=587
      spring.mail.username=your_email@gmail.com # <--- ENTER YOUR EMAIL HERE
      spring.mail.password=your_app_password     # <--- ENTER YOUR APP PASSWORD HERE
      spring.mail.properties.mail.smtp.auth=true
      spring.mail.properties.mail.smtp.starttls.enable=true
      ```

    - **Server Port**:
      ```properties
      server.port=8081
      ```

4. **Build the project**:
    ```bash
    mvn clean install
    ```

5. **Run the application**:
    ```bash
    mvn spring-boot:run
    ```

The application will now be accessible at `http://localhost:8081`.

## Usage

1. **Register**: Navigate to `/register` to create a new user account. You'll be prompted to set up 2FA using a QR code and a secret key.
2. **Login**: After successful 2FA setup, log in at `/login`.
3. **Dashboard**: Once logged in, you'll be redirected to your account dashboard where you can manage accounts, view balances, and initiate transactions.
4. **Transactions**: Access the transaction history page to view filtered logs of your deposits, withdrawals, and transfers.
5. **Bill Payments**: Manage your billers and make payments from the bill payments section.

## Project Structure (High-Level)

- `src/main/java/com/example/BankingAppFB/`:
  - `controller/`: Handles incoming web requests and prepares model data for views.
  - `model/`: Defines the data structures (entities) for the application (`User`, `Account`, `TransactionLog`, `Biller`, `PendingTransaction`).
  - `repository/`: Interfaces for data access operations using Spring Data JPA.
  - `service/`: Contains the core business logic and interacts with repositories.
  - `config/`: Spring Security configurations.
  - `enums/`: Custom enumerations for account types, transaction types, bill types, etc.

- `src/main/resources/`:
  - `application.properties`: Application configuration.
  - `templates/`: Thymeleaf templates for the web UI.
  - `static/`: Static resources like CSS and JavaScript.

## License
This project is licensed under MIT License.
