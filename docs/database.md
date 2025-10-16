# Структура базы данных Twins

## business_account

**Назначение:** хранит данные бизнес-аккаунтов, включая владельца и время создания.

### Поля таблицы

| Поле                | Тип (с constraints)                 | Описание                                  | FK            |
|---------------------|-------------------------------------|-------------------------------------------|---------------|
| id                  | uuid not null primary key           | Идентификатор бизнес-аккаунта             | Нет           |
| name                | varchar                             | Название аккаунта                         | Нет           |
| owner_user_group_id | uuid                                | Ссылка на группу пользователей-владельцев | user_group.id |
| created_at          | timestamp default CURRENT_TIMESTAMP | Дата и время создания                     | Нет           |

**Уникальные индексы:** отсутствуют

---

## business_account_user

**Назначение:** связывает пользователей с бизнес-аккаунтами.

### Поля таблицы

| Поле                | Тип (с constraints)                 | Описание                   | FK                  |
|---------------------|-------------------------------------|----------------------------|---------------------|
| id                  | uuid not null primary key           | Идентификатор записи связи | Нет                 |
| business_account_id | uuid                                | Ссылка на бизнес-аккаунт   | business_account.id |
| user_id             | uuid                                | Ссылка на пользователя     | user.id             |
| created_at          | timestamp default CURRENT_TIMESTAMP | Время создания связи       | Нет                 |

**Уникальные индексы:**

* `business_account_user_business_account_id_user_id_uindex` (business_account_id, user_id)

---

## cud

**Назначение:** справочник для операций Create, Update, Delete.

### Поля таблицы

| Поле | Тип (с constraints)              | Описание                            | FK  |
|------|----------------------------------|-------------------------------------|-----|
| id   | varchar(10) not null primary key | Идентификатор типа операции (C/U/D) | Нет |

**Уникальные индексы:** отсутствуют

---

## data_list

**Назначение:** хранит информацию о справочниках

### Поля таблицы

| Поле                     | Тип (с constraints)                 | Описание                                             | FK        |
|--------------------------|-------------------------------------|------------------------------------------------------|-----------|
| id                       | uuid not null primary key           | Идентификатор списка данных                          | Нет       |
| name_i18n_id             | uuid not null                       | Локализованное имя                                   | i18n.id   |
| description_i18n_id      | uuid not null                       | Локализованное описание                              | i18n.id   |
| updated_at               | timestamp default CURRENT_TIMESTAMP | Время обновления                                     | Нет       |
| domain_id                | uuid not null                       | Ссылка на домен, в рамках которого существует список | domain.id |
| key                      | varchar                             | Уникальный ключ в пределах домена                    | Нет       |
| attribute_1_key          | varchar                             | Ключ дополнительного атрибута 1                      | Нет       |
| attribute_2_key          | varchar                             | Ключ дополнительного атрибута 2                      | Нет       |
| attribute_3_key          | varchar                             | Ключ дополнительного атрибута 3                      | Нет       |
| attribute_4_key          | varchar                             | Ключ дополнительного атрибута 4                      | Нет       |
| attribute_1_name_i18n_id | uuid                                | Имя атрибута 1 (локализованное)                      | i18n.id   |
| attribute_2_name_i18n_id | uuid                                | Имя атрибута 2 (локализованное)                      | i18n.id   |
| attribute_3_name_i18n_id | uuid                                | Имя атрибута 3 (локализованное)                      | i18n.id   |
| attribute_4_name_i18n_id | uuid                                | Имя атрибута 4 (локализованное)                      | i18n.id   |
| created_at               | timestamp                           | Время создания                                       | Нет       |
| external_id              | varchar(255)                        | Внешний идентификатор списка (если есть)             | Нет       |

**Уникальные индексы:**

* `data_list_domain_id_key_uindex` (domain_id, key)

---

## data_list_option

**Назначение:** элементы списка данных (опции).

### Поля таблицы

