package org.twins.bootstrap;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses glossary markdown files from the classpath into {@link GlossaryEntityDto} records.
 *
 * File format (see ai/plans/glossary-as-twins.md §3):
 * <pre>
 * ---
 * slug: twin
 * title: Twin
 * category: core
 * class: TwinEntity          # optional
 * table: twin                # optional
 * is_system: false           # optional
 * actualized_at: 2026-06-17
 * see_also:                  # optional list
 *   - twin-class
 *   - twinflow
 * ---
 *
 * # Twin
 *
 * ## Summary
 * ...
 *
 * ## Fields
 * ...
 * </pre>
 *
 * Files that fail validation are logged at WARN level and excluded from the returned
 * list — they do NOT abort the bootstrap pass.
 */
@Slf4j
@Component
public class GlossaryMarkdownParser {

    private static final String CLASSPATH_PATTERN = "classpath:/docs/glossary/entities/*.md";
    private static final YAMLMapper YAML = new YAMLMapper();

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z][a-z0-9-]*$");
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "core", "workflow", "multi-tenancy", "permissions", "content",
            "cross-cutting", "fields", "validation", "other");
    private static final Set<String> KNOWN_SECTIONS = Set.of(
            "Summary", "Purpose", "Fields", "Relations",
            "API", "API (deprecated)", "Examples", "Dev notes");
    private static final Set<String> REQUIRED_SECTIONS = Set.of("Summary", "Fields");
    private static final Pattern H1_PATTERN = Pattern.compile("^#\\s+(.+?)\\s*$");
    private static final Pattern H2_PATTERN = Pattern.compile("^##\\s+(.+?)\\s*$");

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    /**
     * Scan classpath for all glossary markdown files, parse each.
     * Files that fail validation are logged at WARN level and excluded
     * from the returned list — they do NOT abort the bootstrap pass.
     *
     * @return list of valid DTOs (one per successfully-parsed file); empty list if no files found
     */
    public List<GlossaryEntityDto> parseAll() {
        Resource[] resources;
        try {
            resources = resolver.getResources(CLASSPATH_PATTERN);
        } catch (IOException e) {
            log.error("Failed to scan classpath for glossary markdown files: {}", e.getMessage(), e);
            return List.of();
        }
        log.info("Glossary markdown scan: {} candidate file(s) under {}", resources.length, CLASSPATH_PATTERN);
        List<GlossaryEntityDto> result = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            String source = resource.getFilename();
            try {
                GlossaryEntityDto dto = parse(resource);
                result.add(dto);
            } catch (GlossaryParseException e) {
                log.warn("Skipping glossary file (parse failed): {}", e.getMessage());
            } catch (Exception e) {
                log.warn("Skipping glossary file (unexpected error): {} — {}", source, e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * Parse a single markdown file. Throws {@link GlossaryParseException} on schema violation.
     */
    public GlossaryEntityDto parse(Resource mdFile) throws GlossaryParseException {
        String source = mdFile.getFilename();
        byte[] bytes;
        try (InputStream is = mdFile.getInputStream()) {
            bytes = is.readAllBytes();
        } catch (IOException e) {
            throw new GlossaryParseException(source, "cannot read file: " + e.getMessage(), e);
        }
        String markdownHash = DigestUtils.sha256Hex(bytes);
        String content = stripBom(new String(bytes, StandardCharsets.UTF_8)).replace("\r\n", "\n").replace('\r', '\n');

        String[] split = content.split("^---\\s*$", 3);
        if (split.length < 3) {
            throw new GlossaryParseException(source, "missing YAML frontmatter (need opening and closing '---' lines)");
        }
        String frontmatter = split[1];
        String body = split[2];
        Map<String, Object> yaml = parseYaml(source, frontmatter);

        String slug = requireString(source, yaml, "slug");
        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new GlossaryParseException(source, "slug '" + slug + "' does not match " + SLUG_PATTERN.pattern());
        }
        String filenameStem = source == null ? null : source.replaceAll("\\.md$", "");
        if (filenameStem != null && !filenameStem.equals(slug)) {
            throw new GlossaryParseException(source, "slug '" + slug + "' does not match filename stem '" + filenameStem + "'");
        }
        String title = requireString(source, yaml, "title");
        String category = requireString(source, yaml, "category");
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new GlossaryParseException(source, "category '" + category + "' not in allowed set " + ALLOWED_CATEGORIES);
        }
        String jpaClass = optionalString(yaml, "class");
        String dbTable = optionalString(yaml, "table");
        boolean isSystem = optionalBoolean(yaml, "is_system", false);
        LocalDate actualizedAt = parseActualizedAt(source, yaml);
        Set<String> seeAlso = parseSeeAlso(source, yaml);

        Map<String, String> sections = parseBody(source, body, title);

        return new GlossaryEntityDto(
                slug, title, category, jpaClass, dbTable, isSystem, actualizedAt,
                seeAlso, sections,
                "docs/glossary/entities/" + (source != null ? source : slug + ".md"),
                markdownHash,
                GlossaryEntityDto.computeTwinId(slug));
    }

    private static String stripBom(String s) {
        if (s != null && s.startsWith("﻿")) {
            return s.substring(1);
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(String source, String frontmatter) throws GlossaryParseException {
        try {
            Map<String, Object> parsed = YAML.readValue(frontmatter, Map.class);
            return parsed == null ? Collections.emptyMap() : parsed;
        } catch (Exception e) {
            throw new GlossaryParseException(source, "YAML frontmatter parse error: " + e.getMessage(), e);
        }
    }

    private static String requireString(String source, Map<String, Object> yaml, String key) throws GlossaryParseException {
        Object v = yaml.get(key);
        if (v == null || v.toString().isBlank()) {
            throw new GlossaryParseException(source, "frontmatter missing required key '" + key + "'");
        }
        return v.toString();
    }

    private static String optionalString(Map<String, Object> yaml, String key) {
        Object v = yaml.get(key);
        return v == null ? null : v.toString();
    }

    private static boolean optionalBoolean(Map<String, Object> yaml, String key, boolean defaultValue) {
        Object v = yaml.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    private static LocalDate parseActualizedAt(String source, Map<String, Object> yaml) throws GlossaryParseException {
        Object v = yaml.get("actualized_at");
        if (v == null) {
            throw new GlossaryParseException(source, "frontmatter missing required key 'actualized_at'");
        }
        String s = v.toString();
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new GlossaryParseException(source, "actualized_at '" + s + "' is not a valid ISO date (YYYY-MM-DD)", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> parseSeeAlso(String source, Map<String, Object> yaml) throws GlossaryParseException {
        Object v = yaml.get("see_also");
        if (v == null) return Set.of();
        Set<String> result = new HashSet<>();
        if (v instanceof List<?> list) {
            for (Object item : list) {
                if (item == null || item.toString().isBlank()) {
                    throw new GlossaryParseException(source, "see_also contains blank entry");
                }
                result.add(item.toString());
            }
        } else {
            throw new GlossaryParseException(source, "see_also must be a list of slugs, got " + v.getClass().getSimpleName());
        }
        return result;
    }

    /**
     * Parse body into Map<sectionName, markdownBody>. First H1 must match title.
     * Unknown sections are logged at WARN and skipped. Required sections enforced.
     */
    private Map<String, String> parseBody(String source, String body, String expectedTitle) throws GlossaryParseException {
        String[] lines = body.split("\n", -1);
        String title = null;
        String currentSection = null;
        StringBuilder currentContent = null;
        Map<String, String> sections = new LinkedHashMap<>();
        for (String raw : lines) {
            String line = raw;
            Matcher h1 = H1_PATTERN.matcher(line);
            Matcher h2 = H2_PATTERN.matcher(line);
            if (h1.matches()) {
                if (title != null) {
                    log.warn("{}: extra H1 header encountered ('{}') — only the first H1 is used as title", source, h1.group(1));
                    continue;
                }
                title = h1.group(1).trim();
                continue;
            }
            if (h2.matches()) {
                if (currentSection != null) {
                    flushSection(source, sections, currentSection, currentContent);
                }
                currentSection = h2.group(1).trim();
                currentContent = new StringBuilder();
                continue;
            }
            if (currentSection != null) {
                if (currentContent.length() > 0 || !line.isBlank()) {
                    currentContent.append(line).append('\n');
                }
            }
        }
        if (currentSection != null) {
            flushSection(source, sections, currentSection, currentContent);
        }
        if (title == null) {
            throw new GlossaryParseException(source, "body missing required H1 header with title");
        }
        if (!title.equals(expectedTitle)) {
            log.warn("{}: H1 title '{}' does not match frontmatter title '{}' — using H1 as authoritative", source, title, expectedTitle);
        }
        for (String required : REQUIRED_SECTIONS) {
            if (!sections.containsKey(required)) {
                throw new GlossaryParseException(source, "body missing required section '## " + required + "'");
            }
        }
        return sections;
    }

    private static void flushSection(String source, Map<String, String> sections, String name, StringBuilder content) {
        if (!KNOWN_SECTIONS.contains(name)) {
            log.warn("{}: skipping unknown section '## {}' (not in standard set)", source, name);
            return;
        }
        if (sections.containsKey(name)) {
            log.warn("{}: duplicate section '## {}' — second occurrence overrides the first", source, name);
        }
        String body = content == null ? "" : content.toString().stripTrailing();
        sections.put(name, body);
    }

    // Suppress unused-import warning for Locale (kept for future i18n translation lookups)
    @SuppressWarnings("unused")
    private static final Locale UNUSED_LOCALE_REF = Locale.ENGLISH;
}
