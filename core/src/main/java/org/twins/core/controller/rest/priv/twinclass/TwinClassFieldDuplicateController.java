package org.twins.core.controller.rest.priv.twinclass;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDuplicateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_MANAGE, Permissions.TWIN_CLASS_FIELD_VIEW})
public class TwinClassFieldDuplicateController extends ApiController {

    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldDuplicateV1", summary = "Duplicates twin class field by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field/{twinClassFieldId}/duplicate/v1")
    public ResponseEntity<?> twinClassFieldDuplicateV1(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @PathVariable UUID twinClassFieldId,
            @RequestBody TwinClassFieldDuplicateRqDTOv1 rq
    ) {
        var rs = new TwinClassFieldRsDTOv1();

        try {
            rs
                    .setField(twinClassFieldRestDTOMapper.convert(twinClassFieldService.duplicateField(twinClassFieldId, rq.newKey), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
