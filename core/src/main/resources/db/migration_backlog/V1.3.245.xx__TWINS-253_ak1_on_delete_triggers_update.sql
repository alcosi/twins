CREATE OR REPLACE FUNCTION permission_on_delete_i18n_and_translations_delete() RETURNS TRIGGER AS
$$
BEGIN
    BEGIN
        DELETE FROM i18n WHERE id = OLD.name_i18n_id;
    EXCEPTION
        WHEN others THEN
            RAISE NOTICE 'error: %', SQLERRM;
    END;

    BEGIN
        DELETE FROM i18n WHERE id = OLD.description_i18n_id;
    EXCEPTION
        WHEN others THEN
            RAISE NOTICE 'error: %', SQLERRM;
    END;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_on_delete_i18n_and_translations_delete() RETURNS TRIGGER AS
$$
BEGIN
    BEGIN
        DELETE FROM i18n WHERE id = OLD.name_i18n_id;
    EXCEPTION
        WHEN others THEN
            RAISE NOTICE 'error: %', SQLERRM;
    END;

    BEGIN
        DELETE FROM i18n WHERE id = OLD.description_i18n_id;
    EXCEPTION
        WHEN others THEN
            RAISE NOTICE 'error: %', SQLERRM;
    END;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;
