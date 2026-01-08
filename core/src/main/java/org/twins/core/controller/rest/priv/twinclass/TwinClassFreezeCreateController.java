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
import org.springframework.http.MediaType;
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
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeCreateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFreezeCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFreezeDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFreezeService;

import java.util.List;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_CREATE})
public class TwinClassFreezeCreateController extends ApiController {
    private final TwinClassFreezeDTOMapper twinClassFreezeDTOMapper;
    private final TwinClassFreezeCreateRestDTOReverseMapper twinClassFreezeCreateRestDTOReverseMapper;
    private final TwinClassFreezeService twinClassFreezeService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFreezeCreateV1", summary = "Create new twin class freezes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class freezes data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = TwinClassFreezeRsDTOv1.class)),}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_freeze/v1", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinClassFreezeCreateV1(
            @MapperContextBinding(roots = TwinClassFreezeDTOMapper.class, response = TwinClassFreezeRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassFreezeCreateRqDTOv1 request) {
        TwinClassFreezeRsDTOv1 rs = new TwinClassFreezeRsDTOv1();
        try {
            List<TwinClassFreezeEntity> twinClassFreezeEntityList = twinClassFreezeService.createTwinClassFreezeList(twinClassFreezeCreateRestDTOReverseMapper.convertCollection(request.getTwinClassFreezes()));
            rs
                    .setTwinClassFreezes(twinClassFreezeDTOMapper.convertCollection(twinClassFreezeEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
