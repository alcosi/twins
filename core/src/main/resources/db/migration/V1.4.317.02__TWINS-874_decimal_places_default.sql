-- TWINS-868: FieldTyperNumeric now extends FieldTyperScalable, so the decimalPlaces default
-- changed from 0 to 2 (inherited from FieldTyperScalable). For pure input decimal field types
-- (FieldTyperDecimal = 1317, FieldTyperDecimalIncrement = 1350) the former implicit default 0
-- was the ONLY meaning of decimalPlaces (input formatting / stored-value rendering), so we
-- persist it explicitly for fields that did not configure decimalPlaces — preserving their
-- existing behavior. New fields of these types will pick up the new default 2.
--
-- Aggregation Mater types (CalcSumMater = 1352, CalcSubtractionMater = 1353,
-- CalcMultiplicationMater = 1355, CalcDivisionMater = 1356, CalcChildrenFieldV2 = 1313) are
-- intentionally NOT migrated: their scaleAndRound already used the FieldTyperScalable default 2,
-- and forcing 0 here would turn their sums/divisions into integers. They keep default 2.
--
-- Only adds the key when it is absent; never overwrites an explicitly configured value or any
-- sibling parameter (hstore || merges, other keys are untouched).

UPDATE twin_class_field
SET field_typer_params = COALESCE(field_typer_params, ''::hstore) || '"decimalPlaces"=>"0"'::hstore
WHERE field_typer_featurer_id IN (1317, 1350)
  AND COALESCE(field_typer_params, ''::hstore) -> 'decimalPlaces' IS NULL;
