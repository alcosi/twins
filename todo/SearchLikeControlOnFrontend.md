# TODO

It was resolved to control like '%' wrapping on frontend side. 
So we should update frontend for old controllers. After updating frontend we should change deprecated `org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeContainsIn` to `org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn` 

Still have to update:

| Method | Uri                                    |
|--------|----------------------------------------|
| `POST` | `/private/twinflow_schema/search/v1`   |
| `POST` | `/private/permission/search/v1`        |
| `POST` | `/private/permission_schema/search/v1` |
| `POST` | `/private/twin_status/search/v1`       |
| `POST` | `/private/permission_group/search/v1`       |

Already controlled by frontend

| Method | Uri                                              |
|--------|--------------------------------------------------|
| `POST` | `/private/factory_pipeline/search/v1`            |
| `POST` | `/private/link/search/v1`                        |
| `POST` | `/private/factory_branch/search/v1`              |
| `POST` | `/private/domain/user/search/v1`                 |
| `POST` | `/private/factory_multiplier/search/v1`          |
| `POST` | `/private/factory_condition_set/search/v1`       |
| `POST` | `/private/factory_eraser/search/v1`              |
| `POST` | `/private/factory_multiplier_filter/search/v1`   |
| `POST` | `/private/data_list_option/search/v1`            |
| `POST` | `/public/data_list_option/search/v1`             |
| `POST` | `/private/factory_pipeline_step/search/v1`       |
| `POST` | `/private/data_list/search/v1`                   |
| `POST` | `/public/data_list/search/v1`                    |
| `POST` | `/private/user_group/search/v1`                  |
| `POST` | `/private/featurer/v1`                           |
| `POST` | `/private/twin_class_fields/search/v1`           |
| `POST` | `/private/factory/search/v1`                     |
| `POST` | `/private/permission_grant/twin_role/search/v1`  |
| `POST` | `/private/twin/search/{searchId}/v1`             |
| `POST` | `/private/twin/search_by_alias/{searchAlias}/v1` |
| `POST` | `/private/twin/search/v3`                        |
| `POST` | `/private/twin/search/v2`                        |
| `POST` | `/private/twin/search/v1`                        |
| `POST` | `/private/twin_class/list/v1`                    |
| `POST` | `/private/twin_class/search/v1`                  |


