# Authentication Project

### Secure your application with robust authentication and registration features
This project was developed in collaboration with frontend developer.

## Description

The Authentication Project is designed to provide a secure and efficient authentication and registration system for web applications. Utilizing technologies such as Spring Boot, JMS, and PostgreSQL, this application ensures reliable user management. Key features include email verification, password reset functionality, and seamless integration with frontend and mobile platforms.

## Built With

- [Spring Boot](https://spring.io/projects/spring-boot) - Server framework
- [Spring Security](https://spring.io/projects/spring-security) - Security
- [JMS (Java Mail Sender)](https://javaee.github.io/javamail/) - Email service
- [PostgreSQL](https://www.postgresql.org/) - Database
- [Swagger](https://swagger.io/) - Documentation

## Installation

To install the application, follow these steps:

1. Clone the repository:
   ```
   git clone https://github.com/nastenka-ooops/Auth-Project.git
   cd Auth-Project
   ```

2. If you have Maven installed locally, run:
   ```
   mvn clean install
   ```

   If you do not have Maven installed, run:
   ```
   ./mvnw clean install
   ```

## Testing

To run tests, open a terminal in the root directory and type:
```
mvn clean test
```
All test cases can be found in the `test` directory of the project.

## How To Run

After a successful installation, set the following variables and run the application:

1. Run the application with the required variables:
   ```
   java -jar target/*.jar --DB_HOST=your_db_host DB_NAME=your_db_name DB_PASSWORD=your_db_password DB_PORT=yout_db_port DB_USERNAME=your_db_username MAIL_PASSWORD=your_mail_password MAIL_USERNAME=your_mail_address
   ```

## How To Use

Visit the Swagger documentation to explore and test the API endpoints developed for the authentication and registration system.

## Documentation

[Swagger Documentation](https://auth-project-production.up.railway.app/swagger-ui/index.html)

## Authors

- **Backend Developer** - [Brutskaya Anastasia](https://github.com/nastenka-ooops)
- **Frontend Developer** - [Ainazik Momunalieva](https://github.com/ainaziko)
