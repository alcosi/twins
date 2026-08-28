-- Make twin_factory_multiplier_filter cascade-delete with its multiplier, mirroring
-- pipeline_step -> pipeline and condition -> condition_set. Lets the factory clearElements
-- export flow delete only the parent (twin_factory_multiplier) and rely on the DB to purge
-- its filters, instead of emitting a separate subquery DELETE.
ALTER TABLE twin_factory_multiplier_filter
    DROP CONSTRAINT IF EXISTS twin_factory_multiplier_filter_twin_factory_multiplier_id_fk;

ALTER TABLE twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_twin_factory_multiplier_id_fk
        FOREIGN KEY (twin_factory_multiplier_id) REFERENCES public.twin_factory_multiplier(id)
        ON DELETE CASCADE;