| Поле                       | Тип (с constraints)       | Описание                      | FK                         |
|----------------------------|---------------------------|-------------------------------|----------------------------|
| id                         | uuid not null primary key | Идентификатор опции           | Нет                        |
| data_list_id               | uuid not null             | Ссылка на родительский список | data_list.id               |
| option                     | varchar                   | Текст опции (deprecated)      | Нет                        |
| option_i18n_id             | uuid                      | Локализованная версия текста  | i18n.id                    |
| icon                       | varchar                   | Иконка (путь/код)             | Нет                        |
| attribute_1_value          | varchar                   | Значение атрибута 1           | Нет                        |
| attribute_2_value          | varchar                   | Значение атрибута 2           | Нет                        |
| attribute_3_value          | varchar                   | Значение атрибута 3           | Нет                        |
| attribute_4_value          | varchar                   | Значение атрибута 4           | Нет                        |
| data_list_option_status_id | varchar not null          | Статус опции                  | data_list_option_status.id |
| business_account_id        | uuid                      | Бизнес-аккаунт                | business_account.id        |
| order                      | smallint                  | Порядок сортировки            | Нет                        |
| external_id                | varchar(255)              | Внешний идентификатор         | Нет                        |
| background_color           | varchar(10)               | Цвет фона                     | Нет                        |
| font_color                 | varchar(10)               | Цвет шрифта                   | Нет                        |

**Уникальные индексы:** отсутствуют

---

## data_list_option_status

**Назначение:** справочник статусов элементов списка данных.

### Поля таблицы

| Поле | Тип (с constraints)          | Описание              | FK  |
|------|------------------------------|-----------------------|-----|
| id   | varchar not null primary key | Идентификатор статуса | Нет |

**Уникальные индексы:** отсутствуют

---

## data_list_subset

**Назначение:** подмножества внутри списка данных. Позволяет выделить часть большого списка в отдельный подсписок

### Поля таблицы

| Поле         | Тип (с constraints)       | Описание                | FK           |
|--------------|---------------------------|-------------------------|--------------|
| id           | uuid not null primary key | Идентификатор подсписка | Нет          |
| data_list_id | uuid not null             | Родительский список     | data_list.id |
| name         | varchar(100) not null     | Название подсписка      | Нет          |
| description  | varchar(255)              | Описание                | Нет          |
| key          | varchar(100) not null     | Ключ подсписка          | Нет          |

**Уникальные индексы:** отсутствуют

---

## data_list_subset_option

**Назначение:** связь между подсписками и опциями.

### Поля таблицы

| Поле                | Тип (с constraints) | Описание            | FK                  |
|---------------------|---------------------|---------------------|---------------------|
| data_list_subset_id | uuid not null       | Ссылка на подсписок | data_list_subset.id |
| data_list_option_id | uuid not null       | Ссылка на опцию     | data_list_option.id |

**Уникальные индексы:**

* `data_list_subset_option_uq` (data_list_subset_id, data_list_option_id)

---

## domain

**Назначение:** центральная таблица — описывает домен (организационную единицу) системы Twins.

### Поля таблицы

