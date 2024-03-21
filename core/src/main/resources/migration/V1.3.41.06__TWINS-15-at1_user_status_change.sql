ALTER TABLE "user" DROP CONSTRAINT IF EXISTS user_user_status_id_fk;

DROP TABLE if exists user_status;

CREATE TABLE if not exists user_status (
    id varchar(50)
        constraint user_status_pk
            primary key
);

INSERT INTO user_status (id) VALUES ('ACTIVE');
INSERT INTO user_status (id) VALUES ('DELETED');
INSERT INTO user_status (id) VALUES ('BLOCKED');

alter table "user"
    add constraint user_user_status_id_fk
        foreign key (user_status_id) references user_status (id);