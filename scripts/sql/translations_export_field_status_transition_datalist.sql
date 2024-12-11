SELECT
    ts.name_i18n_id AS i18n_id,
    CONCAT('class[', tc.key, '].status[', ts.key, '].name') AS reference,
    COALESCE(en.translation, '—') AS english_translation,
    COALESCE(pl.translation, '—') AS polish_translation,
    'status' AS source
FROM
    twin_status ts
        LEFT JOIN
    twin_class tc ON ts.twins_class_id = tc.id
        LEFT JOIN
    i18n_translation en ON en.i18n_id = ts.name_i18n_id AND en.locale = 'en'
        LEFT JOIN
    i18n_translation pl ON pl.i18n_id = ts.name_i18n_id AND pl.locale = 'pl'

UNION ALL

SELECT
    tcf.name_i18n_id AS i18n_id,
    CONCAT('class[', tc.key, '].field[', tcf.key, '].name') AS reference,
    COALESCE(en.translation, '—') AS english_translation,
    COALESCE(pl.translation, '—') AS polish_translation,
    'field' AS source
FROM
    twin_class_field tcf
        LEFT JOIN
    twin_class tc ON tcf.twin_class_id = tc.id
        LEFT JOIN
    i18n_translation en ON en.i18n_id = tcf.name_i18n_id AND en.locale = 'en'
        LEFT JOIN
    i18n_translation pl ON pl.i18n_id = tcf.name_i18n_id AND pl.locale = 'pl'

UNION ALL

SELECT
    tt.name_i18n_id AS i18n_id,
    CONCAT('class[', tc.key, '].transition[', tta.alias, '].src[', ts.key ,'].name') AS reference,
    COALESCE(en.translation, '—') AS english_translation,
    COALESCE(pl.translation, '—') AS polish_translation,
    'transition' AS source
FROM
    twinflow_transition tt
        LEFT JOIN
    twinflow_transition_alias tta ON tt.twinflow_transition_alias_id = tta.id
        LEFT JOIN
    twinflow tf ON tf.id = tt.twinflow_id
        LEFT JOIN
    twin_class tc ON tf.twin_class_id = tc.id
        LEFT JOIN
    twin_status ts ON tt.src_twin_status_id = ts.id
        LEFT JOIN
    i18n_translation en ON en.i18n_id = tt.name_i18n_id AND en.locale = 'en'
        LEFT JOIN
    i18n_translation pl ON pl.i18n_id = tt.name_i18n_id AND pl.locale = 'pl'

UNION ALL

SELECT
    dlo.option_i18n_id AS i18n_id,
    CONCAT('datalist[', dl.key, '].option[', dlo.option, ']') AS reference,
    COALESCE(en.translation, '—') AS english_translation,
    COALESCE(pl.translation, '—') AS polish_translation,
    'option' AS source
FROM
    data_list_option dlo
        LEFT JOIN
    data_list dl ON dlo.data_list_id = dl.id
        LEFT JOIN
    i18n_translation en ON en.i18n_id = dlo.option_i18n_id AND en.locale = 'en'
        LEFT JOIN
    i18n_translation pl ON pl.i18n_id = dlo.option_i18n_id AND pl.locale = 'pl';