| Поле                                   | Тип (с constraints)                 | Описание                               | FK                   |
|----------------------------------------|-------------------------------------|----------------------------------------|----------------------|
| id                                     | uuid not null primary key           | Идентификатор домена                   | Нет                  |
| key                                    | varchar(100)                        | Уникальный ключ домена                 | Нет                  |
| business_account_initiator_featurer_id | integer                             | Инициализатор бизнес-аккаунта          | featurer.id          |
| business_account_initiator_params      | hstore                              | Параметры инициализации аккаунта       | Нет                  |
| description                            | varchar(255)                        | Описание домена                        | Нет                  |
| identity_provider_id                   | uuid not null                       | Провайдер идентификации                | identity_provider.id |
| user_group_manager_featurer_id         | integer                             | Фичер для управления группами          | featurer.id          |
| user_group_manager_params              | hstore                              | Параметры менеджера групп              | Нет                  |
| permission_schema_id                   | uuid                                | Схема разрешений                       | permission_schema.id |
| twinflow_schema_id                     | uuid                                | Схема twinflow                         | twinflow_schema.id   |
| twin_class_schema_id                   | uuid                                | Схема классов                          | twin_class_schema.id |
| business_account_template_twin_id      | uuid                                | Шаблон бизнес-аккаунта                 | Нет                  |
| created_at                             | timestamp default CURRENT_TIMESTAMP | Время создания                         | Нет                  |
| default_i18n_locale_id                 | varchar                             | Язык по умолчанию                      | i18n_locale.locale   |
| ancestor_twin_class_id                 | uuid                                | Глобальный родительский twin-класс     | twin_class.id        |
| domain_type_id                         | varchar not null                    | Тип домена                             | domain_type.id       |
| alias_counter                          | integer default 0                   | Счетчик алиасов                        | Нет                  |
| default_tier_id                        | uuid                                | Тарифный план (tier) по умолчанию      | tier.id              |
| attachments_storage_used_count         | bigint default 0                    | Кол-во вложений                        | Нет                  |
| attachments_storage_used_size          | bigint default 0                    | Размер вложений                        | Нет                  |
| domain_user_template_twin_id           | uuid                                | Шаблон twin-а для профиля пользователя | Нет                  |
| icon_dark_resource_id                  | uuid                                | Иконка для темной схемы                | resource.id          |
| icon_light_resource_id                 | uuid                                | Иконка для светлой схемы               | resource.id          |
| resources_storage_id                   | uuid                                | Хранилище ресурсов                     | storage.id           |
| attachments_storage_id                 | uuid                                | Хранилище вложений                     | storage.id           |
| domain_status_id                       | varchar default 'ACTIVE'            | Статус                                 | domain_status.id     |
| navbar_face_id                         | uuid                                | Навигационная панель                   | face.id              |
| name                                   | varchar(50)                         | Название домена                        | Нет                  |
| domain_user_initiator_featurer_id      | integer not null                    | Инициализатор пользователей домена     | featurer.id          |
| domain_user_initiator_params           | hstore                              | Параметры инициализатора пользователей | Нет                  |

**Уникальные индексы:** отсутствуют

---

## domain_business_account

**Назначение:** связывает домены и бизнес-аккаунты, включая используемые схемы и квоты.

### Поля таблицы

| Поле                           | Тип (с constraints)                 | Описание            | FK                   |
|--------------------------------|-------------------------------------|---------------------|----------------------|
| id                             | uuid not null primary key           | Идентификатор связи | Нет                  |
| domain_id                      | uuid not null                       | Домен               | domain.id            |
| business_account_id            | uuid not null                       | Бизнес-аккаунт      | business_account.id  |
| permission_schema_id           | uuid                                | Схема разрешений    | permission_schema.id |
| twinflow_schema_id             | uuid                                | Схема twinflow      | twinflow_schema.id   |
| twin_class_schema_id           | uuid                                | Схема twin-классов  | twin_class_schema.id |
| created_at                     | timestamp default CURRENT_TIMESTAMP | Время создания      | Нет                  |
| tier_id                        | uuid not null                       | Тариф (tier)        | tier.id              |
| attachments_storage_used_count | bigint default 0                    | Кол-во вложений     | Нет                  |
| attachments_storage_used_size  | bigint default 0                    | Размер вложений     | Нет                  |

**Уникальные индексы:**

* `domain_business_account_domain_id_business_account_id_uindex` (domain_id, business_account_id)

---

## domain_locale

**Назначение:** связывает домен с доступными локалями (языками интерфейса).

### Поля таблицы

| Поле           | Тип (с constraints)       | Описание            | FK                 |
|----------------|---------------------------|---------------------|--------------------|
| id             | uuid not null primary key | Идентификатор связи | Нет                |
| domain_id      | uuid not null             | Домен               | domain.id          |
| i18n_locale_id | varchar not null          | Код локали          | i18n_locale.locale |
| icon           | varchar                   | Иконка локали       | Нет                |
| active         | boolean default true      | Признак активности  | Нет                |

