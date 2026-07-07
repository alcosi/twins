# Twins Relationship Model: Domain, Business Account, and User

## Core Idea

The Twins architecture is not a simple hierarchy.

Instead, it is a **flexible relationship network** built around three independent core entities:

* **Domain**
* **Business Account (BA)**
* **User**

These entities can exist independently, but become powerful when connected together.

The model is closer to a **triangle of relationships** than a strict tree hierarchy.

---

# Core Entities

## 1. Domain

A **Domain** defines a business ecosystem, platform logic, and operational rules.

The Domain owns:

* business schemas,
* workflows,
* statuses,
* automations,
* permissions,
* classes,
* processes,
* and shared business architecture.

A Domain acts as the **provider of the business environment**.

### Example

A Domain may represent:

* construction ecosystem,
* ERP platform,
* logistics platform,
* marketplace,
* HR system,
* supply management platform.

---

## 2. Business Account (BA)

A **Business Account** represents a real company, organization, or operational entity.

A BA owns:

* its employees,
* operational data,
* internal structure,
* projects,
* resources,
* and business operations.

Most importantly:

> A single Business Account can participate in multiple Domains simultaneously.

This means the same company may use different Domains for different purposes.

### Example

Company `BuildCorp` may work in:

* Construction Management Domain
* Procurement Domain
* HR Domain
* Financial Domain

using the same Business Account identity.

---

## 3. User

A **User** is a person interacting with the system.

Users are independent entities and may:

* exist without a BA,
* exist without a Domain,
* belong to multiple BAs,
* belong to multiple Domains.

The permissions and visibility of a User depend on the relationships established between:

* User ↔ Business Account
* User ↔ Domain

---

# Registration & Relationship Model

The system is based on explicit registrations and associations.

---

# Business Account Registration in Domain

A Business Account may register inside a Domain.

```text id="ba-domain"
Business Account → Domain
```

This means:

* the BA becomes part of the Domain ecosystem,
* gains access to Domain functionality,
* can use Domain workflows and business logic.

## Example

```text id="domain-example"
BuildCorp
    registers in
Construction Management Domain
```

---

# User Registration in Business Account

A User may join a Business Account.

```text id="user-ba"
User → Business Account
```

This defines:

* organizational membership,
* company-level permissions,
* operational access.

## Example

```text id="user-ba-example"
John Smith
    joins
BuildCorp
```

---

# User Registration in Domain

A User may also register directly in a Domain.

```text id="user-domain"
User → Domain
```

This grants:

* Domain-level access,
* workflow participation,
* visibility inside the Domain ecosystem.

---

# Full Relationship Combination

The most powerful scenario appears when all three relationships exist simultaneously.

```text id="triangle"
           Domain
           /    \
          /      \
         /        \
        /          \
Business Account --- User
```

In this model:

* BA participates in Domain
* User participates in BA
* User participates in Domain

As a result:

> The User can operate inside the Domain on behalf of the Business Account.

---

# Practical Meaning

This separation allows the platform to support:

* multi-tenant ecosystems,
* cross-company collaboration,
* marketplaces,
* contractor relationships,
* partner integrations,
* external employees,
* federated business environments.

---

# Example Scenario

## Construction Ecosystem

### Step 1 — Domain Exists

```text id="step1"
Construction Domain
```

The Domain defines:

* project workflows,
* statuses,
* approvals,
* procurement logic.

---

### Step 2 — Company Registers

```text id="step2"
BuildCorp
    registers in
Construction Domain
```

Now BuildCorp can use the Domain infrastructure.

---

### Step 3 — User Joins Company

```text id="step3"
John Smith
    joins
BuildCorp
```

John becomes a company employee.

---

### Step 4 — User Registers in Domain

```text id="step4"
John Smith
    registers in
Construction Domain
```

Now John can:

* access Domain workflows,
* participate in approvals,
* manage projects,
* interact with other companies inside the Domain,
* operate on behalf of BuildCorp.

---

# Key Architectural Advantage

This model provides strong separation between:

| Concept          | Responsibility                  |
| ---------------- | ------------------------------- |
| Domain           | Business ecosystem & rules      |
| Business Account | Company & operational ownership |
| User             | Human actor & permissions       |

Because they are independent entities:

* companies can participate in multiple ecosystems,
* users can collaborate across organizations,
* domains can operate as marketplaces or platforms,
* permissions become highly flexible and scalable.

---

# Simplified Conceptual View

```text id="simplified"
Domain
    = Ecosystem / Platform

Business Account
    = Company / Organization

User
    = Person / Actor
```

The platform behavior emerges from how these three entities are connected together.
