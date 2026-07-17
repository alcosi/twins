package org.twins.core.controller.rest.priv.twinpointer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinpointer.TwinPointerViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinpointer.TwinPointerRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinPointerService;

import java.util.UUID;

@Tag(name = ApiTag.TWIN_POINTER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_POINTER_MANAGE, Permissions.TWIN_POINTER_VIEW})
public class TwinPointerViewController extends ApiController {
    private final TwinPointerService twinPointerService;
    private final TwinPointerRestDTOMapper twinPointerRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinPointerViewV1", summary = "Twin pointer by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin pointer data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinPointerViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_pointer/{twinPointerId}/v1")
    public ResponseEntity<?> twinPointerViewV1(
            @MapperContextBinding(roots = TwinPointerRestDTOMapper.class, response = TwinPointerViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.UUID_ID) @PathVariable UUID twinPointerId) {
        TwinPointerViewRsDTOv1 rs = new TwinPointerViewRsDTOv1();
        try {
            TwinPointerEntity entity = twinPointerService.findEntitySafe(twinPointerId);
            rs
                    .setTwinPointer(twinPointerRestDTOMapper.convert(entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
