package org.twins.core.controller.rest.priv.validator;

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
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dto.rest.validator.TwinValidatorSearchRqDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.validator.TwinValidatorRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinvalidator.TwinValidatorSearchService;

@Tag(name = ApiTag.TWIN_VALIDATOR)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_VALIDATOR_MANAGE, Permissions.TWIN_VALIDATOR_VIEW})
public class TwinValidatorSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinValidatorSearchService twinValidatorSearchService;
    private final TwinValidatorSearchRestDTOReverseMapper twinValidatorSearchRestDTOReverseMapper;
    private final TwinValidatorRestDTOMapper twinValidatorRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinValidatorSearchV1", summary = "Twin validator search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin validator search", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinValidatorSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_validator/search/v1")
    public ResponseEntity<?> twinValidatorSearchV1(
            @MapperContextBinding(roots = TwinValidatorRestDTOMapper.class, response = TwinValidatorSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinValidatorSearchRqDTOv1 request) {
        TwinValidatorSearchRsDTOv1 rs = new TwinValidatorSearchRsDTOv1();
        try {
            PaginationResult<TwinValidatorEntity> validatorsList = twinValidatorSearchService
                    .search(twinValidatorSearchRestDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(validatorsList))
                    .setValidators(twinValidatorRestDTOMapper.convertCollection(validatorsList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
