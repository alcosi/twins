CREATE TABLE if not exists public.twin_alias_type (
              alias_type_id char NOT NULL,
              description character varying(255),
              CONSTRAINT twin_alias_type_pk PRIMARY KEY (alias_type_id)
);

INSERT INTO public.twin_alias_type VALUES ('D', 'Domain key is uniq, so given type of alias is also uniq even between all registered domains') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('C', 'Twin class key is uniq only in given domain. That is why such kind of alias can be duplicated between different domains.') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('B', 'This alias is differ from domain class alias because it is uniq only inside BA inside some domain.') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('S', 'Space for owner type domain') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('T', 'Space for owner type domainBusinessAccount') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('K', 'Space for owner type domainUser') on conflict(alias_type_id) do nothing;


CREATE TABLE IF NOT EXISTS public.twin_alias (
                                                 id UUID PRIMARY KEY,
                                                 alias_value VARCHAR(255) NOT NULL,
                                                 twin_alias_type_id VARCHAR(1) NOT NULL,
                                                 twin_id UUID,
                                                 user_id UUID,
                                                 domain_id UUID,
                                                 business_account_id UUID,
                                                 FOREIGN KEY (twin_alias_type_id) REFERENCES public.twin_alias_type(alias_type_id),
                                                 FOREIGN KEY (twin_id) REFERENCES public.twin(id),
                                                 FOREIGN KEY (user_id) REFERENCES public.user(id),
                                                 FOREIGN KEY (domain_id) REFERENCES public.domain(id),
                                                 FOREIGN KEY (business_account_id) REFERENCES public.business_account(id)
);
CREATE UNIQUE INDEX if not exists twin_alias_D_C_S ON twin_alias (domain_id, alias_value) WHERE twin_alias_type_id IN ('D', 'C', 'S');
CREATE UNIQUE INDEX if not exists twin_alias_B_K ON twin_alias (domain_id, business_account_id, alias_value) WHERE twin_alias_type_id IN ('B', 'K');
CREATE UNIQUE INDEX if not exists twin_alias_T ON twin_alias (domain_id, user_id, alias_value) WHERE twin_alias_type_id = 'T';
alter table public.twin_alias add if not exists created_at timestamp default current_timestamp;

DROP TABLE if exists twin_domain_alias;
DROP TABLE if exists twin_business_account_alias;

DELETE FROM twin_alias;
DELETE FROM twin_business_account_alias_counter;

UPDATE twin_business_account_alias_counter SET alias_counter = 0;
UPDATE domain SET alias_counter = 0;
UPDATE twin_class SET domain_alias_counter = 0;
UPDATE space SET domain_alias_counter = 0, business_account_alias_counter = 0;

