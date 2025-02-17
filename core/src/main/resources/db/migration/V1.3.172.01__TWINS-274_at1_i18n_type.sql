-- missing migration
INSERT INTO i18n_type (id, name) VALUES ('twinFactoryName', 'Twin Factory Name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO i18n_type (id, name) VALUES ('twinFactoryDescription', 'Twin Factory Description') on conflict on constraint i18n_type_pk do nothing ;
