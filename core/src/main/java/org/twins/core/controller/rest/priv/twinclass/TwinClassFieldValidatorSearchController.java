package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
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
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldValidatorRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldValidatorSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorSearchService;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_MANAGE, Permissions.TWIN_CLASS_FIELD_VIEW})
public class TwinClassFieldValidatorSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinClassFieldValidatorSearchService twinClassFieldValidatorSearchService;
    private final TwinClassFieldValidatorSearchRestDTOReverseMapper twinClassFieldValidatorSearchRestDTOReverseMapper;
    private final TwinClassFieldValidatorRestDTOMapper twinClassFieldValidatorRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldValidatorSearchV1", summary = "Twin class field validator search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field validator search", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldValidatorSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field_validator/search/v1")
    public ResponseEntity<?> twinClassFieldValidatorSearchV1(
            @MapperContextBinding(roots = TwinClassFieldValidatorRestDTOMapper.class, response = TwinClassFieldValidatorSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinClassFieldValidatorSearchRqDTOv1 request) {
        TwinClassFieldValidatorSearchRsDTOv1 rs = new TwinClassFieldValidatorSearchRsDTOv1();
        try {
            PaginationResult<TwinClassFieldValidatorEntity> result = twinClassFieldValidatorSearchService
                    .search(twinClassFieldValidatorSearchRestDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(result))
                    .setValidators(twinClassFieldValidatorRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
