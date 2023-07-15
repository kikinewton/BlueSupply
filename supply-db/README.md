# Database Migration Service

## Overview

The Database Migration Service is a service responsible for handling database migrations and performing daily backups. It utilizes the Flyway database migration tool to manage and execute changes to the database schema. Additionally, it includes a scheduler for automating the daily backup process.

## Features

- **Database Schema Migration**: The service leverages Flyway to manage and apply database schema migrations in a reliable and organized manner.

- **Scheduled Database Backup**: The built-in scheduler automates the daily backup process to ensure data safety and easy restoration if needed.

## Getting Started

Follow the steps below to set up and use the Database Migration Service:

1. Install the necessary dependencies and configure the service to match your environment.

2. Place the Flyway migration scripts in the designated folder to apply the required database schema changes.

3. Configure the scheduler to execute the daily database backup. Adjust settings such as backup location to suit your needs.

4. Start the service and monitor the logs to ensure successful execution of schema migrations and backups.

## Usage

Once the service is running, it will automatically apply pending database schema migrations using Flyway, keeping the schema up-to-date. The scheduler initiates a daily backup for added data protection.

