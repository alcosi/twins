package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionCreateDTOv1;
import org.twins.core.dto.rest.permission.PermissionCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionListRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionCreateRestReverseDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.util.ArrayList;
import java.util.List;

@Tag(description = "Create permission", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.PERMISSION_CREATE)
public class PermissionCreateController extends ApiController {

    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionCreateRestReverseDTOMapper permissionCreateRestReverseDTOMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PermissionService permissionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionCreateV1", summary = "Permission batch add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission/v1")
    public ResponseEntity<?> permissionCreateV1(
            @MapperContextBinding(roots = PermissionRestDTOMapper.class, response = PermissionListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid PermissionCreateRqDTOv1 request) {
        PermissionListRsDTOv1 rs = new PermissionListRsDTOv1();
        try {
            List<I18nEntity> namesI18n = new ArrayList<>(request.getPermissions().size());
            List<I18nEntity> descriptionsI18n = new ArrayList<>(request.getPermissions().size());
            for (PermissionCreateDTOv1 permission : request.getPermissions()) {
                namesI18n.add(i18NSaveRestDTOReverseMapper.convert(permission.getNameI18n(), mapperContext));
                descriptionsI18n.add(i18NSaveRestDTOReverseMapper.convert(permission.getDescriptionI18n(), mapperContext));
            }
            List<PermissionEntity> permissionEntities = permissionService.createPermissions(
                    permissionCreateRestReverseDTOMapper.convertCollection(request.getPermissions()), namesI18n, descriptionsI18n);
            rs
                    .setPermissions(permissionRestDTOMapper.convertCollection(permissionEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
