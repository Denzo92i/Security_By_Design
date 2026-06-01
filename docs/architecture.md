# Technical Architecture - Task 1

## 1. Overview
The system implements a Layered Architecture to ensure separation between business logic and external cloud services.

## 2. Access Matrix (Security)
Access control is managed via HTTP verbs using Spring Security:

| Method | Action | RW Role (Admin) | RO Role (User) |
| :--- | :--- | :--- | :--- |
| **GET** | Read | 200 OK | 200 OK |
| **POST** | Create | 201 Created | 405 Method Not Allowed |

## 3. Google Drive Integration Flow
The diagram below illustrates the lifecycle of a customer folder creation:

```mermaid
graph TD
    A[Postman Client] -->|POST| B[CustomerController]
    B --> C[CustomerService]
    C --> D[GoogleDriveService]
    D -->|JSON Metadata| E((Google Drive API))
    E -->|Folder ID| C
    C -->|Save| F[Database]