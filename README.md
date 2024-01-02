# transfers (REST API services)
## Назначение
Идея проекта заключается в возможности проведения переводов по номеру счета и по номеру телефона в 3х разных валютах (рубли, доллары, белорусские рубли).
## Описание
Проект состоит из 4х микросервисов:
* сервис учетных записей (pet-backend-users)
* сервис счетов клиента (pet-backend-accounts)
* сервис переводов (pet-backend-transfers)
* сервис истории операций (pet-backend-history)
## Сборка и запуск
**Склонируйте репозиторий и перейдите в корень проекта:**
```bash
git clone https://github.com/tolstikhin-vn/transfers.git
cd transfers
```
**В директории каждого микросервиса произведите "Link Gradle Project" для settings.gradle**

**Запустите Docker**

**В директории каждого микросервиса выполните команду для сборки:**
```bash
./gradlew clean build
```
**После успешной сборки выполните команды для запуска проекта в Docker:**
```bash
docker-compose up -d --build
docker-compose up
```
## API:
### pet-backend-users
**Base URL:** localhost:8081<br>
**Endpoint (POST)** /users - создание учетной записи<br> 
**Endpoint (GET)** /users/{id} - получение данных по клиенту<br> 
**Endpoint (GET)** /users/phone-number/{phoneNumber} - получение данных по клиенту по номеру телефона<br> 
**Endpoint (PUT)** /users/{id} - изменение данных по клиенту<br> 
**Endpoint (DELETE)** /users/{id} - удаление клиента

### pet-backend-accounts
**Base URL:** localhost:8082<br> 
**Endpoint (POST)** /accounts - создание счета<br> 
**Endpoint (GET)** /accounts/{clientId} - получение счетов клиента<br> 
**Endpoint (DELETE)** /accounts/{accountNumber} - удаление счета<br> 
**Endpoint (GET)** /accounts/balance/{accountNumber} - получение баланса<br> 
**Endpoint (PUT)** /accounts/balance/{accountNumber} - изменение/пополнение баланса

### pet-backend-transfers
**Base URL:** localhost:8083<br> 
**Endpoint (POST)** /transfers - операция перевода денег<br> 
**Endpoint (GET)** /transfers/{uuid} - получения информации о транзакции

### pet-backend-history
**Base URL:** localhost:8084<br> 
**Endpoint (GET)** /history/{clientId} - получение истории операция по id клиента
## Формат входящих/исходящих параметров (пример)
**Входящие параметры (Request body):**  
```json
{
 "lastName": "Ivanov",
 "firstName": "Ivan",
 "fatherName": "Ivanovich",
 "phoneNumber": "79999999999",
 "birthDate": "22.09.1990",
 "passportNumber": "4510666666",
 "email": "ivan@mail.ru"
}

```
**Исходящие параметры:**
```json
{ 
 "clientId": 1,
 "message": "Пользователь успешно создан"
}
```

## Языки и инструменты
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?&style=for-the-badge&logo=java&logoColor=white")
![Spring](https://img.shields.io/badge/spring%20-%236DB33F.svg?&style=for-the-badge&logo=spring&logoColor=white")
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?&style=for-the-badge&logo=postgresql&logoColor=white")
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
## Социальные сети
[![Telegram](https://img.shields.io/badge/-Telegram-090909?style=for-the-badge&logo=telegram&logoColor=27A0D9)](https://t.me/suun_rise)
