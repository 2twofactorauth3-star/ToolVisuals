# ToolVisuals Protocols

Компонент переключения Java-протоколов на основе ViaFabricPlus, автоматически включённый в клиентский JAR ToolVisuals.

Поддерживает Java-протоколы от 1.7.2 до нативной версии сборки: 1.21.4, 1.21.11 или 26.3. Выбор версии доступен в меню ToolVisuals.

Исходники для каждой версии находятся в `src/targets/<Minecraft>`, общие правила и подключение меню — в `src/shared`. Происхождение исходников и зависимости закреплены в манифестах компонента.

Сначала выполните из корня проекта `python build-tools/dependencies.py` с JDK 25 в `JAVA_HOME` или `PATH`. Затем доступны команды сборки `./gradlew :viafabricplusFork:fork263`, `:viafabricplusFork:fork12111` и `:viafabricplusFork:fork1214`. Общая команда `./gradlew build` собирает клиентские JAR с этим компонентом. Результаты находятся в `build/components/viafabricplus`.

Условия GPL ViaFabricPlus и ViaLoader и уведомления авторов находятся в `COPYING`, `NOTICE` и `ATTRIBUTION`. Лицензии вложенных библиотек сохраняются в их архивах.
