package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaSearchRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowSchemaSearchService;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWINFLOW_SCHEMA_MANAGE, Permissions.TWINFLOW_SCHEMA_VIEW})
public class TwinflowSchemaSearchController extends ApiController {
    private final TwinflowSchemaSearchRestDTOReverseMapper twinflowSchemaSearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinflowSchemaSearchService twinflowSchemaSearchService;
    private final TwinflowSchemaRestDTOMapper twinflowSchemaRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowSchemaSearchV1", summary = "Returns twinflow schema search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow schema list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowSchemaSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twinflow_schema/search/v1")
    public ResponseEntity<?> twinflowSchemaSearchV1(
            @MapperContextBinding(roots = TwinflowSchemaRestDTOMapper.class, response = TwinflowSchemaSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinflowSchemaSearchRqDTOv1 request) {
        TwinflowSchemaSearchRsDTOv1 rs = new TwinflowSchemaSearchRsDTOv1();
        try {
            PaginationResult<TwinflowSchemaEntity> twinflowSchemaList = twinflowSchemaSearchService
                    .findTwinflowSchemaForDomain(twinflowSchemaSearchRestDTOReverseMapper.convert(request), pagination);
            rs
                    .setTwinflowSchemas(twinflowSchemaRestDTOMapper.convertCollection(twinflowSchemaList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinflowSchemaList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
