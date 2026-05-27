alter table featurer_param
    alter column key type varchar(100) using key::varchar(100);

alter table featurer_param
    alter column name type varchar(100) using name::varchar(100);