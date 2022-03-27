# Blogfy (API)

[![Build (API)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml/badge.svg)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml)

Blogfy backend _REST API_ is built with Ktor framework with PostgreSQL as database and deployed on the Heroku.

Currently this API is deployed on _`https://blogfy-server.herokuapp.com`_. You can try it 😃.

[![Blogfy](https://img.shields.io/badge/Blogfy✅-APK-red.svg?style=for-the-badge&logo=android)](https://github.com/Zlagi/Blogfy/releases/tag/4)

## Features 👓

- Easy structure
- Authentication
- Automatic and easy deployment to Heroku
- Test cases

.
├── Application.kt
├── config
│   └── AppConfig.kt
├── database
│   ├── DatabaseFactory.kt
│   └── DatabaseFactoryImpl.kt
├── di
    └── AppModule.kt


