# Twins Architecture Overview

## What is Twins?

**Twins** is a cloud-based virtual management platform consisting of:

* Database layer
* Backend layer
* (Planned) Frontend layer

The main goal of Twins is to provide a highly flexible platform that can be adapted to different industries and business domains. Instead of building industry-specific software from scratch, organizations can use Twins as a foundation for creating solutions tailored to their business processes, internal management, and communication needs.

---

# Multi-Level Architecture

Twins is built around a strict hierarchical architecture consisting of three levels.

## Level 1 — Twins

The **Twins** level is the core of the platform.

It provides:

* Fundamental abstractions
* Data structures
* Workflow mechanisms
* Security model
* Core technologies

At this level, Twins acts as a universal processing engine capable of supporting solutions for completely different industries, from construction and logistics to IT and manufacturing.

The Twins database and backend serve as the foundation upon which all higher-level entities are built.

---

## Level 2 — Domains

A **Domain** represents an organization that uses Twins technology to create and provide a specific business solution.

Domains can:

* Build their own business processes
* Define their own subject area
* Configure workflows
* Manage users and permissions
* Provide services to connected organizations

### Example

Imagine a company called **Elpmee** that provides supply-chain and task-management solutions for construction companies.

In this case:

* Elpmee is a **Domain**
* Its business logic is implemented using Twins
* All operational data is stored within the Domain's Twins environment

---

## Level 3 — Business Accounts

A **Business Account** represents a company or organization that operates inside a Domain.

Business Accounts use:

* The Domain's subject area
* Configured business processes
* Existing workflows and management tools

### Example

Continuing the previous example:

* Elpmee remains the Domain
* Individual construction companies become Business Accounts
* These companies use Elpmee's configured classes, workflows, and processes to manage their daily operations

Business Accounts inherit the capabilities provided by the Domain while maintaining their own operational data and users.

---

# Users

A **User** is a person or entity that interacts with Twins-based software.

Users may exist on different levels of the hierarchy:

* As members of a Domain organization
* As members of a Business Account

Since both Domains and Business Accounts provide management functionality, users can participate in processes at either level depending on their responsibilities and permissions.

---

# Architecture Hierarchy

```text
Twins (Level 1)
│
├── Domain A (Level 2)
│   ├── Business Account A1 (Level 3)
│   ├── Business Account A2 (Level 3)
│   └── Business Account A3 (Level 3)
│
├── Domain B (Level 2)
│   ├── Business Account B1 (Level 3)
│   └── Business Account B2 (Level 3)
│
└── Domain C (Level 2)
    └── Business Account C1 (Level 3)
```

This hierarchy allows Twins to remain a universal platform while enabling each Domain to create highly specialized solutions for its own ecosystem of Business Accounts.
