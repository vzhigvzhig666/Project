# Тестирование Alkaa

Основа проекта — [Alkaa 3.4.2](https://github.com/igorescodro/alkaa/tree/8524e062027ca8f9766cc8bf434a13ffdcbfdb97)

| Набор | Сценарии | Прошло |
| --- | --- | ---: |
| Unit | Сохранение двух задач, создание напоминания, сохранение без напоминания, пустой заголовок, изменение описания, изменение отсутствующей задачи | 6/6 |
| Compose и Espresso | Создание в списке Tasks, открытие и закрытие карточки, поиск существующей задачи, поиск без результатов | 4/4 |
| UI Automator | Создание и открытие задачи из списка и из поиска, поиск без результатов | 2/2 |
| Ktor Client | GET 200/404, POST 201/400 и PATCH 200/404 в DummyJSON Todos | 6/6 |

Alkaa хранит задачи локально, поэтому API проверяется отдельно на DummyJSON

## Результаты

Все 18 тестов прошли, UI-тесты запущены на Pixel 9 Pro XL с Android 15

Покрытие модуля `domain` по 75 JVM-тестам — 83,9% строк и 74,2% ветвей

## Запуск

Нужны JDK 17, Android SDK 36 с Build Tools 36.0.0, эмулятор на английском и доступ к DummyJSON

```sh
./gradlew --no-configuration-cache :domain:domainCoverage :api-tests:test :app:connectedDebugAndroidTest
python3 qa/report.py
```

Для команды генерации Allure нужен Node.js

- [Allure](allure/index.html)
- [Покрытие JaCoCo](coverage/index.html)
