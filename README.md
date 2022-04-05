# Blogfy (API)

[![Build (API)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml/badge.svg)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6.10-orange.svg?logo=kotlin)](http://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/ktor-1.6.7-orange.svg?logo=kotlin)](https://ktor.io)

Blogfy backend _REST API_ is built with Ktor framework with PostgreSQL as database and deployed on the Heroku.

Currently this API is deployed on _`https://blogfy-server.herokuapp.com`_. You can try it 😃.

# Features 👓

- Authentication for email based auth.
- Authentication for Google identity provider (authenticate with Firebase JWT).
- Refresh and revoke Ktor JWT.
- Create, update, and delete blog.
- Check blog author.
- Fetch blogs with pagination.
- Fetch account properties and update account password.
- Send push notifications to android clients.
- Validate requests body and authorization header (custom Ktor JWT challenge).
- Automatic and easy deployment to Heroku.


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
- [Firebase Admin](https://firebase.google.com/docs/admin/setup) - The Admin SDK is a set of server libraries that lets you interact with Firebase.
- [One Signal](https://onesignal.com) - An Api for Push Notifications, Email, SMS & In-App..
- [Exposed](https://github.com/JetBrains/Exposed) - An ORM/SQL framework for Kotlin.
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) - JDBC Database driver for PostgreSQL.
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - High performance JDBC connection pooling.
- [Koin](https://insert-koin.io/docs/reference/koin-ktor/ktor/) - Dependency injection framework.
- [jBCrypt](https://www.mindrot.org/projects/jBCrypt/) - Password hashing algorithm.
- [Commons Email](https://commons.apache.org/email/) - An API for sending email.

# REST API Specification

## Authentication

### Sign up

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

### Sign in

```http
POST http://localhost:8080/auth/signin
Content-Type: application/json

{
    "email" : "test@gmail.com",
    "password": "12346789"
}

```

### Google 
#### ⚠️ single endpoint for both signin and signup.

```http
POST http://localhost:8080/auth/idp/google
Content-Type: application/json
Authorization: Bearer YOUR_FIREBASE_AUTH_TOKEN

{
    "username" : "user"
}

```

### Refresh ktor token

```http
POST http://localhost:8080/auth/token/refresh
Content-Type: application/json

{
    "token" : "token"
}

```

### Revoke ktor token

```http
POST http://localhost:8080/auth/token/revoke
Content-Type: application/json

{
    "token" : "token"
}

```

### Send reset password link

```http
POST http://localhost:8080/auth/reset-password
Content-Type: application/json

{
    "email" : "test@gmail.com"
}

```

### Confirm reset password

```http
POST http://localhost:8080/auth/confirm-reset-password?token=KTOR_AUTH_TOKEN
Content-Type: application/json

{
    "currentPassword": "oldpassword",
    "newPassword": "newpassword",
    "confirmNewPassword": "newpassword"
}

```

## Blog operations

### Get all blogs by query

#### ⚠️ without query parameters
```http
GET http://localhost:8080/blog/list
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```
#### ⚠️ with query parameters
```http
GET http://localhost:8080/blog/list?search_query=test&page=2&limit=5
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```

### Create New Blog
#### ⚠️ creation time is sent from android client side.

```http
POST http://localhost:8080/blog
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN

{
  "title": "Hey there! This is title",
  "description": "Write some description here...",
  "creationTime": "Date: 2022-03-07 Time: 22:10:56"
}
```

### Update Blog
#### ⚠️creation time is sent from android client side.

```http
PUT http://localhost:8080/blog/BLOG_ID_HERE
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN

{
  "title": "Updated title!",
  "note": "Updated body here...",
  "creationTime": "Date: 2022-03-07 Time: 22:20:38"
}
```

### Delete Blog

```http
DELETE http://localhost:8080/blog/NOTE_ID_HERE
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```

### Check Blog Author

```http
DELETE http://localhost:8080/blog/NOTE_ID_HERE/is_author
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```

### Send push notifications

```http
POST http://localhost:8080/blog/notification
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```

## Account operations

### Get Account

```http
Get http://localhost:8080/account
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN
```

### Update Password

```http
PUT http://localhost:8080/account/password
Content-Type: application/json
Authorization: Bearer KTOR_AUTH_TOKEN

{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword",
  "confirmNewPassword": "newpassword"
}
```

## Inspiration

This is project is a sample, to inspire you and should handle most of the common cases, but please take a look at
additional resources.

### Android projects

Other high-quality projects will help you to find solutions that work for your project:

- [NotyKT](https://github.com/PatilShreyas/NotyKT/tree/master/noty-api)
- [KtorEasy](https://github.com/mathias21/KtorEasy)
- [Ktor-pushnotification](https://github.com/philipplackner/com.plcoding.ktor-pushnotification)

## Contribute

* Bug fixes and Pull Requests are highly appreciated and you're more than welcome to send us your feedbacks <3

## License

    Copyright 2022 Haythem Mejerbi.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
