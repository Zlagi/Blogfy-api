# Blogfy (API)

[![Build (API)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml/badge.svg)](https://github.com/Zlagi/blogfy-api/actions/workflows/run-build.yml)

Blogfy backend _REST API_ is built with Ktor framework with PostgreSQL as database and deployed on the Heroku.

Currently this API is deployed on _`https://blogfy-server.herokuapp.com`_. You can try it 😃.

[![Blogfy](https://img.shields.io/badge/Blogfy✅-APK-red.svg?style=for-the-badge&logo=android)](https://github.com/Zlagi/Blogfy/releases/tag/4)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6.10-orange.svg?logo=kotlin)](http://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/ktor-1.6.7-orange.svg?logo=kotlin)](https://ktor.io)

## Features 👓

- Easy structure
- Authentication
- Automatic and easy deployment to Heroku
- Test cases

# Package Structure
    
    dev.zlagi.application    # Root Package
    .
    ├── application                
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
    └── data
        ├── dao       
        ├── database        
        ├── di            
        ├── entity          
        └── model          

