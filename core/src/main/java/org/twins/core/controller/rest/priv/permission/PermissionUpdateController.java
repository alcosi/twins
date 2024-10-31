package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.dao.I18nEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.permission.PermissionUpdateRqDTOv1;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;

import org.twins.core.mappers.rest.permission.PermissionUpdateRestReverseDTOMapper;
import org.twins.core.service.permission.PermissionService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionUpdateController extends ApiController {

    private final PermissionUpdateRestReverseDTOMapper permissionUpdateRestReverseDTOMapper;
    private final I18nRestDTOReverseMapper i18NRestDTOReverseMapper;
    private final PermissionService permissionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionUpdateV1", summary = "Update permission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission/{permissionId}/v1")
    public ResponseEntity<?> permissionUpdateV1(
            @Parameter(example = DTOExamples.PERMISSION_ID) @PathVariable UUID permissionId,
            @RequestBody PermissionUpdateRqDTOv1 request) {
        Response rs = new Response();
        try {
            I18nEntity nameI18n = i18NRestDTOReverseMapper.convert(request.getNameI18n());
            I18nEntity descriptionI18n = i18NRestDTOReverseMapper.convert(request.getDescriptionI18n());
            permissionService.updatePermission(permissionUpdateRestReverseDTOMapper.convert(request.setId(permissionId)), nameI18n, descriptionI18n);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
