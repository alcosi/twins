package org.twins.core.dto.rest.i18n;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that an {@link I18nSaveDTOv1} field carries at least one non-blank translation:
 * either {@code translationInCurrentLocale}, or at least one non-blank value in the {@code translations} map.
 * <p>
 * {@code null} field is considered valid — pair this annotation with {@code @NotNull} on mandatory fields
 * (e.g. {@code nameI18n} in Create DTOs) so that null and empty content produce distinct violation messages.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = I18nHasTranslationValidator.class)
public @interface I18nHasTranslation {
    String message() default "at least one non-blank translation is required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