**Уникальные индексы:**

* `domain_id_locale_id_uk` (domain_id, i18n_locale_id)

---

## domain_status

**Назначение:** справочник статусов домена (например, ACTIVE, DISABLED и т.д.).

### Поля таблицы

| Поле        | Тип (с constraints)          | Описание              | FK  |
|-------------|------------------------------|-----------------------|-----|
| id          | varchar not null primary key | Идентификатор статуса | Нет |
| description | text                         | Описание статуса      | Нет |

**Уникальные индексы:** отсутствуют

---

## domain_type

**Назначение:** справочник типов доменов, включая их настройки и связанный функционал.

### Поля таблицы

| Поле                                   | Тип (с constraints)          | Описание                             | FK                   |
|----------------------------------------|------------------------------|--------------------------------------|----------------------|
| id                                     | varchar not null primary key | Идентификатор типа                   | Нет                  |
| name                                   | varchar(255)                 | Название типа домена                 | Нет                  |
| description                            | varchar(255)                 | Описание                             | Нет                  |
| domain_initiator_featurer_id           | integer not null             | Фичер-инициатор домена               | featurer.id          |
| domain_initiator_params                | hstore                       | Параметры инициатора                 | Нет                  |
| default_user_group_manager_featurer_id | integer                      | Фичер менеджера групп                | featurer.id          |
| default_user_group_manager_params      | hstore                       | Параметры менеджера групп            | Нет                  |
| default_identity_provider_id           | uuid not null                | Провайдер идентификации по умолчанию | identity_provider.id |

**Уникальные индексы:** отсутствуют

---

## domain_type_twin_class_owner_type

**Назначение:** связывает типы доменов с типами владельцев twin-классов. В B2B домене доступны больше типов владельцев

### Поля таблицы

| Поле                     | Тип (с constraints) | Описание                  | FK                       |
|--------------------------|---------------------|---------------------------|--------------------------|
| domain_type_id           | varchar not null    | Тип домена                | domain_type.id           |
| twin_class_owner_type_id | varchar not null    | Тип владельца twin-класса | twin_class_owner_type.id |

**Уникальные индексы:**

* Первичный ключ (domain_type_id, twin_class_owner_type_id)

---

## draft

**Назначение:** хранит черновики изменений объектов Twins (twin, link, attachment и т.д.).

### Поля таблицы

| Поле                 | Тип (с constraints)                 | Описание                  | FK                  |
|----------------------|-------------------------------------|---------------------------|---------------------|
| id                   | uuid not null primary key           | Идентификатор черновика   | Нет                 |
| domain_id            | uuid not null                       | Домен                     | domain.id           |
| business_account_id  | uuid                                | Бизнес-аккаунт            | business_account.id |
| auto_commit          | boolean default false not null      | Автоматическое применение | Нет                 |
| draft_status_id      | varchar                             | Статус черновика          | draft_status.id     |
| draft_status_details | varchar                             | Подробности статуса       | Нет                 |
| created_by_user_id   | uuid not null                       | Автор                     | user.id             |
| created_at           | timestamp default CURRENT_TIMESTAMP | Время создания            | Нет                 |

**Уникальные индексы:** отсутствуют

---

## draft_status

**Назначение:** справочник статусов черновиков (например, NEW, SAVED, COMMITTED).

### Поля таблицы

| Поле | Тип (с constraints)               | Описание              | FK  |
|------|-----------------------------------|-----------------------|-----|
| id   | varchar(255) not null primary key | Идентификатор статуса | Нет |

**Уникальные индексы:** отсутствуют

---

## draft_twin_attachment

**Назначение:** изменения вложений (attachments) в рамках черновика.

### Поля таблицы

