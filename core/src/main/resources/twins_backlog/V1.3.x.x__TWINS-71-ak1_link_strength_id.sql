CREATE TABLE IF NOT EXISTS link_strength
(
    id          VARCHAR(255) CONSTRAINT link_strength_pk PRIMARY KEY,
    description VARCHAR(255)
);
INSERT INTO link_strength (id, description)
VALUES ('MANDATORY', 'Twin of src_twin_class_id can`t be created without such link. Cascade deletion will occur.'),
       ('OPTIONAL', 'Twin of src_twin_class_id can be created without such link. If link exists cascade deletion will not occur. '),
       ('OPTIONAL_BUT_DELETE_CASCADE', 'Link is optional, but if exist cascade deletion will occur')
ON CONFLICT (id) DO NOTHING;

DROP VIEW IF EXISTS public.link_lazy;

ALTER TABLE public.link DROP COLUMN IF EXISTS mandatory;

ALTER TABLE public.link ADD COLUMN IF NOT EXISTS link_strength_id VARCHAR(255) DEFAULT 'OPTIONAL';

ALTER TABLE public.link DROP CONSTRAINT IF EXISTS link_strength_id_fk;

ALTER TABLE public.link ADD CONSTRAINT link_strength_id_fk FOREIGN KEY (link_strength_id)
    REFERENCES public.link_strength (id) ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE VIEW public.link_lazy AS
SELECT l.id, l.domain_id, l.src_twin_class_id, l.dst_twin_class_id, l.forward_name_i18n_id,
       l.backward_name_i18n_id, l.link_type_id, l.created_by_user_id, l.created_at, l.link_strength_id,
       tc.key AS fk_src_twin_class_key,
       tc2.key AS fk_dst_twin_class_key
FROM ((public.link l
    LEFT JOIN public.twin_class tc ON ((l.src_twin_class_id = tc.id)))
    LEFT JOIN public.twin_class tc2 ON ((l.dst_twin_class_id = tc2.id)));
