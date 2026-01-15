package org.twins.architecture.dto;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static org.twins.architecture.dto.DtoHelpers.mustHaveField;
import static org.twins.architecture.dto.DtoPredicates.*;

/**
 * ArchUnit tests for DTO Code Convention.
 *
 * Based on DTO Convention MD:
 *  - Unified naming
 *  - Clear inheritance hierarchy
 *  - Strict separation of Request / Response DTOs
 */
@AnalyzeClasses(packages = "org.twins")
public class DtoConventionArchTest {

    /**
     * All DTO classes must be located in dto packages.
     */
    @ArchTest
    static final ArchRule dtoMustBeInDtoPackage =
            classes()
                    .that(isDto())
                    .should().resideInAnyPackage(
                            "org.twins.core.dto.rest..",
                            "org.twins.face.dto.rest.."
                    )
                    .because("All DTO classes must be located in dto packages");


//    /**
//     * All Request DTOs must extend base Request class.
//     */
//    @ArchTest
//    static final ArchRule requestDtoMustExtendRequest =
//            classes()
//                    .that(isRequestDto())
//                    .should().beAssignableTo(Request.class)
//                    .because("All Request DTOs must extend Request");


    /**
     * Create DTO must NOT contain 'id' field.
     */
    @ArchTest
    static final ArchRule createDtoMustNotContainId =
            noFields()
                    .that(isIdField())
                    .should().beDeclaredInClassesThat(isCreateDto())
                    .because("Create DTO must not contain 'id' field");

    /**
     * Update DTO MUST contain 'id' field.
     */
    @ArchTest
    static final ArchRule updateDtoMustContainId =
            classes()
                    .that(isUpdateDto())
                    .should(mustHaveField("id"))
                    .because("Every UpdateDTO must contain 'id' field");
//
//    // -------------------------------------------------------------------------
//    // 4. Response hierarchy rules
//    // -------------------------------------------------------------------------

    /**
     * SearchRsDTO must extend ListRsDTO.
     */
    @ArchTest
    static final ArchRule searchRsMustExtendListRs =
            classes()
                    .that(isResponseDto())
                    .and().haveSimpleNameEndingWith("SearchRsDTO")
                    .should().haveSuperclassThat()
                    .haveSimpleNameEndingWith("ListRsDTO")
                    .because("SearchRsDTO must extend ListRsDTO");

//    // -------------------------------------------------------------------------
//    // 5. Pagination rules
//    // -------------------------------------------------------------------------
//
//    /**
//     * Pagination DTO is allowed only in SearchRsDTO.
//     */
//    @ArchTest
//    static final ArchRule paginationAllowedOnlyInSearchRs =
//            fields()
//                    .that().haveRawType(
//                            org.twins.core.dto.rest.pagination.PaginationDTOv1.class
//                    )
//                    .should().beDeclaredInClassesThat()
//                    .haveSimpleNameEndingWith("SearchRsDTO")
//                    .because("Pagination is allowed only in SearchRsDTO");

}