| Поле                   | Тип (с constraints)       | Описание               | FK       |
|------------------------|---------------------------|------------------------|----------|
| id                     | uuid not null primary key | Идентификатор записи   | Нет      |
| draft_id               | uuid not null             | Черновик               | draft.id |
| time_in_millis         | bigint not null           | Время фиксации         | Нет      |
| cud_id                 | varchar not null          | Тип операции           | cud.id   |
| twin_attachment_id     | uuid                      | Вложение               | Нет      |
| twin_id                | uuid not null             | Twin                   | Нет      |
| twinflow_transition_id | uuid                      | Переход twinflow       | Нет      |
| storage_file_key       | varchar(255)              | Ключ файла в хранилище | Нет      |
| view_permission_id     | uuid                      | Разрешение на просмотр | Нет      |
| created_by_user_id     | uuid                      | Автор                  | Нет      |
| external_id            | varchar                   | Внешний идентификатор  | Нет      |
| title                  | varchar                   | Заголовок              | Нет      |
| description            | varchar                   | Описание               | Нет      |
| twin_comment_id        | uuid                      | Комментарий            | Нет      |
| twin_class_field_id    | uuid                      | Поле twin-класса       | Нет      |
| modifications          | varchar                   | Изменения              | Нет      |

**Уникальные индексы:** отсутствуют

---

## draft_twin_erase_status

**Назначение:** справочник статусов удаления twin-объектов в черновиках.

### Поля таблицы

| Поле | Тип (с constraints)               | Описание              | FK  |
|------|-----------------------------------|-----------------------|-----|
| id   | varchar(255) not null primary key | Идентификатор статуса | Нет |

**Уникальные индексы:** отсутствуют

---

## draft_twin_field_data_list

**Назначение:** изменения полей типа data_list внутри twin в рамках черновика.

### Поля таблицы

| Поле                    | Тип (с constraints)       | Описание              | FK                  |
|-------------------------|---------------------------|-----------------------|---------------------|
| id                      | uuid not null primary key | Идентификатор записи  | Нет                 |
| draft_id                | uuid not null             | Черновик              | draft.id            |
| time_in_millis          | bigint not null           | Время фиксации        | Нет                 |
| cud_id                  | varchar not null          | Тип операции          | cud.id              |
| twin_field_data_list_id | uuid                      | Поле twin (data_list) | Нет                 |
| twin_id                 | uuid not null             | Twin                  | Нет                 |
| twin_class_field_id     | uuid not null             | Поле twin-класса      | twin_class_field.id |
| data_list_option_id     | uuid                      | Значение из списка    | data_list_option.id |

**Уникальные индексы:** отсутствуют

---

## draft_twin_field_simple

**Назначение:** хранит изменения простых (текстовых/числовых) полей twin в черновиках.

### Поля таблицы

| Поле                 | Тип (с constraints)       | Описание             | FK                  |
|----------------------|---------------------------|----------------------|---------------------|
| id                   | uuid not null primary key | Идентификатор записи | Нет                 |
| draft_id             | uuid not null             | Черновик             | draft.id            |
| time_in_millis       | bigint not null           | Время фиксации       | Нет                 |
| cud_id               | varchar not null          | Тип операции         | cud.id              |
| twin_field_simple_id | uuid                      | Поле twin (простое)  | Нет                 |
| twin_id              | uuid not null             | Twin                 | Нет                 |
| twin_class_field_id  | uuid not null             | Поле twin-класса     | twin_class_field.id |
| value                | text                      | Значение             | Нет                 |

**Уникальные индексы:** отсутствуют

---

## draft_twin_field_user

**Назначение:** хранит изменения полей типа “пользователь” (user reference) в черновиках.

### Поля таблицы

