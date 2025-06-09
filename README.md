# 🧾 XeroIMS – Advanced Inventory Management System

**XeroIMS** is a full-stack, production-ready inventory management system built with Spring Boot and MySQL. Designed for real-world deployment, it offers complete control over product flow, supplier data, pricing, transactions, and business analytics — all from a single dashboard.

> 🔒 While the code is complete and ready for deployment, cloud hosting (MySQL) is currently paused due to budget. A detailed demo video is available below.

---

## 🎥 Live Demo

[![Watch Demo] https://youtu.be/9xiHZxbinIg

📽️ **Click the link above to view the walkthrough of the system.**

---

## ⚙️ Tech Stack

- **Backend:** Java · Spring Boot · Maven · JPA (Hibernate)
- **Database:** MySQL (local/cloud-ready)
- **Frontend:** HTML · CSS · Thymeleaf
- **IDE:** IntelliJ IDEA

---

## 🔐 System Design

XeroIMS assumes **every user as an admin**, granting full access to all modules:


---

## ✅ Features

### 🗂️ Core Modules
- **Category**: Define product classifications
- **Product**: SKU entries linked to categories, stock-managed
- **Supplier**: Mapped to relevant categories
- **Pricing**: Supplier-specific pricing for each product

### 💳 Transactions & Billing
- **Sales & Purchases**:
  - Supplier selection
  - Auto calculate totals from pricing table
  - Supports cancellation or approval
- **Billing Module**:
  - Approved transactions automatically update stock levels
  - Tracks profit, balance, and expense entries
  - Highlights pending transactions

### 📊 Dashboard
- Graphs:
  - Inventory quantity trends per month
  - Sale vs Purchase (Monthly)
  - Top 5 products sold
- Stats Summary:
  - Total Sales, Purchases, Categories, Suppliers, Products

### 📁 Reports & Utilities
- Download Excel reports with custom filters
- View pending sales/purchases
- Profile section (User ID, name, email)
- Built-in FAQ/help page

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- MySQL (localhost or PlanetScale)
- Maven

### Setup

```bash
# Clone repo
git clone https://github.com/SharmaNishchay/xeroims.git
cd xeroims

# Configure DB in:
# src/main/resources/application.properties

# Build & Run
mvn clean install
mvn spring-boot:run
