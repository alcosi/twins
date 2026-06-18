-- TWINS-840: Add GIN trigram index on i18n_translation.translation for fast case-insensitive LIKE search.
--
-- Context:
-- I18nSpecification.joinAndSearchByI18NFieldDirect / doubleJoinAndSearchByI18NFieldDirect build predicates via
-- buildLikePredicates(), which compiles to: cb.like(cb.lower(path), '%' + value.toLowerCase() + '%', '\\').
-- Resulting SQL: WHERE lower(i18n_translation.translation) LIKE '%term%' ESCAPE '\'.
--
-- Existing UNIQUE(i18n_id, locale) constraint covers JOIN path (lookup by i18n_id + locale filter)
-- but does NOT cover LIKE filters on translation. Without this index, every i18n-based search
-- triggers a sequential scan over i18n_translation, which becomes a hotspot once ~20 entities
-- issue LIKE queries against it.
--
-- pg_trgm is required for gin_trgm_ops operator class. Listed as dependency because it was not
-- installed previously (only uuid-ossp, hstore, ltree, pgcrypto are present in earlier migrations).
-- CREATE EXTENSION requires superuser or database owner privileges — coordinate with DBA if it fails.
--
-- Note on CONCURRENTLY:
-- Flyway wraps migrations in a transaction, so CREATE INDEX CONCURRENTLY cannot be used here.
-- For large i18n_translation tables (>1M rows), consider running this statement manually with
-- CONCURRENTLY out-of-band before deploying the application version that depends on this index.
-- Build time on a typical dev table (<100k rows) is well under a second.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Functional GIN index on lower(translation) so PostgreSQL can serve both:
--   * LIKE '%term%' on lower(translation)
--   * ILIKE '%term%' on translation (planner rewrites ILIKE to lower() expression)
CREATE INDEX IF NOT EXISTS idx_i18n_translation_translation_lower_trgm
    ON i18n_translation USING gin (lower(translation) gin_trgm_ops);
