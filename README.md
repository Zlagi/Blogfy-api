# Blogfy (API)

[![Build (API)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml/badge.svg)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml)

Blogfy backend _REST API_ is built with Ktor framework with PostgreSQL as database and deployed on the Heroku.

Currently this API is deployed on _`https://blogfy-server.herokuapp.com`_. You can try it 😃.

[![Blogfy](https://img.shields.io/badge/Blogfy✅-APK-red.svg?style=for-the-badge&logo=android)](https://github.com/Zlagi/Blogfy/releases/tag/4)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6.10-orange.svg?logo=kotlin)](http://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/ktor-1.6.7-orange.svg?logo=kotlin)](https://ktor.io)

# Features 👓

- Authentication for email based auth.
- Authentication for Google identity provider (authenticate with Firebase JWT).
- Refresh and revoke Ktor JWT.
- Create, update, and delete blog.
- Check blog author.
- Fetch blogs with pagination.
- Fetch account properties and update account password.
- Validate requests body and authorization header (custom Ktor JWT challenge).
- Automatic and easy deployment to Heroku

# Package Structure
    
    dev.zlagi.application    # Root Package
    .
    ├── application          # Ktor application entry point and API routes
    |   ├── auth             
    |   ├── controller        
    │   ├── exception        
    │   ├── model            
    │   ├── plugins          
    │   ├── router           
    │   ├── utils            
    │   └── Application.Kt   
    │
    |
    └── data                 # Data source and operations.
        ├── dao       
        ├── database        
        ├── di            
        ├── entity          
        └── model
        
# Built With 🛠
- [Ktor](https://ktor.io/) - Ktor is an asynchronous framework for creating microservices, web applications, and more. It’s fun, free, and open source.
- [Firebase_Admin](https://firebase.google.com/docs/admin/setup) - The Admin SDK is a set of server libraries that lets you interact with Firebase.
- [Exposed](https://github.com/JetBrains/Exposed) - An ORM/SQL framework for Kotlin.
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) - JDBC Database driver for PostgreSQL.
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - High performance JDBC connection pooling.
- [Koin](https://insert-koin.io/docs/reference/koin-ktor/ktor/) - Dependency injection framework.
- [jBCrypt](https://www.mindrot.org/projects/jBCrypt/) - Password hashing algorithm.
- [Commons_Email](https://commons.apache.org/email/) - An API for sending email.

# REST API Specification

## Authentication

### Sign Up

```http
POST http://localhost:8080/auth/signup
Content-Type: application/json

{
    "email" : "test@gmail.com",
    "username" : "user",
    "password": "12346789",
    "confirmPassword" : "12346789"
}

```

### Sign In

```http
POST http://localhost:8080/auth/signin
Content-Type: application/json

{
    "email" : "test@gmail.com",
    "password": "12346789"
}

```

### Google Identity Provider (single endpoint for both signin and signup)

```http
POST http://localhost:8080/auth/idp/google
Content-Type: application/json
Authorization: Bearer YOUR_FIREBASE_AUTH_TOKEN

{
    "username" : "user"
}

```

### Refresh Ktor JWT

```http
POST http://localhost:8080/auth/token/refresh
Content-Type: application/json

{
    "token" : "token"
}

```

### Revoke Ktor JWT

```http
POST http://localhost:8080/auth/token/revoke
Content-Type: application/json

{
    "token" : "token"
}

```

## Blog operations

### Get all blogs by query

# Without query parameters
```http
GET http://localhost:8080/blog/list
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```
# With query parameters
```http
GET http://localhost:8080/blog/list?search_query=test&page=2&limit=5
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```

### Create New Note

```http
POST http://localhost:8080/note/new
Content-Type: application/json
Authorization: Bearer YOUR_AUTH_TOKEN

{
  "title": "Hey there! This is title",
  "note": "Write note here..."
}
```

### Update Note

```http
PUT http://localhost:8080/note/NOTE_ID_HERE
Content-Type: application/json
Authorization: Bearer YOUR_AUTH_TOKEN

{
  "title": "Updated title!",
  "note": "Updated body here..."
}
```

### Delete Note

```http
DELETE http://localhost:8080/note/NOTE_ID_HERE
Content-Type: application/json
Authorization: Bearer YOUR_AUTH_TOKEN
```
