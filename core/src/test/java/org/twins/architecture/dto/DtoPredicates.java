package org.twins.architecture.dto;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;

import java.util.regex.Pattern;

/**
 * Common ArchUnit predicates for DTO conventions.
 * Single source of truth for DTO naming rules.
 */
public final class DtoPredicates {

    private DtoPredicates() {
    }

    public static final Pattern DTO_CLASS_NAME_PATTERN =
            Pattern.compile(".*DTO(v\\d+)?$");

    public static final Pattern REQUEST_DTO_CLASS_NAME_PATTERN =
            Pattern.compile(".*RqDTO(v\\d+)?$");

    public static final Pattern RESPONSE_DTO_CLASS_NAME_PATTERN =
            Pattern.compile(".*RsDTO(v\\d+)?$");

    public static final Pattern CREATE_DTO_CLASS_NAME_PATTERN =
            Pattern.compile(".*CreateDTO(v\\d+)?$");

    public static final Pattern UPDATE_DTO_CLASS_NAME_PATTERN =
            Pattern.compile(".*UpdateDTO(v\\d+)?$");

    /** Any DTO (DTO / DTOv1 / DTOv2 ...) */
    public static DescribedPredicate<JavaClass> isDto() {
        return new DescribedPredicate<>("DTO class") {
            @Override
            public boolean test(JavaClass javaClass) {
                return DTO_CLASS_NAME_PATTERN
                        .matcher(javaClass.getSimpleName())
                        .matches();
            }
        };
    }

    /** Request DTO (RqDTO / RqDTOv1 ...) */
    public static DescribedPredicate<JavaClass> isRequestDto() {
        return new DescribedPredicate<>("Request DTO") {
            @Override
            public boolean test(JavaClass javaClass) {
                return REQUEST_DTO_CLASS_NAME_PATTERN
                        .matcher(javaClass.getSimpleName())
                        .matches();
            }
        };
    }

    /** Response DTO (RsDTO / RsDTOv1 ...) */
    public static DescribedPredicate<JavaClass> isResponseDto() {
        return new DescribedPredicate<>("Response DTO") {
            @Override
            public boolean test(JavaClass javaClass) {
                return RESPONSE_DTO_CLASS_NAME_PATTERN
                        .matcher(javaClass.getSimpleName())
                        .matches();
            }
        };
    }


    /** Create DTO */
    public static DescribedPredicate<JavaClass> isCreateDto() {
        return new DescribedPredicate<>("Create DTO") {
            @Override
            public boolean test(JavaClass javaClass) {
                return CREATE_DTO_CLASS_NAME_PATTERN
                        .matcher(javaClass.getSimpleName())
                        .matches();
            }
        };
    }

    /** Update DTO */
    public static DescribedPredicate<JavaClass> isUpdateDto() {
        return new DescribedPredicate<>("Update DTO") {
            @Override
            public boolean test(JavaClass javaClass) {
                return UPDATE_DTO_CLASS_NAME_PATTERN
                        .matcher(javaClass.getSimpleName())
                        .matches();
            }
        };
    }

    /** Field named 'id' */
    public static DescribedPredicate<JavaField> isIdField() {
        return new DescribedPredicate<>("field named 'id'") {
            @Override
            public boolean test(JavaField field) {
                return field.getName().equals("id");
            }
        };
    }
}