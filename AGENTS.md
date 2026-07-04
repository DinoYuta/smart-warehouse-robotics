# AGENTS.md

## Project Name

Smart Warehouse Robotics Decision Engine using Interpreter and Strategy Patterns

---

# Project Vision

This project is a smart warehouse robotics decision system inspired by:

* SAP EWM
* Amazon Robotics
* Enterprise Logistics Platforms

The system dynamically evaluates warehouse conditions using:

* Interpreter Pattern
* Strategy Pattern

The goal is to simulate how warehouse robots can make runtime decisions without hardcoded logic.

---

# Core Architecture

## Interpreter Pattern

Used for:

* parsing rule conditions
* evaluating robot states
* executing dynamic business rules

Example:
IF battery < 20 THEN ChargingStrategy

---

## Strategy Pattern

Used for:

* dynamically changing robot behavior
* runtime action selection

Example Strategies:

* ChargingStrategy
* FastRouteStrategy
* ObstacleAvoidanceStrategy
* EnergySavingStrategy

---

# Main System Modules

## 1. Dashboard Module

Displays:

* robot status
* warehouse activity
* active strategies
* recent executions

---

## 2. Rule Engine Module

Handles:

* rule creation
* rule parsing
* expression evaluation
* rule execution

---

## 3. Robot Management Module

Handles:

* robot monitoring
* current strategy
* battery level
* robot state

---

## 4. Strategy Module

Handles:

* strategy implementations
* runtime behavior switching

---

## 5. Simulation Module

Allows:

* testing robot conditions
* visualizing rule execution
* displaying interpreter results

---

# Technology Stack

## Backend

* Java Spring Boot

## Frontend

* JSP + Bootstrap OR React

## Database

* Microsoft SQL Server

---

# Project Rules

## DO

* Keep architecture modular
* Focus on design patterns
* Keep rule engine dynamic
* Maintain clean OOP structure
* Use reusable services

---

## DON'T

* Hardcode robot behaviors
* Create giant if-else chains
* Overcomplicate AI logic
* Add unnecessary features early

---

# Coding Standards

## Naming

* PascalCase for classes
* camelCase for variables
* Clear descriptive names

---

## Package Structure

com.warehouse
├── controller
├── service
├── repository
├── model
├── strategy
├── interpreter
├── simulator
├── dto
└── config

---

# Initial Supported Conditions

* battery
* obstacleDetected
* robotLoad
* distance
* priority

---

# Initial Supported Operators

* >
* <
* > =
* <=
* ==
* AND
* OR

---

# Initial Supported Strategies

* ChargingStrategy
* FastRouteStrategy
* SafeRouteStrategy
* ObstacleAvoidanceStrategy
* EnergySavingStrategy

---

# Long-Term Goal

Transform the project from:

* educational prototype

into:

* enterprise logistics decision engine prototype
* smart warehouse simulation platform
