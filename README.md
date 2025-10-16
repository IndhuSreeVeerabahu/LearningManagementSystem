# Learning Management System (LMS)

A comprehensive, full-featured Learning Management System built with Spring Boot, featuring role-based access control, JWT authentication, and a modern Udemy-like interface.

## Features

### Authentication & Authorization
- **JWT-based Authentication**: Secure login and session management
- **Role-based Access Control**: Three distinct user roles (Admin, Instructor, Student)
- **Secure Password Handling**: BCrypt encryption for password security

### Admin Module
- **User Management**: View, update, delete, and manage user roles
- **Course Approval**: Approve or reject courses created by instructors
- **System Overview**: Dashboard with key metrics and pending approvals
- **User Search**: Advanced search functionality for users

### Instructor Module
- **Course Creation**: Create and manage courses with rich content
- **Lesson Management**: Add lessons with text, videos, PDFs, images, and audio
- **Media Upload**: Support for multiple file types and formats
- **Student Tracking**: View enrolled students and their progress
- **Course Publishing**: Control course visibility and publication status

### Student Module
- **Course Browsing**: Browse and search available courses
- **Enrollment System**: Enroll in courses with duplicate prevention
- **Progress Tracking**: Track learning progress and completion
- **Lesson Viewing**: Access course materials and multimedia content
- **Learning Dashboard**: Personalized learning experience

### Course & Lesson Management
- **Rich Content Support**: Text, video, PDF, image, and audio content
- **Course Organization**: Structured lessons with ordering
- **Media Integration**: Seamless file upload and management
- **Progress Monitoring**: Real-time progress tracking

### Media Upload Module
- **Multi-format Support**: Videos, PDFs, images, and audio files
- **File Organization**: Organized storage with proper directory structure
- **Upload Management**: Secure file handling and validation

## Tech Stack

### Backend
- **Spring Boot 3**: Modern Java framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database operations
- **JWT**: Token-based authentication
- **MySQL**: Relational database
- **Maven**: Dependency management

### Frontend
- **Thymeleaf**: Server-side templating
- **Tailwind CSS**: Utility-first CSS framework
- **Responsive Design**: Mobile-first approach
- **Modern UI/UX**: Udemy-inspired interface

### Testing
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration testing

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd learning-management-system
```

### 2. Database Setup
1. Install MySQL 8.0+
2. Create a database named `lms_db`
3. Update database credentials in `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lms_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
```

### 3. Configure Application
Update the following in `src/main/resources/application.yml`:
- Database connection details
- JWT secret key
- File upload directory path

### 4. Run the Application
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

### 5. Create Initial Admin User
You can create an admin user through the registration endpoint or directly in the database.

## Project Structure

```
src/
├── main/
│   ├── java/com/lms/
│   │   ├── controller/          # REST and Web controllers
│   │   ├── entity/             # JPA entities
│   │   ├── repository/         # Data access layer
│   │   ├── service/            # Business logic layer
│   │   ├── security/           # Security configuration
│   │   ├── dto/                # Data transfer objects
│   │   └── LearningManagementSystemApplication.java
│   └── resources/
│       ├── templates/          # Thymeleaf templates
│       │   ├── auth/           # Authentication pages
│       │   ├── admin/          # Admin interface
│       │   ├── instructor/     # Instructor interface
│       │   ├── student/        # Student interface
│       │   └── layout/         # Base templates
│       └── application.yml     # Application configuration
└── test/                       # Test files
```

## User Roles

### Admin
- Manage all users and courses
- Approve/reject instructor courses
- System-wide administration
- Access to all modules

### Instructor
- Create and manage courses
- Add lessons with multimedia content
- View enrolled students
- Track student progress

### Student
- Browse and enroll in courses
- Access course materials
- Track learning progress
- View completion certificates

## UI Features

- **Responsive Design**: Works on desktop, tablet, and mobile
- **Modern Interface**: Clean, professional design inspired by Udemy
- **Intuitive Navigation**: Role-based navigation menus
- **Progress Visualization**: Progress bars and completion tracking
- **Search Functionality**: Advanced search across courses and users
- **File Upload**: Drag-and-drop file upload interface

## Database Schema

### Core Entities
- **User**: User accounts with role-based access
- **Course**: Course information and metadata
- **Lesson**: Individual lessons within courses
- **Enrollment**: Student-course relationships
- **Progress**: Learning progress tracking

### Key Relationships
- User (1) → (N) Course (as instructor)
- User (1) → (N) Enrollment (as student)
- Course (1) → (N) Lesson
- Course (1) → (N) Enrollment
- User (1) → (N) Progress (as student)

## Configuration

### JWT Configuration
```yaml
jwt:
  secret: your-secret-key-here
  expiration: 86400000  # 24 hours
```

### File Upload Configuration
```yaml
file:
  upload-dir: uploads/
```

### Database Configuration
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Testing

Run tests using Maven:
```bash
mvn test
```

Run specific test classes:
```bash
mvn test -Dtest=UserServiceTest
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/logout` - User logout

### Admin Endpoints
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/users` - User management
- `GET /admin/courses` - Course management
- `POST /admin/courses/{id}/approve` - Approve course
- `POST /admin/courses/{id}/reject` - Reject course

### Instructor Endpoints
- `GET /instructor/dashboard` - Instructor dashboard
- `GET /instructor/courses` - My courses
- `POST /instructor/courses` - Create course
- `GET /instructor/courses/{id}/lessons` - Course lessons
- `POST /instructor/courses/{id}/lessons` - Add lesson

### Student Endpoints
- `GET /student/dashboard` - Student dashboard
- `GET /student/courses` - Browse courses
- `POST /student/courses/{id}/enroll` - Enroll in course
- `GET /student/courses/{id}/lessons/{lessonId}` - View lesson

## Deployment

### Production Deployment
1. Update `application.yml` with production database credentials
2. Set secure JWT secret key
3. Configure file upload directory
4. Build the application:
```bash
mvn clean package
```
5. Deploy the generated JAR file to your server

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/learning-management-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- Create an issue in the repository
- Check the documentation
- Review the code comments

## Future Enhancements

- [ ] Real-time notifications
- [ ] Video streaming integration
- [ ] Mobile app development
- [ ] Advanced analytics dashboard
- [ ] Payment integration
- [ ] Certificate generation
- [ ] Discussion forums
- [ ] Live chat support
- [ ] Multi-language support
- [ ] Advanced reporting features

---

**Built with Spring Boot and modern web technologies**
