package org.twins.core.controller.rest.priv.twinstatus;

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
import org.twins.core.dto.rest.twinstatus.TwinStatusDuplicateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusService;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_STATUS_CREATE})
public class TwinStatusDuplicateController extends ApiController {
    private final TwinStatusService twinStatusService;
    private final TwinStatusDuplicateRestDTOReverseMapper twinStatusDuplicateRestDTOReverseMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusDuplicateV1", summary = "Duplicates twin statuses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/duplicate/v1")
    public ResponseEntity<?> twinStatusDuplicateV1(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinStatusDuplicateRqDTOv1 request) {
        var rs = new TwinStatusListRsDTOv1();

        try {
            var duplicates = twinStatusDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedStatuses = twinStatusService.duplicate(duplicates);
            rs
                    .setStatuses(twinStatusRestDTOMapper.convertCollection(duplicatedStatuses, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
