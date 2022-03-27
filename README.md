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

# Package Structure
    
    com.vaibhav.taskify    # Root Package
    .
    ├── data                # For data handling.
    |   ├── local           # Room DB and its related classes
    |   ├── remote          # Firebase, HarperDB and their relative classes
    │   ├── model           # Model data classes, both remote and local entities
    │   └── repo            # Single source of data.
    |
    ├── di                  # Dependency Injection             
    │   └── module          # DI Modules
    |
    ├── ui                  # UI/View layer
    |   ├── adapters        # All Adapters, viewholder and diffUtils for recyclerViews      
    │   ├── auth            # Authorization Activity and its fragments
    │   ├── mainScreen      # Home Activity and its fragments
    |   ├── addTaskScreen   # Add Task Activity and its fragments
    |   ├── onBoarding      # OnboardingScreen
    │   └── splashScreen    # SplashScreen
    |
    ├── service             # Timer Service and its related classes
    |
    └── utils               # Utility Classes / Kotlin extensions

