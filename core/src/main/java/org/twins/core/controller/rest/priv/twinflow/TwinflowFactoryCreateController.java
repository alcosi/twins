package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryRepository;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryCreateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryCreateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryBaseRestDTOMapperV1;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryCreateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowFactoryService;

import java.util.UUID;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
//@ProtectedBy({Permissions.TWINFLOW_FACTORY_MANAGE, Permissions.TWINFLOW_FACTORY_CREATE})
public class TwinflowFactoryCreateController extends ApiController {

    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TwinflowFactoryCreateRestDTOReverseMapper twinflowFactoryCreateRestDTOReverseMapper;
    private final TwinflowFactoryBaseRestDTOMapperV1 twinflowFactoryBaseRestDTOMapper;
    private final TwinflowFactoryService twinflowFactoryService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowFactoryCreateV1", summary = "TwinflowFactory add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TwinflowFactory data add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowFactoryCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twinflow/{twinflowId}/factory/v1")
    public ResponseEntity<?> twinflowFactoryCreateV1(
            @MapperContextBinding(roots = TwinflowFactoryBaseRestDTOMapperV1.class, response = TwinflowFactoryCreateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinflowFactoryCreateRqDTOv1 request) {
        TwinflowFactoryCreateRsDTOv1 rs = new TwinflowFactoryCreateRsDTOv1();
        try {
            TwinflowFactoryEntity twinflowFactoryEntity = twinflowFactoryCreateRestDTOReverseMapper.convert(request);
            twinflowFactoryEntity = twinflowFactoryService.createTwinflowFactory(twinflowFactoryEntity);

            rs
                    .setTwinflowFactory(twinflowFactoryBaseRestDTOMapper.convert(twinflowFactoryEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