DO $$
    DECLARE
        twin_record RECORD;
        alias_key VARCHAR;
        domain_counter INT;
        ba_counter INT;
        space_counter INT;
        new_alias VARCHAR;
    BEGIN
        -- Генерация псевдонимов для DOMAIN
        FOR twin_record IN
            SELECT twin.id, twin.twin_class_id, tc.domain_id, twin.owner_business_account_id, tc.twin_class_owner_type_id
            FROM twin
                     JOIN twin_class tc ON twin.twin_class_id = tc.id
            WHERE tc.twin_class_owner_type_id = 'domain'
            LOOP
                -- Генерация псевдонима для Domain
                domain_counter := (SELECT alias_counter FROM domain WHERE id = twin_record.domain_id);
                domain_counter := domain_counter + 1;
                UPDATE domain SET alias_counter = domain_counter WHERE id = twin_record.domain_id;
                alias_key := (SELECT key FROM domain WHERE id = twin_record.domain_id);
                new_alias := alias_key || '-D' || domain_counter;
                INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, alias_value, twin_id, created_at)
                VALUES (gen_random_uuid(), 'D', twin_record.domain_id, new_alias, twin_record.id, now());

                -- Генерация псевдонима для Domain Class
                domain_counter := (SELECT domain_alias_counter FROM twin_class WHERE id = twin_record.twin_class_id);
                domain_counter := domain_counter + 1;
                UPDATE twin_class SET domain_alias_counter = domain_counter WHERE id = twin_record.twin_class_id;
                alias_key := (SELECT key FROM twin_class WHERE id = twin_record.twin_class_id);
                new_alias := alias_key || '-C' || domain_counter;
                INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, alias_value, twin_id, created_at)
                VALUES (gen_random_uuid(), 'C', twin_record.domain_id, new_alias, twin_record.id, now());

                -- Генерация псевдонима для Space
                space_counter := (SELECT domain_alias_counter FROM space WHERE twin_id = twin_record.id);
                IF space_counter IS NOT NULL THEN
                    space_counter := space_counter + 1;
                    UPDATE space SET domain_alias_counter = space_counter WHERE twin_id = twin_record.id;
                    alias_key := (SELECT key FROM space WHERE twin_id = twin_record.id);
                    new_alias := alias_key || '-S' || space_counter;
                    INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, alias_value, twin_id, created_at)
                    VALUES (gen_random_uuid(), 'S', twin_record.domain_id, new_alias, twin_record.id, now());
                END IF;
            END LOOP;

        -- Генерация псевдонимов для DOMAIN_BUSINESS_ACCOUNT
        FOR twin_record IN
            SELECT twin.id, twin.twin_class_id, tc.domain_id, twin.owner_business_account_id, tc.twin_class_owner_type_id
            FROM twin
                     JOIN twin_class tc ON twin.twin_class_id = tc.id
            WHERE tc.twin_class_owner_type_id = 'domainBusinessAccount'
            LOOP
                -- Генерация псевдонима для Domain
                domain_counter := (SELECT alias_counter FROM domain WHERE id = twin_record.domain_id);
                domain_counter := domain_counter + 1;
                UPDATE domain SET alias_counter = domain_counter WHERE id = twin_record.domain_id;
                alias_key := (SELECT key FROM domain WHERE id = twin_record.domain_id);
                new_alias := alias_key || '-D' || domain_counter;
                INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, alias_value, twin_id, created_at)
                VALUES (gen_random_uuid(), 'D', twin_record.domain_id, new_alias, twin_record.id, now());

                -- Генерация псевдонима для Business Account Class
                ba_counter := (SELECT alias_counter FROM twin_business_account_alias_counter WHERE business_account_id = twin_record.owner_business_account_id AND twin_class_id = twin_record.twin_class_id);
                if twin_record.owner_business_account_id is not null then
                IF ba_counter IS NULL THEN
                    INSERT INTO twin_business_account_alias_counter(id, business_account_id, twin_class_id, alias_counter)
                    VALUES (gen_random_uuid(), twin_record.owner_business_account_id, twin_record.twin_class_id, 1);
                    ba_counter := 1;
                ELSE
                    ba_counter := ba_counter + 1;
                    UPDATE twin_business_account_alias_counter SET alias_counter = ba_counter WHERE business_account_id = twin_record.owner_business_account_id AND twin_class_id = twin_record.twin_class_id;
                END IF;
                alias_key := (SELECT key FROM twin_class WHERE id = twin_record.twin_class_id);
                new_alias := alias_key || '-B' || ba_counter;
                INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, business_account_id, alias_value, twin_id, created_at)
                VALUES (gen_random_uuid(), 'B', twin_record.domain_id, twin_record.owner_business_account_id, new_alias, twin_record.id, now());
                END IF;

                -- Генерация псевдонима для Space
                space_counter := (SELECT business_account_alias_counter FROM space WHERE twin_id = twin_record.id);
                IF space_counter IS NOT NULL THEN
                    space_counter := space_counter + 1;
                    UPDATE space SET business_account_alias_counter = space_counter WHERE twin_id = twin_record.id;
                    alias_key := (SELECT key FROM space WHERE twin_id = twin_record.id);
                    new_alias := alias_key || '-K' || space_counter;
                    INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, business_account_id, alias_value, twin_id, created_at)
                    VALUES (gen_random_uuid(), 'K', twin_record.domain_id, twin_record.owner_business_account_id, new_alias, twin_record.id, now());
                END IF;
            END LOOP;

        -- Генерация псевдонимов для DOMAIN_USER
        FOR twin_record IN
            SELECT twin.id, twin.twin_class_id, tc.domain_id, twin.owner_business_account_id, tc.twin_class_owner_type_id
            FROM twin
                     JOIN twin_class tc ON twin.twin_class_id = tc.id
            WHERE tc.twin_class_owner_type_id = 'domainUser'
            LOOP
                -- Генерация псевдонима для Domain
                domain_counter := (SELECT alias_counter FROM domain WHERE id = twin_record.domain_id);
                domain_counter := domain_counter + 1;
                UPDATE domain SET alias_counter = domain_counter WHERE id = twin_record.domain_id;
                alias_key := (SELECT key FROM domain WHERE id = twin_record.domain_id);
                new_alias := alias_key || '-D' || domain_counter;
                INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, alias_value, twin_id, created_at)
                VALUES (gen_random_uuid(), 'D', twin_record.domain_id, new_alias, twin_record.id, now());

                -- Генерация псевдонима для User Class
                space_counter := (SELECT domain_alias_counter FROM space WHERE twin_id = twin_record.id);
                IF space_counter IS NOT NULL THEN
                    space_counter := space_counter + 1;
                    UPDATE space SET domain_alias_counter = space_counter WHERE twin_id = twin_record.id;
                    alias_key := (SELECT key FROM space WHERE twin_id = twin_record.id);
                    new_alias := alias_key || '-T' || space_counter;
                    INSERT INTO twin_alias(id, twin_alias_type_id, domain_id, alias_value, twin_id, created_at)
                    VALUES (gen_random_uuid(), 'T', twin_record.domain_id, new_alias, twin_record.id, now());
                END IF;
            END LOOP;
    END $$;
