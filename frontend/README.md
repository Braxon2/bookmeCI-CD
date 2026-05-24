# BookMe — Frontend Application

> A modern, responsive web application for the BookMe ecosystem.

BookMe Frontend allows users to discover properties, manage bookings, and view dynamic pricing, while providing hosts with a comprehensive dashboard to manage their listings. Built with a focus on performance and accessibility, it integrates directly with the [BookMe Backend](https://github.com/Braxon2/BookMe-app) via secure RESTful APIs.

---

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)

---

## Features

- **Dynamic Property Discovery** — High-performance browsing and filtering of properties based on location, price, and availability.
- **Interactive Host Dashboard** — A dedicated space for property owners to manage listings, update seasonal pricing, and track bookings.
- **Modern UI/UX** — A sleek, accessible interface built with Mantine 8, featuring dark/light mode support and responsive layouts.
- **Secure Authentication** — Integrated JWT-based authentication flow for guest and host accounts.
- **Media Gallery** — Optimized display of property images fetched via AWS S3 through the backend service.
- **Real-time Validation** — Client-side form handling and validation for booking requests and property creation.

---

## Technologies

| Layer                        | Technology                    |
| ---------------------------- | ----------------------------- |
| Library                      | React 19.2.0                  |
| UI Component Framework       | Mantine 8.3.15                |
| Runtime Environment          | Node.js 24.14.1               |
| Build Tool & Package Manager | npm v11+                      |
| Styling                      | CSS Modules / Mantine Theming |

---

## Prerequisites

Before getting started, ensure you have the following:

- [Node.js v24+](https://nodejs.org/)
- [npm v11+](https://www.npmjs.com/)
- A running **BookMe Backend** instance with a database initialized — see the [backend repository](https://github.com/Braxon2/BookMe-app) for setup instructions.

---

## Installation & Setup

**1. Clone the repository:**

```bash
git clone https://github.com/Braxon2/BookMe-front.git
```

**2. Navigate to the project root:**

```bash
cd BookMe-front
```

**3. Install dependencies:**

```bash
npm install
```

**4. Start the development server:**

```bash
npm run dev
```

The application will be available at `http://localhost:5173` by default (Vite's standard dev port).
