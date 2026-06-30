package org.twins.bootstrap;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory representation of a parsed glossary entity file.
 * Identity + structured body sections + bootstrap metadata.
 *
 * @param slug                   kebab-case, matches filename stem (regex ^[a-z][a-z0-9-]*$)
 * @param title                  display name, non-empty
 * @param category               one of: core|workflow|multi-tenancy|permissions|content|cross-cutting|fields|validation|other
 * @param jpaClass               optional JPA simple name (e.g. "TwinEntity")
 * @param dbTable                optional DB table name (e.g. "twin")
 * @param isSystem               optional, default false
 * @param actualizedAt           ISO date — last human curation review
 * @param seeAlso                set of slugs referenced from this entity
 * @param sectionSummary         body of H2 "## Summary" (required) — written to Twin.description
 * @param sectionPurpose         body of H2 "## Purpose"
 * @param sectionFields          body of H2 "## Fields" (required)
 * @param sectionRelations       body of H2 "## Relations"
 * @param sectionApi             body of H2 "## API"
 * @param sectionApiDeprecated   body of H2 "## API (deprecated)"
 * @param sectionExamples        body of H2 "## Examples"
 * @param sectionDevNotes        body of H2 "## Dev notes"
 * @param markdownSource         classpath-relative path (e.g. "docs/glossary/entities/twin.md")
 * @param markdownHash           SHA-256 hex of file bytes, computed at parse time
 * @param twinId                 deterministic UUIDv5 from ("glossary:" + slug)
 */
public record GlossaryEntityDto(
        String slug,
        String title,
        String category,
        String jpaClass,
        String dbTable,
        boolean isSystem,
        LocalDate actualizedAt,
        Set<String> seeAlso,
        String sectionSummary,
        String sectionPurpose,
        String sectionFields,
        String sectionRelations,
        String sectionApi,
        String sectionApiDeprecated,
        String sectionExamples,
        String sectionDevNotes,
        String markdownSource,
        String markdownHash,
        UUID twinId
) {
    public static UUID computeTwinId(String slug) {
        return UUID.nameUUIDFromBytes(("glossary:" + slug).getBytes(StandardCharsets.UTF_8));
    }
}
