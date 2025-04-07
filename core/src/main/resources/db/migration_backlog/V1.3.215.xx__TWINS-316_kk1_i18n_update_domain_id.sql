-- twin_class
UPDATE i18n
SET domain_id = tc.domain_id
FROM twin_class tc
WHERE i18n.id IN (tc.name_i18n_id, tc.description_i18n_id);

-- twin_status
UPDATE i18n
SET domain_id = subquery.domain_id
FROM (
         SELECT ts.name_i18n_id AS i18n_id, tc.domain_id
         FROM twin_status ts
                  JOIN twin_class tc ON tc.id = ts.twins_class_id

         UNION

         SELECT ts.description_i18n_id AS i18n_id, tc.domain_id
         FROM twin_status ts
                  JOIN twin_class tc ON tc.id = ts.twins_class_id
     ) AS subquery
WHERE i18n.id = subquery.i18n_id;

-- twin_class_field
UPDATE i18n
SET domain_id = subquery.domain_id
FROM (
         SELECT ts.name_i18n_id AS i18n_id, tc.domain_id
         FROM twin_class_field ts
                  JOIN twin_class tc ON tc.id = ts.twin_class_id

         UNION

         SELECT ts.description_i18n_id AS i18n_id, tc.domain_id
         FROM twin_class_field ts
                  JOIN twin_class tc ON tc.id = ts.twin_class_id
     ) AS subquery
WHERE i18n.id = subquery.i18n_id;

-- card
UPDATE i18n
SET domain_id = tc.domain_id
FROM card c
         JOIN card_access ca ON ca.card_id = c.id
         JOIN twin_class tc ON tc.id = ca.twin_class_id
WHERE i18n.id = c.name_i18n_id;

-- data_list
UPDATE i18n
SET domain_id = tc.domain_id
FROM data_list tc
WHERE i18n.id IN (tc.name_i18n_id, tc.description_i18n_id);

-- data_list_option
UPDATE i18n
SET domain_id = dl.domain_id
FROM data_list_option dlo
         JOIN data_list dl ON dl.id = dlo.data_list_id
WHERE i18n.id = dlo.option_i18n_id;

-- link
UPDATE i18n
SET domain_id = subq.domain_id
FROM (
         SELECT forward_name_i18n_id AS i18n_id, domain_id
         FROM link
         WHERE forward_name_i18n_id IS NOT NULL

         UNION

         SELECT backward_name_i18n_id AS i18n_id, domain_id
         FROM link
         WHERE backward_name_i18n_id IS NOT NULL
     ) AS subq
WHERE i18n.id = subq.i18n_id;

-- twinflow
UPDATE i18n
SET domain_id = subq.domain_id
FROM (
         SELECT tf.name_i18n_id AS i18n_id, tc.domain_id
         FROM twinflow tf
                  JOIN twin_class tc ON tc.id = tf.twin_class_id
         WHERE tf.name_i18n_id IS NOT NULL

         UNION

         SELECT tf.description_i18n_id AS i18n_id, tc.domain_id
         FROM twinflow tf
                  JOIN twin_class tc ON tc.id = tf.twin_class_id
         WHERE tf.description_i18n_id IS NOT NULL
     ) AS subq
WHERE i18n.id = subq.i18n_id;

-- twinflow_transition
UPDATE i18n
SET domain_id = subq.domain_id
FROM (
         SELECT
             tft.name_i18n_id AS i18n_id,
             tc.domain_id
         FROM twinflow_transition tft
                  JOIN twinflow tf ON tf.id = tft.twinflow_id
                  JOIN twin_class tc ON tc.id = tf.twin_class_id
         WHERE tft.name_i18n_id IS NOT NULL

         UNION

         SELECT
             tft.description_i18n_id AS i18n_id,
             tc.domain_id
         FROM twinflow_transition tft
                  JOIN twinflow tf ON tf.id = tft.twinflow_id
                  JOIN twin_class tc ON tc.id = tf.twin_class_id
         WHERE tft.description_i18n_id IS NOT NULL
     ) AS subq
WHERE i18n.id = subq.i18n_id;

