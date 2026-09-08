package org.cambium.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class ValidationResult {
    boolean valid = false;
    String message;
    /**
     * Optional i18n of the user-facing error. Callers collect these ids and resolve
     * translations in one bulk query, then set {@link #message}.
     * Do not mutate {@link #VALID}.
     */
    UUID messageI18nId;

    public static final ValidationResult VALID = new ValidationResult(true);

    public ValidationResult() {
    }

    public ValidationResult(boolean valid) {
        this.valid = valid;
    }

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public void addMessage(String s) {
        if (message == null) message = s;
        else message += s;
    }
}
