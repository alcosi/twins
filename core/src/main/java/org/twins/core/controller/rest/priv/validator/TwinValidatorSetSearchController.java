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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dto.rest.validator.TwinValidatorSetSearchRqDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorSetSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.validator.TwinValidatorSetRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.validator.TwinValidatorSetSearchService;

@Tag(name = ApiTag.TWIN_VALIDATOR)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_VALIDATOR_SET_MANAGE, Permissions.TWIN_VALIDATOR_SET_VIEW})
public class TwinValidatorSetSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinValidatorSetSearchService twinValidatorSetSearchService;
    private final TwinValidatorSetSearchRestDTOReverseMapper twinValidatorSetSearchRestDTOReverseMapper;
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinValidatorSetSearchV1", summary = "Twin validator set search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin validator set search", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinValidatorSetSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_validator_set/search/v1")
    public ResponseEntity<?> twinValidatorSetSearchV1(
            @MapperContextBinding(roots = TwinValidatorSetRestDTOMapper.class, response = TwinValidatorSetSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinValidatorSetSearchRqDTOv1 request) {
        TwinValidatorSetSearchRsDTOv1 rs = new TwinValidatorSetSearchRsDTOv1();
        try {
            PaginationResult<TwinValidatorSetEntity> validatorSetsList = twinValidatorSetSearchService.findTwinValidatorSetsForDomain(
                    twinValidatorSetSearchRestDTOReverseMapper.convert(request), pagination);
            rs
                    .setPagination(paginationMapper.convert(validatorSetsList))
                    .setValidatorSets(twinValidatorSetRestDTOMapper.convertCollection(validatorSetsList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