-- space_role
UPDATE i18n
SET domain_id = tc.domain_id
FROM space_role sr
         JOIN twin_class tc ON tc.id = sr.twin_class_id
WHERE i18n.id = sr.name_i18n_id;

-- permission
UPDATE i18n
SET domain_id = pg.domain_id
FROM (
         SELECT
             p.name_i18n_id AS i18n_id,
             pg.domain_id
         FROM permission p
                  JOIN permission_group pg ON pg.id = p.permission_group_id
         WHERE p.name_i18n_id IS NOT NULL

         UNION ALL

         SELECT
             p.description_i18n_id AS i18n_id,
             pg.domain_id
         FROM permission p
                  JOIN permission_group pg ON pg.id = p.permission_group_id
         WHERE p.description_i18n_id IS NOT NULL
     ) AS pg
WHERE i18n.id = pg.i18n_id;

-- user_group
UPDATE i18n
SET domain_id = ug.domain_id
FROM (
         SELECT name_i18n_id AS i18n_id, domain_id
         FROM user_group
         WHERE name_i18n_id IS NOT NULL

         UNION ALL

         SELECT description_i18n_id AS i18n_id, domain_id
         FROM user_group
         WHERE description_i18n_id IS NOT NULL
     ) ug
WHERE i18n.id = ug.i18n_id;

-- user_class_owner_type
UPDATE i18n
SET domain_id = subq.domain_id
FROM (
         SELECT
             tcot.name_i18n_id AS i18n_id,
             tc.domain_id
         FROM twin_class_owner_type tcot
                  JOIN twin_class tc ON tc.twin_class_owner_type_id = tcot.id
         WHERE tcot.name_i18n_id IS NOT NULL

         UNION ALL

         SELECT
             tcot.description_i18n_id AS i18n_id,
             tc.domain_id
         FROM twin_class_owner_type tcot
                  JOIN twin_class tc ON tc.twin_class_owner_type_id = tcot.id
         WHERE tcot.description_i18n_id IS NOT NULL
     ) subq
WHERE i18n.id = subq.i18n_id;

-- twin_factory
UPDATE i18n
SET domain_id = tf.domain_id
FROM (
         SELECT name_i18n_id AS i18n_id, domain_id
         FROM twin_factory
         WHERE name_i18n_id IS NOT NULL

         UNION ALL

         SELECT description_i18n_id AS i18n_id, domain_id
         FROM twin_factory
         WHERE description_i18n_id IS NOT NULL
     ) tf
WHERE i18n.id = tf.i18n_id;

-- face_navbar_nb001
UPDATE i18n
SET domain_id = f.domain_id
FROM face_navbar_nb001 fn
         JOIN face f ON f.id = fn.face_id
WHERE i18n.id = fn.admin_area_label_i18n_id;


-- face_navbar_nb001_menu_items
UPDATE i18n
SET domain_id = f.domain_id
FROM face_navbar_nb001_menu_items fn
         JOIN face f ON f.id = fn.face_id
WHERE i18n.id = fn.description_i18n_id;

-- face_page_pg001
UPDATE i18n
SET domain_id = f.domain_id
FROM face_page_pg001 fp1
         JOIN face f ON f.id = fp1.face_id
WHERE i18n.id = fp1.title_i18n_id;

-- face_page_pg002
UPDATE i18n
SET domain_id = f.domain_id
FROM face_page_pg002 fp2
         JOIN face f ON f.id = fp2.face_id
WHERE i18n.id = fp2.title_i18n_id;

-- face_page_pg002_tab
UPDATE i18n
SET domain_id = f.domain_id
FROM face_page_pg002_tab fp2
         JOIN face f ON f.id = fp2.face_id
WHERE i18n.id = fp2.title_i18n_id;

-- face_twidget_tw001
UPDATE i18n
SET domain_id = f.domain_id
FROM face_twidget_tw001 ft1
         JOIN face f ON f.id = ft1.face_id
WHERE i18n.id = ft1.label_i18n_id;

-- face_twidget_tw002
UPDATE i18n
SET domain_id = f.domain_id
FROM face_twidget_tw002 ft2
         JOIN face f ON f.id = ft2.face_id
WHERE i18n.id = ft2.label_i18n_id;

