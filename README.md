📦 Mini E-Shop – Android App

A modern e-commerce mobile application built with Jetpack Compose, MVVM, Hilt, Room, Firebase Auth, Firestore, and DataStore.
The app supports authentication, product browsing, cart management, orders, favorites, localization, theming, and an admin mode for managing products.

✨ Features
👤 Authentication

Login & Register using Firebase Authentication

“Remember me” support

Persistent login using DataStore

🛍️ Shopping Experience

Product list with category filter, sorting, search

Product detail screen with stock status

Add to cart + checkout flow

Favorite products

Order history (placeholder or implemented depending on version)

🛠️ Admin Features

Admins can:

Add new product

Edit product

Delete product

Manage inventory (stock quantity)

Admin state is synced in real-time with Firestore.

🌐 Localization

Supports English & Vietnamese

Language is saved in DataStore

App UI updates instantly when language changes

🎨 Dynamic Theming

User can choose:

Light

Dark

System default theme

☁️ Offline-First Data Layer

Products synced from Firestore → saved to Room

UI always reads from Room for performance & offline support

🏗️ Architecture Overview
Presentation Layer (Jetpack Compose UI)
        │
        ▼
ViewModel (MVVM, StateFlow, business logic)
        │
        ▼
Domain Layer (Repositories Interfaces + Models)
        │
        ▼
Data Layer
    ├── Room (Local DB)
    ├── Firestore (Remote DB)
    ├── DataStore (Preferences)

📌 Key Principles

Unidirectional data flow

Repository pattern

Dependency Injection with Hilt

Reactive Streams using Kotlin Flow

Single-activity architecture using Navigation Compose

🔧 Tech Stack
UI

Jetpack Compose (Material 3)

Navigation Compose

Coil for image loading

Architecture

MVVM + Repository pattern

Kotlin Coroutines + Flow

Data

Firebase Authentication

Firebase Firestore

Room Database

DataStore Preferences

Dependency Injection

Dagger Hilt

📁 Project Structure
com.example.mini_e_shop
│
├── data
│   ├── local (Room)
│   │   ├── entity
│   │   ├── dao
│   │   └── database
│   ├── remote (Firebase)
│   ├── repository (Repository Implementations)
│   └── preferences (DataStore)
│
├── domain
│   ├── model
│   └── repository (Repository Interfaces)
│
├── presentation
│   ├── auth
│   ├── main
│   ├── products_list
│   ├── product_detail
│   ├── cart
│   ├── favorites
│   ├── profile
│   ├── orders
│   ├── checkout
│   ├── add_edit_product
│   ├── settings
│   └── support
│
├── ui.theme (Custom colors & theme)
└── MainActivity.kt (Single NavHost)

🚀 How the App Works
🔄 Authentication Flow

User logs in → Firebase Auth verifies credentials

User profile is loaded from Firestore

User data saved to Room + DataStore

Navigation switches to Main Screen

🛒 Product Flow

Products are downloaded from Firestore

Auto-sync using snapshot listeners

Saved into Room

UI reads from Room using Flow → instant updates

❤️ Favorites & Cart

Stored locally via Room

Connected to logged-in user using their ID

▶️ Screenshots

(Add your screenshots here in the README)

/screenshots/home.png
/screenshots/product_detail.png
/screenshots/cart.png
...

🛠️ Build & Run
Requirements

Android Studio Hedgehog or newer

Min SDK 24

Kotlin 1.9+

Steps

Clone repository

Set up Firebase:

Enable Authentication (Email/Password)

Create Firestore DB

Add your google-services.json

Build & run from Android Studio

🔥 Future Improvements

Online cart sync to Firestore (multi-device)

Order confirmation with backend payments

Push notifications for promotions

Analytics dashboard for admin

🤝 Contributing

Feel free to open issues or submit pull requests.

📜 License

MIT License — free to use and modify.
