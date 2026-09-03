package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorCountRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldValidatorCountRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldValidatorSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorSearchService;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_MANAGE, Permissions.TWIN_CLASS_FIELD_VIEW})
public class TwinClassFieldValidatorCountController extends ApiController {
    private final TwinClassFieldValidatorSearchService twinClassFieldValidatorSearchService;
    private final TwinClassFieldValidatorSearchRestDTOReverseMapper twinClassFieldValidatorSearchRestDTOReverseMapper;
    private final TwinClassFieldValidatorCountRestDTOMapper twinClassFieldValidatorCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldValidatorCountV1", summary = "Return count of twin class field validators grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldValidatorCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field_validator/search/count/v1")
    public ResponseEntity<?> twinClassFieldValidatorCountV1(
            @MapperContextBinding(roots = TwinClassFieldValidatorCountRestDTOMapper.class, response = TwinClassFieldValidatorCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinClassFieldValidatorCountRqDTOv1 request) {
        TwinClassFieldValidatorCountRsDTOv1 rs = new TwinClassFieldValidatorCountRsDTOv1();
        try {
            var results = twinClassFieldValidatorSearchService.countByGroupFields(
                    twinClassFieldValidatorSearchRestDTOReverseMapper.convert(request.getSearch(), mapperContext),
                    request.getGroupFields(),
                    pagination);
            rs
                    .setCounts(twinClassFieldValidatorCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