| Поле                | Тип (с constraints)       | Описание             | FK                  |
|---------------------|---------------------------|----------------------|---------------------|
| id                  | uuid not null primary key | Идентификатор записи | Нет                 |
| draft_id            | uuid not null             | Черновик             | draft.id            |
| time_in_millis      | bigint not null           | Время фиксации       | Нет                 |
| cud_id              | varchar not null          | Тип операции         | cud.id              |
| twin_field_user_id  | uuid                      | Поле twin (user)     | Нет                 |
| twin_id             | uuid not null             | Twin                 | Нет                 |
| twin_class_field_id | uuid not null             | Поле twin-класса     | twin_class_field.id |
| user_id             | uuid                      | Пользователь         | user.id             |

**Уникальные индексы:** отсутствуют

---

Отлично 👌
Продолжаю документацию по структуре БД **Twins** —
следующие **10 таблиц**, строго в соответствии с SQL и требованиями (Markdown, без выдуманных описаний).

---

# Структура базы данных Twins

*(Продолжение, часть 3)*

---

## draft_twin_link

**Назначение:** хранит изменения связей (link) между twin-объектами в рамках черновика.

### Поля таблицы

| Поле               | Тип (с constraints)       | Описание                              | FK       |
|--------------------|---------------------------|---------------------------------------|----------|
| id                 | uuid not null primary key | Идентификатор записи                  | Нет      |
| draft_id           | uuid not null             | Черновик                              | draft.id |
| time_in_millis     | bigint not null           | Время фиксации                        | Нет      |
| cud_id             | varchar not null          | Тип операции (Create/Update/Delete)   | cud.id   |
| twin_link_id       | uuid                      | Идентификатор существующей связи twin | Нет      |
| src_twin_id        | uuid                      | Twin-источник                         | Нет      |
| dst_twin_id        | uuid                      | Twin-приёмник                         | Нет      |
| link_id            | uuid                      | Идентификатор типа связи              | Нет      |
| created_by_user_id | uuid                      | Пользователь, создавший связь         | Нет      |

**Уникальные индексы:** отсутствуют

---

## draft_twin_marker

**Назначение:** хранит изменения маркеров twin-объектов в черновиках.

### Поля таблицы

| Поле                       | Тип (с constraints)            | Описание                              | FK                  |
|----------------------------|--------------------------------|---------------------------------------|---------------------|
| id                         | uuid not null primary key      | Идентификатор записи                  | Нет                 |
| draft_id                   | uuid not null                  | Черновик                              | draft.id            |
| time_in_millis             | bigint not null                | Время фиксации                        | Нет                 |
| twin_id                    | uuid not null                  | Twin                                  | Нет                 |
| create_else_delete         | boolean default false not null | Флаг: создать или удалить маркер      | Нет                 |
| marker_data_list_option_id | uuid                           | Ссылка на значение из списка маркеров | data_list_option.id |

**Уникальные индексы:** отсутствуют

---

## draft_twin_persist

**Назначение:** хранит информацию о сохранении twin-объектов в процессе черновика (создание/обновление).

### Поля таблицы

| Поле                      | Тип (с constraints)       | Описание                          | FK                  |
|---------------------------|---------------------------|-----------------------------------|---------------------|
| id                        | uuid not null primary key | Идентификатор операции            | Нет                 |
| draft_id                  | uuid not null             | Черновик                          | draft.id            |
| time_in_millis            | bigint not null           | Время фиксации                    | Нет                 |
| create_else_update        | boolean not null          | Флаг: создать или обновить        | Нет                 |
| twin_id                   | uuid not null             | Идентификатор twin                | Нет                 |
| twin_class_id             | uuid                      | Twin-класс                        | twin_class.id       |
| head_twin_id              | uuid                      | Главный twin                      | Нет                 |
| external_id               | varchar(100)              | Внешний идентификатор             | Нет                 |
| twin_status_id            | uuid                      | Статус twin                       | Нет                 |
| name                      | varchar(100)              | Имя twin                          | Нет                 |
| description               | text                      | Описание                          | Нет                 |
| created_by_user_id        | uuid                      | Создатель                         | user.id             |
| assigner_user_id          | uuid                      | Назначивший пользователь          | user.id             |
| owner_business_account_id | uuid                      | Владелец — бизнес-аккаунт         | business_account.id |
| owner_user_id             | uuid                      | Владелец — пользователь           | user.id             |
| view_permission_id        | uuid                      | Разрешение на просмотр            | permission.id       |
| conflict_description      | varchar(255)              | Описание конфликта при сохранении | Нет                 |

