CREATE OR REPLACE FUNCTION ltree_is_root(ltree_path ltree)
RETURNS boolean AS $$
BEGIN
RETURN nlevel(ltree_path) = 1;
END;
$$ LANGUAGE plpgsql IMMUTABLE;