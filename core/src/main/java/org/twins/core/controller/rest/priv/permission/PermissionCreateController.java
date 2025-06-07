package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.twins.core.dto.rest.permission.PermissionCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionCreateRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionCreateRestReverseDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Create permission", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_MANAGE, Permissions.PERMISSION_CREATE})
public class PermissionCreateController extends ApiController {

    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionCreateRestReverseDTOMapper permissionCreateRestReverseDTOMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final PermissionService permissionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionCreateV1", summary = "Create permission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission/v1")
    public ResponseEntity<?> permissionCreateV1(
            @MapperContextBinding(roots = PermissionRestDTOMapper.class, response = PermissionCreateRsDTOv1.class) MapperContext mapperContext,
            @RequestBody PermissionCreateRqDTOv1 request) {
        PermissionCreateRsDTOv1 rs = new PermissionCreateRsDTOv1();
        try {
            I18nEntity nameI18n = i18NSaveRestDTOReverseMapper.convert(request.getNameI18n(), mapperContext);
            I18nEntity descriptionI18n = i18NSaveRestDTOReverseMapper.convert(request.getDescriptionI18n(), mapperContext);
            rs.setPermission(permissionRestDTOMapper
                    .convert(permissionService.createPermission(permissionCreateRestReverseDTOMapper
                            .convert(request, mapperContext), nameI18n, descriptionI18n), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
