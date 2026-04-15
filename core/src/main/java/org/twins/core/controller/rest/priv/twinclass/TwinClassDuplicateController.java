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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.twinclass.TwinClassDuplicateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassService;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_CREATE})
public class TwinClassDuplicateController extends ApiController {
    private final TwinClassService twinClassService;
    private final TwinClassDuplicateRestDTOReverseMapper twinClassDuplicateRestDTOReverseMapper;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassDuplicateV1", summary = "Duplicates twin classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin classes copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/duplicate/v1")
    public ResponseEntity<?> twinClassDuplicateV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassDuplicateRqDTOv1 request) {
        var rs = new TwinClassListRsDTOv1();

        try {
            var duplicates = twinClassDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedClasses = twinClassService.duplicate(duplicates);
            rs
                    .setTwinClassList(twinClassRestDTOMapper.convertCollection(duplicatedClasses, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
