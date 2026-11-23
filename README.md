<div align="center">

# ITTest

### Профориентационный тест на Android

Мобильный клиент учебного сервиса: войдите в аккаунт, выберите направление и ответьте на вопросы.

<br>

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Retrofit](https://img.shields.io/badge/Networking-Retrofit-3E4348)

</div>

---

## О проекте

ITTest — Android-приложение, которое подключается к учебному API профориентационного сервиса. Пользователь может зарегистрироваться или войти, получить список профессий и вопросов и пройти тест.

## Возможности

- Регистрация и вход в аккаунт.
- Загрузка списка профессий и связанных с ними вопросов.
- Работа с ответами через API.
- Проверка заполнения полей и совпадения паролей при регистрации.
- Обработка ошибок сервера и отсутствия соединения.

В исходном коде нет тестовых логинов и паролей. Используйте аккаунт своего сервера или создайте его через регистрацию.

## Запуск

1. Откройте папку `ITTest` в Android Studio.
2. Дождитесь синхронизации Gradle.
3. Проверьте адрес сервера в `app/src/main/java/ru/ittest/app/Api.kt`; если он недоступен, замените его на адрес своего API.
4. Запустите конфигурацию `app` на эмуляторе или Android-устройстве с доступом к серверу.

## Технологии

- Kotlin и Android SDK.
- Retrofit и Gson для HTTP-запросов и JSON.
- Минимальная версия Android: API 24.

## Структура

```text
app/src/main/java/ru/ittest/app/    экран приложения и API-клиент
app/src/main/res/                   макеты, строки и ресурсы Android
```

---

<div align="center">Android · Kotlin · Профориентация</div>
