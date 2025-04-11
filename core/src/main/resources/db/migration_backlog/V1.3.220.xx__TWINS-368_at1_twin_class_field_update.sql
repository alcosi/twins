UPDATE public.twin_class_field
SET required = false
WHERE required IS NULL;

ALTER TABLE public.twin_class_field
    ALTER COLUMN required SET NOT NULL;
