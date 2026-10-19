package org.twins.bootstrap;

/**
 * Thrown by {@link GlossaryMarkdownParser} when a markdown file fails validation
 * (malformed frontmatter, missing required sections, invalid slug/category, etc.).
 *
 * Caught by {@link GlossaryMarkdownParser#parseAll()} which logs the failure at WARN
 * and excludes the file from the bootstrap pass — does NOT abort the whole pass.
 */
public class GlossaryParseException extends RuntimeException {

    private final String source;

    public GlossaryParseException(String source, String message) {
        super(source + ": " + message);
        this.source = source;
    }

    public GlossaryParseException(String source, String message, Throwable cause) {
        super(source + ": " + message, cause);
        this.source = source;
    }

    public String source() {
        return source;
    }
}
