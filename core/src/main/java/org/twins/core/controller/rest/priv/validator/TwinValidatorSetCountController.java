package org.twins.core.controller.rest.priv.validator;

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
import org.twins.core.dto.rest.validator.TwinValidatorSetCountRqDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorSetCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.validator.TwinValidatorSetCountRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinvalidator.TwinValidatorSetSearchService;

@Tag(name = ApiTag.TWIN_VALIDATOR)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_VALIDATOR_SET_MANAGE, Permissions.TWIN_VALIDATOR_SET_VIEW})
public class TwinValidatorSetCountController extends ApiController {
    private final TwinValidatorSetSearchService twinValidatorSetSearchService;
    private final TwinValidatorSetSearchRestDTOReverseMapper twinValidatorSetSearchRestDTOReverseMapper;
    private final TwinValidatorSetCountRestDTOMapper twinValidatorSetCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinValidatorSetCountV1", summary = "Return count of twin validator sets grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinValidatorSetCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_validator_set/count/v1")
    public ResponseEntity<?> twinValidatorSetCountV1(
            @MapperContextBinding(roots = TwinValidatorSetCountRestDTOMapper.class, response = TwinValidatorSetCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinValidatorSetCountRqDTOv1 request) {
        TwinValidatorSetCountRsDTOv1 rs = new TwinValidatorSetCountRsDTOv1();
        try {
            var results =
                    twinValidatorSetSearchService.countByGroupFields(twinValidatorSetSearchRestDTOReverseMapper
                            .convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(twinValidatorSetCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
