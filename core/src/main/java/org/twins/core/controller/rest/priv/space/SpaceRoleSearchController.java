package org.twins.core.controller.rest.priv.space;

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
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dto.rest.space.SpaceRoleSearchRqDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.space.SpaceRoleSearchService;

@Tag(name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.SPACE_ROLE_MANAGE, Permissions.SPACE_ROLE_VIEW})
public class SpaceRoleSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final SpaceRoleSearchDTOReverseMapper spaceRoleSearchDTOReverseMapper;
    private final SpaceRoleSearchService spaceRoleSearchService;
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleSearchListV1", summary = "Return a list of all space role for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Space role list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = SpaceRoleSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/space_role/search/v1")
    public ResponseEntity<?> spaceRoleSearchListV1(
            @MapperContextBinding(roots = SpaceRoleDTOMapper.class, response = SpaceRoleSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody SpaceRoleSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        SpaceRoleSearchRsDTOv1 rs = new SpaceRoleSearchRsDTOv1();
        try {
            PaginationResult<SpaceRoleEntity> spaceRoleList = spaceRoleSearchService
                    .findSpaceRole(spaceRoleSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setSpaceRoles(spaceRoleDTOMapper.convertCollection(spaceRoleList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(spaceRoleList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
