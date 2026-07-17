package org.twins.core.controller.rest.priv.twinpointer;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.domain.twin.TwinPointerUpdate;
import org.twins.core.dto.rest.twinpointer.TwinPointerListRsDTOv1;
import org.twins.core.dto.rest.twinpointer.TwinPointerUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinpointer.TwinPointerRestDTOMapper;
import org.twins.core.mappers.rest.twinpointer.TwinPointerUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinPointerService;

import java.util.List;

@Tag(name = ApiTag.TWIN_POINTER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TWIN_POINTER_UPDATE)
public class TwinPointerUpdateController extends ApiController {
    private final TwinPointerService twinPointerService;
    private final TwinPointerRestDTOMapper twinPointerRestDTOMapper;
    private final TwinPointerUpdateRestDTOReverseMapper twinPointerUpdateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinPointerUpdateV1", summary = "Twin pointer update (batch)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin pointer updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinPointerListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_pointer/v1")
    public ResponseEntity<?> twinPointerUpdateV1(
            @MapperContextBinding(roots = TwinPointerRestDTOMapper.class, response = TwinPointerListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinPointerUpdateRqDTOv1 request) {
        TwinPointerListRsDTOv1 rs = new TwinPointerListRsDTOv1();
        try {
            List<TwinPointerUpdate> updateList = twinPointerUpdateRestDTOReverseMapper.convertCollection(request.getTwinPointers());
            List<TwinPointerEntity> updated = twinPointerService.updateTwinPointers(updateList);
            rs
                    .setTwinPointers(twinPointerRestDTOMapper.convertCollection(updated, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