**Уникальные индексы:** отсутствуют

---

## error

**Назначение:** справочник ошибок, содержащий локализованные сообщения для клиента.

### Поля таблицы

| Поле               | Тип (с constraints)       | Описание                               | FK      |
|--------------------|---------------------------|----------------------------------------|---------|
| id                 | uuid not null primary key | Идентификатор ошибки                   | Нет     |
| code_local         | integer                   | Внутренний код ошибки                  | Нет     |
| code_external      | varchar(255)              | Внешний код                            | Нет     |
| name               | varchar(40)               | Короткое имя ошибки                    | Нет     |
| description        | varchar(255)              | Описание ошибки                        | Нет     |
| client_msg_i18n_id | uuid not null             | Сообщение для клиента (локализованное) | i18n.id |

**Уникальные индексы:**

* `error__uniq` (code_local, code_external)

---

## face

**Назначение:** описывает интерфейсные элементы (faces) в системе Twins.

### Поля таблицы

| Поле               | Тип (с constraints)                 | Описание                  | FK                |
|--------------------|-------------------------------------|---------------------------|-------------------|
| id                 | uuid not null primary key           | Идентификатор face        | Нет               |
| domain_id          | uuid                                | Домен                     | domain.id         |
| face_component_id  | varchar(5) not null                 | Вид компонента интерфейса | face_component.id |
| name               | varchar                             | Название                  | Нет               |
| description        | varchar                             | Описание                  | Нет               |
| created_at         | timestamp default CURRENT_TIMESTAMP | Время создания            | Нет               |
| created_by_user_id | uuid                                | Автор                     | user.id           |

**Уникальные индексы:** отсутствуют

---

## face_component

**Назначение:** справочник визуальных компонентов, поддерживаемых в интерфейсе Faces.

### Поля таблицы

| Поле                   | Тип (с constraints)             | Описание                      | FK                     |
|------------------------|---------------------------------|-------------------------------|------------------------|
| id                     | varchar(5) not null primary key | Идентификатор вида компонента | Нет                    |
| face_component_type_id | varchar not null                | Вид компонента                | face_component_type.id |
| name                   | varchar not null                | Название компонента           | Нет                    |
| description            | varchar                         | Описание                      | Нет                    |

**Уникальные индексы:** отсутствуют

---

## face_component_type

**Назначение:** справочник типов компонент интерфейса (например, панель, форма, кнопка).

### Поля таблицы

| Поле        | Тип (с constraints)          | Описание           | FK  |
|-------------|------------------------------|--------------------|-----|
| id          | varchar not null primary key | Идентификатор типа | Нет |
| name        | varchar not null             | Название типа      | Нет |
| description | varchar                      | Описание           | Нет |

**Уникальные индексы:** отсутствуют

---

## face_navbar_nb001

**Назначение:** конфигурация навигационной панели (navbar) интерфейса NB001.

### Поля таблицы

| Поле                        | Тип (с constraints)       | Описание                        | FK          |
|-----------------------------|---------------------------|---------------------------------|-------------|
| face_id                     | uuid not null primary key | Ссылка на лицо                  | face.id     |
| admin_area_label_i18n_id    | uuid                      | Текст административной области  | i18n.id     |
| admin_area_icon_resource_id | uuid                      | Иконка административной области | resource.id |
| user_area_label_i18n_id     | uuid                      | Текс пользовательской области   | i18n.id     |
| user_area_icon_resource_id  | uuid                      | Иконка пользовательской области | resource.id |

**Уникальные индексы:** отсутствуют

---

## face_navbar_nb001_menu_item_status

**Назначение:** справочник статусов пунктов меню навигационной панели NB001.

### Поля таблицы

