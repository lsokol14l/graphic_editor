# Image Processor (Spring Boot + Vue + Tailwind)

Веб-приложение для обработки изображений с бэкендом на Java и фронтендом на Vue.

## Что умеет приложение

1. Загружать одно или два изображения.
2. Выполнять операции:
   1. `SUM`
   2. `MULTIPLY`
   3. `AVERAGE`
   4. `MINIMUM`
   5. `MAXIMUM`
   6. `MASK`
3. Выбирать цветовые каналы для обработки:
   1. `RGB`
   2. `R`
   3. `G`
   4. `B`
   5. `RG`
   6. `GB`
   7. `RB`
4. Накладывать маску (`CIRCLE`, `SQUARE`, `RECTANGLE`) по центру изображения.
5. Сохранять результат в формате PNG.

## Структура проекта

1. Бэкенд (Spring Boot): [src/main/java/by/michael](src/main/java/by/michael)
2. Фронтенд (Vue + Vite): [frontend](frontend)
3. Статика, которую отдает Spring: [src/main/resources/static](src/main/resources/static)

Ключевые файлы:

1. Точка входа Spring: [src/main/java/by/michael/Main.java](src/main/java/by/michael/Main.java)
2. REST API: [src/main/java/by/michael/ImageController.java](src/main/java/by/michael/ImageController.java)
3. Алгоритм обработки: [src/main/java/by/michael/ImageProcessor.java](src/main/java/by/michael/ImageProcessor.java)
4. Генерация масок: [src/main/java/by/michael/MaskFactory.java](src/main/java/by/michael/MaskFactory.java)
5. Сборка и автосборка фронта: [build.gradle.kts](build.gradle.kts)

## Требования

1. JDK 21
2. Node.js + npm
3. Gradle Wrapper (уже есть в проекте)

Проверка:

```powershell
java -version
node -v
npm -v
```

Если в PowerShell ошибка на `npm` из-за policy, используйте:

```powershell
npm.cmd -v
```

## Как запускать

### Вариант 1. Одна команда через Gradle (рекомендуется)

Из корня проекта:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

Что делает сборка:

1. Устанавливает зависимости фронта (`npm install`)
2. Собирает фронт (`npm run build`)
3. Очищает [src/main/resources/static](src/main/resources/static)
4. Копирует `frontend/dist` в [src/main/resources/static](src/main/resources/static)
5. Собирает и запускает Spring Boot

После запуска откройте:

1. `http://localhost:8080`

### Вариант 2. Раздельный dev-режим

Терминал 1 (бэкенд):

```powershell
.\gradlew.bat bootRun
```

Терминал 2 (фронтенд):

```powershell
cd frontend
npm install
npm run dev
```

Адреса:

1. Бэкенд API: `http://localhost:8080`
2. Фронтенд dev: `http://localhost:5173`

## API

Эндпоинт:

1. `POST /api/images/process`
2. `Content-Type: multipart/form-data`

Параметры формы:

1. `image1` (обязательно)
2. `image2` (обязательно для всех операций, кроме `MASK`)
3. `operation` (`SUM | MULTIPLY | AVERAGE | MINIMUM | MAXIMUM | MASK`)
4. `channels` (`RGB | R | G | B | RG | GB | RB`)
5. `maskShape` (`CIRCLE | SQUARE | RECTANGLE`, нужен для `MASK`)

Ответ:

1. PNG-изображение (`image/png`)

## Алгоритм обработки изображений

### 1. Подготовка входных изображений

1. Проверяется, что первое изображение задано.
2. Для бинарных операций (`SUM`, `MULTIPLY`, `AVERAGE`, `MINIMUM`, `MAXIMUM`) требуется второе изображение.
3. Базовым выбирается изображение с большей площадью.
4. Меньшее изображение масштабируется на весь размер базового через отображение координат:

$$
x_s = round\left(x \cdot \frac{W_s - 1}{W_t - 1}\right),\quad
y_s = round\left(y \cdot \frac{H_s - 1}{H_t - 1}\right)
$$

где:

1. $W_t, H_t$ — ширина и высота целевого (большего) изображения
2. $W_s, H_s$ — ширина и высота источника (меньшего) изображения

### 2. Работа с каналами

Пиксель раскладывается на каналы через битовые операции:

$$
R = (rgb >> 16) \& 255,\quad G = (rgb >> 8) \& 255,\quad B = rgb \& 255
$$

Операция применяется только к выбранным каналам. Невыбранные каналы берутся из базового изображения без изменений.

### 3. Формулы операций

Для каждого выбранного канала:

1. `SUM`: $out = clamp(base + inf)$
2. `MULTIPLY`: $out = \frac{base \cdot inf}{255}$
3. `AVERAGE`: $out = \frac{base + inf}{2}$
4. `MINIMUM`: $out = min(base, inf)$
5. `MAXIMUM`: $out = max(base, inf)$

`clamp` ограничивает значение диапазоном $[0, 255]$.

### 4. Маска

Для операции `MASK`:

1. Маска создается по размеру первого изображения.
2. Центр маски всегда в центре изображения.
3. Вне маски выбранные каналы зануляются, невыбранные остаются исходными.

Формы маски:

1. `CIRCLE`
2. `SQUARE`
3. `RECTANGLE`

## Частые проблемы

1. Ошибка `Unsupported class file major version 69`
   1. Проверьте, что используется JDK 21.
   2. Выполните `clean` перед сборкой.
2. `npm` не запускается в PowerShell из-за ExecutionPolicy
   1. Используйте `npm.cmd`.
3. Не найден Node/npm в сборке Gradle
   1. Добавьте Node.js в `PATH`.

## Команды для быстрой проверки

```powershell
.\gradlew.bat tasks --group frontend
.\gradlew.bat clean build
.\gradlew.bat bootRun
```
