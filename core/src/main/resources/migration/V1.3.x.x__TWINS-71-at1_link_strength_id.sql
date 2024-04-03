CREATE TABLE IF NOT EXISTS link_strength
(
    id VARCHAR(255) CONSTRAINT link_strength_pk PRIMARY KEY,
    description VARCHAR(255)
);
INSERT INTO link_strength (id, description)
VALUES ('MANDATORY', 'twin of src_twin_class_id cant be created without such link'),
       ('OPTIONAL', 'twin of src_twin_class_id can be created without such link'),
       ('OPTIONAL_BUT_DELETE_CASCADE', '')
ON CONFLICT (id) DO NOTHING;

DO $$
    BEGIN
        IF EXISTS (SELECT FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'link' AND column_name = 'mandatory') THEN
            ALTER TABLE public.link RENAME COLUMN mandatory TO link_strength_id;
        END IF;
        IF EXISTS (SELECT FROM information_schema.columns WHERE table_schema = 'public'AND table_name = 'link' AND column_name = 'link_strength_id') THEN
            ALTER TABLE public.link ALTER COLUMN link_strength_id TYPE varchar(255) USING link_strength_id::varchar(255);
        END IF;
        IF EXISTS (SELECT FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'link' AND column_name = 'link_strength_id') THEN
            ALTER TABLE public.link ALTER COLUMN link_strength_id SET DEFAULT 'OPTIONAL';
        END IF;
        IF EXISTS (SELECT FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'link' AND column_name = 'link_strength_id') AND
           EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'link_strength') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.constraint_column_usage WHERE table_name = 'link' AND column_name = 'link_strength_id' AND constraint_name = 'link_link_strength_id_fkey') THEN
                ALTER TABLE public.link
                    ADD CONSTRAINT link_strength_id_fk FOREIGN KEY (link_strength_id)
                        REFERENCES public.link_strength (id)
                        ON UPDATE CASCADE ON DELETE RESTRICT;
            END IF;
        END IF;
    END
$$;