-- face_twidget_tw004
UPDATE i18n
SET domain_id = f.domain_id
FROM face_twidget_tw004 ft4
         JOIN face f ON f.id = ft4.face_id
WHERE i18n.id = ft4.label_i18n_id;

-- face_widget_wt001
UPDATE i18n
SET domain_id = f.domain_id
FROM face_widget_wt001 fw
         JOIN face f ON f.id = fw.face_id
WHERE i18n.id = fw.label_i18n_id;

-- face_twidget_tw002_accordion_item
UPDATE i18n
SET domain_id = f.domain_id
FROM face_twidget_tw002_accordion_item fai
         JOIN face f ON f.id = fai.face_id
WHERE i18n.id = fai.label_i18n_id;

-- face_page_pg002_tab
UPDATE i18n
SET domain_id = f.domain_id
FROM face_page_pg002_tab fpt
         JOIN face f ON f.id = fpt.face_id
WHERE i18n.id = fpt.title_i18n_id;

-- data_list attribute names
UPDATE i18n
SET domain_id = dl.domain_id
    FROM (
    SELECT attribute_1_name_i18n_id AS i18n_id, domain_id
    FROM data_list
    WHERE attribute_1_name_i18n_id IS NOT NULL

    UNION ALL

    SELECT attribute_2_name_i18n_id AS i18n_id, domain_id
    FROM data_list
    WHERE attribute_2_name_i18n_id IS NOT NULL

    UNION ALL

    SELECT attribute_3_name_i18n_id AS i18n_id, domain_id
    FROM data_list
    WHERE attribute_3_name_i18n_id IS NOT NULL

    UNION ALL

    SELECT attribute_4_name_i18n_id AS i18n_id, domain_id
    FROM data_list
    WHERE attribute_4_name_i18n_id IS NOT NULL
) dl
WHERE i18n.id = dl.i18n_id;


-- add fk to data_list i18n
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'data_list'::regclass
              AND conname = 'fk_data_list_attribute_1_i18n'
        ) THEN
            EXECUTE 'ALTER TABLE data_list ADD CONSTRAINT fk_data_list_attribute_1_i18n
                FOREIGN KEY (attribute_1_name_i18n_id) REFERENCES i18n(id)
                ON DELETE SET NULL ON UPDATE CASCADE';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'data_list'::regclass
              AND conname = 'fk_data_list_attribute_2_i18n'
        ) THEN
            EXECUTE 'ALTER TABLE data_list ADD CONSTRAINT fk_data_list_attribute_2_i18n
                FOREIGN KEY (attribute_2_name_i18n_id) REFERENCES i18n(id)
                ON DELETE SET NULL ON UPDATE CASCADE';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'data_list'::regclass
              AND conname = 'fk_data_list_attribute_3_i18n'
        ) THEN
            EXECUTE 'ALTER TABLE data_list ADD CONSTRAINT fk_data_list_attribute_3_i18n
                FOREIGN KEY (attribute_3_name_i18n_id) REFERENCES i18n(id)
                ON DELETE SET NULL ON UPDATE CASCADE';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'data_list'::regclass
              AND conname = 'fk_data_list_attribute_4_i18n'
        ) THEN
            EXECUTE 'ALTER TABLE data_list ADD CONSTRAINT fk_data_list_attribute_4_i18n
                FOREIGN KEY (attribute_4_name_i18n_id) REFERENCES i18n(id)
                ON DELETE SET NULL ON UPDATE CASCADE';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'data_list'::regclass
              AND conname = 'fk_data_list_name_i18n'
        ) THEN
            EXECUTE 'ALTER TABLE data_list ADD CONSTRAINT fk_data_list_name_i18n
                FOREIGN KEY (name_i18n_id) REFERENCES i18n(id)
                ON DELETE SET NULL ON UPDATE CASCADE';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'data_list'::regclass
              AND conname = 'fk_data_list_description_i18n'
        ) THEN
            EXECUTE 'ALTER TABLE data_list ADD CONSTRAINT fk_data_list_description_i18n
                FOREIGN KEY (description_i18n_id) REFERENCES i18n(id)
                ON DELETE SET NULL ON UPDATE CASCADE';
        END IF;
    END $$;