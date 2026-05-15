# 🎓 Advanced Java Programming (TPO & UTP)

![Java](https://img.shields.io/badge/Language-Java_11%2B-orange.svg)
![Status](https://img.shields.io/badge/Status-Completed-success.svg)
![Concepts](https://img.shields.io/badge/Concepts-NIO%20%7C%20Concurrency%20%7C%20Networking-blue.svg)

A comprehensive collection of advanced Java projects developed during the **Object-Oriented Programming Technologies (TPO)** and **Universal Programming Techniques (UTP)** courses. This repository demonstrates proficiency in modern Java APIs, asynchronous programming, networking, and data processing.

## 📖 Repository Overview

This repository contains various independent projects, each focusing on specific advanced Java concepts. From custom NIO chat servers to multi-threaded Swing applications and data stream processors, these projects highlight robust and scalable software design.

### ✨ Highlighted Concepts
- **Java NIO (New I/O)**: Non-blocking client-server architectures and high-performance file processing.
- **Concurrency & Multithreading**: Utilizing `ExecutorService`, `Future`, and Virtual Threads to handle asynchronous tasks securely.
- **Data Streams & Lambdas**: Fluent data manipulation, filtering, and transformation using the Java Streams API.
- **Web Integrations**: Combining JavaFX with external web APIs (REST, JSON parsing) and embedded WebViews.
- **Localization (i18n)**: Handling dynamic data formatting and translation based on `pl_PL`, `en_GB`, and other locales.

---

## 🗂 Project Structure

| Project | Description | Core Technologies |
| :--- | :--- | :--- |
| 🌐 **`Web-Service-Integration`** | A JavaFX application that interacts with external REST APIs (OpenWeather, Fixer) to fetch weather and exchange rates, displaying results alongside an embedded Wikipedia WebView. | `JavaFX`, `REST API`, `JSON`, `WebEngine` |
| 🗃️ **`NIO-File-Processor`** | A high-performance file utility that traverses directory trees and processes text files using memory-mapped buffers and non-blocking I/O. | `java.nio.file`, `FileChannel`, `ByteBuffer` |
| 💬 **`NIO-Chat-Application`** | A non-blocking chat server and client implementation allowing multiple clients to connect, send messages, and disconnect asynchronously without threads locking. | `Selector`, `SocketChannel`, `NIO` |
| ⚡ **`Async-Chat-Application`** | An evolution of the chat server utilizing modern asynchronous techniques and virtual threads for extreme scalability. | `Concurrency`, `Virtual Threads` |
| ✈️ **`Flight-Data-Streams`** | A robust data processing tool that reads raw flight and destination strings, transforming them using advanced lambda expressions and stream mapping. | `Streams API`, `Lambdas`, `Regex` |
| 📁 **`Directory-Processor-Utility`** | An advanced file traversal utility demonstrating directory stream walking and deep file content manipulation. | `java.nio.file.Files`, `Paths` |
| 🔄 **`Concurrent-Swing-Tasks`** | A Swing-based application that safely handles heavy background computations using `TaskWrapper` and multithreading, ensuring the UI remains responsive. | `Swing`, `ExecutorService`, `Concurrency` |
| 🌍 **`Travel-Data-Database`** | A comprehensive localization project that reads travel offers in multiple languages, formats dates and currencies dynamically, and stores them in a local SQLite database with a GUI viewer. | `JDBC`, `SQLite`, `Locale`, `NumberFormat` |

---

## 🚀 Getting Started

Each project is designed to be self-contained. To run any of the projects locally:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AndrewPerchekliy/TPO-Project.git
   cd TPO-Project
   ```

2. **Open in your IDE:**
   Import the repository into IntelliJ IDEA, Eclipse, or your preferred IDE. Each folder acts as its own module or project.

3. **Run the Main Class:**
   Navigate to the `src` directory of the project you want to test and execute the `Main.java` class.

> [!NOTE]
> Some projects, like the `Web-Service-Integration`, require external API keys (e.g., OpenWeatherMap, Fixer.io) passed as environment variables or VM arguments.

---
*Developed by **Andrii Percheklii** as part of academic coursework.*
