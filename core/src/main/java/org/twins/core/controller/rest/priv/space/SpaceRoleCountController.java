package org.twins.core.controller.rest.priv.space;

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
import org.twins.core.dto.rest.space.SpaceRoleCountRqDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.space.SpaceRoleCountRestDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.space.SpaceRoleSearchService;

@Tag(name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.SPACE_ROLE_MANAGE, Permissions.SPACE_ROLE_VIEW})
public class SpaceRoleCountController extends ApiController {
    private final SpaceRoleSearchService spaceRoleSearchService;
    private final SpaceRoleSearchDTOReverseMapper spaceRoleSearchDTOReverseMapper;
    private final SpaceRoleCountRestDTOMapper spaceRoleCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleCountV1", summary = "Return count of space roles grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = SpaceRoleCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/space_role/count/v1")
    public ResponseEntity<?> spaceRoleCountV1(
            @MapperContextBinding(roots = SpaceRoleCountRestDTOMapper.class, response = SpaceRoleCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid SpaceRoleCountRqDTOv1 request) {
        SpaceRoleCountRsDTOv1 rs = new SpaceRoleCountRsDTOv1();
        try {
            var results = spaceRoleSearchService
                    .countByGroupFields(spaceRoleSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(spaceRoleCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
