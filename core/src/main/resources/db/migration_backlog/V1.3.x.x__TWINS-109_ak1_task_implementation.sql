CREATE TABLE public.twin_alias_type (
              alias_type_id char NOT NULL,
              description character varying(255),
              CONSTRAINT twin_alias_type_pk PRIMARY KEY (alias_type_id)
);

INSERT INTO public.twin_alias_type VALUES ('D', '');
INSERT INTO public.twin_alias_type VALUES ('C', '');
INSERT INTO public.twin_alias_type VALUES ('B', '');
INSERT INTO public.twin_alias_type VALUES ('S', '');
INSERT INTO public.twin_alias_type VALUES ('T', '');
INSERT INTO public.twin_alias_type VALUES ('K', '');


CREATE TABLE twin_alias (
                            id UUID PRIMARY KEY,
                            twin_alias_type_id CHAR(1),
                            domain_id UUID,
                            business_account_id UUID,
                            user_id UUID,
                            alias VARCHAR(255) NOT NULL,
                            twin_id UUID,
                            created_at TIMESTAMP,
                            FOREIGN KEY (twin_alias_type_id) REFERENCES twin_alias_type(alias_type_id),
                            FOREIGN KEY (domain_id) REFERENCES domain(id),
                            FOREIGN KEY (business_account_id) REFERENCES business_account(id),
                            FOREIGN KEY (user_id) REFERENCES user(id),
                            FOREIGN KEY (twin_id) REFERENCES twin(id)
);

CREATE UNIQUE INDEX twin_alias_D_C_S ON twin_alias (domain_id, alias) WHERE twin_alias_type_id IN ('D', 'C', 'S');
CREATE UNIQUE INDEX twin_alias_B_K ON twin_alias (domain_id, business_account_id, alias) WHERE twin_alias_type_id IN ('B', 'K');
CREATE UNIQUE INDEX twin_alias_T ON twin_alias (domain_id, user_id, alias) WHERE twin_alias_type_id = 'T';
