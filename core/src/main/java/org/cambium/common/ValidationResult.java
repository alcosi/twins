package org.cambium.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ValidationResult {
    boolean valid = false;
    String message;

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