| Поле | Тип (с constraints)          | Описание                          | FK  |
|------|------------------------------|-----------------------------------|-----|
| id   | varchar not null primary key | Идентификатор статуса пункта меню | Нет |

**Уникальные индексы:** отсутствуют

---

## featurer

**Назначение:** хранит определения функциональных компонентов (фич), используемых в разных частях системы.

### Поля таблицы

| Поле             | Тип (с constraints)            | Описание                   | FK               |
|------------------|--------------------------------|----------------------------|------------------|
| id               | integer not null primary key   | Идентификатор фичера       | Нет              |
| featurer_type_id | integer not null               | Тип фичера                 | featurer_type.id |
| class            | varchar not null               | Класс реализации фичера    | Нет              |
| name             | varchar not null               | Имя фичера                 | Нет              |
| description      | varchar(255)                   | Описание                   | Нет              |
| deprecated       | boolean default false not null | Признак устаревшего фичера | Нет              |

**Уникальные индексы:** отсутствуют

---


## featurer_param

**Назначение:** параметры, доступные для конкретных фичеров.

### Поля таблицы

| Поле                   | Тип (с constraints)                | Описание                                                      | FK                                                        |
| ---------------------- | ---------------------------------- | ------------------------------------------------------------- | --------------------------------------------------------- |
| featurer_id            | integer **not null**               | Идентификатор фичера (часть составного PK).                   | `featurer.id` (references featurer)                       |
| injectable             | boolean                            | Флаг, указывающий, инъецируем ли параметр (NULL допускается). | Нет                                                       |
| `order`                | integer **not null**               | Порядок параметра (число). Поле называется `"order"`.         | Нет                                                       |
| key                    | varchar(40) **not null**           | Ключ параметра (часть составного PK).                         | Нет                                                       |
| name                   | varchar(40) **not null**           | Имя/заголовок параметра.                                      | Нет                                                       |
| description            | varchar(255)                       | Описание параметра.                                           | Нет                                                       |
| featurer_param_type_id | varchar(40)                        | Тип параметра (ссылка на справочник типов).                   | `featurer_param_type.id` (references featurer_param_type) |
| is_optional            | boolean **not null** default false | Флаг — необязательный ли параметр (имеет default false).      | Нет                                                       |
| default_value          | varchar                            | Значение по умолчанию (если задано).                          | Нет                                                       |
| example_values         | character varying[]                | Массив примерных значений (тип — array of varchar).           | Нет                                                       |

**Уникальные индексы:**

* `featurer_param_featurer_id_key_uindex` (featurer_id, key)

---

## featurer_param_type

**Назначение:** справочник типов параметров фичеров (например, string, boolean, integer).

### Поля таблицы

| Поле        | Тип (с constraints)                                                          | Описание                                                                                 | FK  |
| ----------- | ---------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- | --- |
| id          | varchar(40) **not null** — primary key (constraint `featurer_param_type_pk`) | Уникальный идентификатор типа параметра. (описание в скрипте отсутствует)                | Нет |
| regexp      | varchar(255) **not null**                                                    | Регулярное выражение для валидации значений этого типа. (описание в скрипте отсутствует) | Нет |
| example     | varchar(255) **not null**                                                    | Пример значения для данного типа. (описание в скрипте отсутствует)                       | Нет |
| description | varchar(255)                                                                 | Текстовое описание типа параметра. (описание в скрипте отсутствует)                      | Нет |

**Уникальные индексы:** отсутствуют

---

## featurer_type

**Назначение:** справочник типов фичеров (например, system, domain, twinflow и т.п.).

### Поля таблицы

| Поле        | Тип (с constraints)          | Описание             | FK  |
| ----------- | ---------------------------- | -------------------- | --- |
| id          | integer not null primary key | Идентификатор типа   | Нет |
| name        | varchar(255) not null        | Название типа фичера | Нет |
| description | varchar(255)                 | Описание             | Нет |

**Уникальные индексы:** отсутствуют

---
