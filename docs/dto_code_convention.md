# DTO Code Convention

## 1. Общие правила

Все DTO-классы должны следовать единообразному наименованию и структуре.

- Все DTO располагаются в пакетах `org/twins/core/dto/**`
- DTO **не должны** содержать бизнес-логику
- DTO **не должны** зависеть от Entity-классов
- DTO используются исключительно для передачи данных между слоями и/или внешними интерфейсами

### 1.1 Суффиксы DTO

Используются следующие обязательные постфиксы:

- `RqDTO` — DTO для **запросов**
- `RsDTO` — DTO для **ответов**

Примеры:
- `ResourceCreateRqDTO`
- `ResourceSearchRsDTO`

---

## 2. Иерархия наследования DTO

DTO-иерархия строится от абстрактной бизнес-сущности (например, `Resource`).

### 2.1 Базовые DTO

| DTO | Назначение |
|---|---|
| `ResourceDTO` | Представление ресурса |
| `ResourceSaveDTO` | Абстрактный класс для создания и редактирования |
| `ResourceCreateDTO` | Создание нового ресурса |
| `ResourceUpdateDTO` | Обновление существующего ресурса |
| `ResourceSearchDTO` | Параметры поиска |


### 2.2 DTO для создания и обновления

Все DTO, используемые для добавления и обновления данных, **обязаны наследоваться** от `ResourceSaveDTO`.

`ResourceSaveDTO` содержит поля, общие для операций создания и редактирования.

#### Создание

```java
@Schema(name = "ResourceCreate")
public class ResourceCreateDTO extends ResourceSaveDTO {
}
```

#### Обновление

```java
@Schema(name = "ResourceUpdate")
public class ResourceUpdateDTO extends ResourceSaveDTO {

    @Schema
    private UUID id;
}
```

> ⚠️ Идентификатор (`id`) **обязан** присутствовать только в DTO для обновления.

---

## 3. DTO запросов (Request DTO)

### 3.1 Общие правила

- Все DTO запросов **обязаны наследоваться** от базового класса `Request`
- DTO запросов используют постфикс `RqDTO`

### 3.2 Примеры

#### Создание ресурсов

```java
public class ResourceCreateRqDTO extends Request {
    public List<ResourceCreateDTO> resources;
}
```

#### Обновление ресурсов

```java
public class ResourceUpdateRqDTO extends Request {
    public List<ResourceUpdateDTO> resources;
}
```

#### Поиск ресурсов

```java
public class ResourceSearchRqDTO extends Request {
    public ResourceSearchDTO search;
}
```

---

## 4. DTO ответов (Response DTO)

### 4.1 Общие правила

- Все DTO ответов используют постфикс `RsDTO`
- Все DTO ответов **обязаны наследоваться** от:
  - `Response`, либо
  - `ResponseRelatedObjectsDTOv1`


### 4.2 Ответ на создание и обновление

Общее DTO для ответа на операции добавления и редактирования:

```java
public class ResourceListRsDTO extends Response {
    public List<ResourceDTO> resources;
}
```

---

### 4.3 Ответ на поиск

DTO ответа на поиск **наследуется** от `ResourceListRsDTO` и дополнительно содержит информацию о пагинации.

```java
public class ResourceSearchRsDTO extends ResourceListRsDTO {
    public PaginationDTOv1 pagination;
}
```

---

## 5. Дополнительные соглашения

- DTO должны быть максимально плоскими
- Вложенные DTO допускаются только при наличии явной бизнес-необходимости
- Коллекции в DTO всегда инициализируются на уровне сервиса
- Nullable-поля должны быть явно задокументированы через `@Schema`

