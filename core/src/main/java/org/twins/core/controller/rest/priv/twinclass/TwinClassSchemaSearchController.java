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
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassSchemaDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassSchemaSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassSchemaSearchService;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassSchemaSearchController extends ApiController {
    private final TwinClassSchemaDTOMapper twinClassSchemaDTOMapper;
    private final TwinClassSchemaSearchService twinClassSchemaSearchService;
    private final TwinClassSchemaSearchRestDTOReverseMapper twinClassSchemaSearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassSchemaSearchV1", summary = "Returns twin class schemas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class schema list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassSchemaSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_schema/search/v1")
    public ResponseEntity<?> twinClassSchemaSearchV1(
            @MapperContextBinding(roots = TwinClassSchemaDTOMapper.class, response = TwinClassSchemaSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinClassSchemaSearchRqDTOv1 request) {
        TwinClassSchemaSearchRsDTOv1 rs = new TwinClassSchemaSearchRsDTOv1();
        try {
            PaginationResult<TwinClassSchemaEntity> twinClassSchemaList = twinClassSchemaSearchService.findTwinClassSchemas(twinClassSchemaSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(twinClassSchemaList))
                    .setTwinClassSchemas(twinClassSchemaDTOMapper.convertCollection(twinClassSchemaList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}