package org.twins.core.controller.rest.priv.validator;

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
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.validator.TwinValidatorSetCreate;
import org.twins.core.dto.rest.validator.TwinValidatorSetCreateRqDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorSetListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.validator.TwinValidatorSetCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;
import java.util.List;

@Tag(name = ApiTag.TWIN_VALIDATOR)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_VALIDATOR_SET_MANAGE, Permissions.TWIN_VALIDATOR_SET_CREATE})
public class TwinValidatorSetCreateController extends ApiController {
    private final TwinValidatorSetService twinValidatorSetService;
    private final TwinValidatorSetCreateRestDTOReverseMapper twinValidatorSetCreateRestDTOReverseMapper;
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinValidatorSetCreateV1", summary = "Twin validator set batch create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin validator set batch create", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinValidatorSetListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_validator_set/v1")
    public ResponseEntity<?> twinValidatorSetCreateV1(
            @MapperContextBinding(roots = TwinValidatorSetRestDTOMapper.class, response = TwinValidatorSetListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinValidatorSetCreateRqDTOv1 request) {
        TwinValidatorSetListRsDTOv1 rs = new TwinValidatorSetListRsDTOv1();
        try {
            List<TwinValidatorSetCreate> createList = twinValidatorSetCreateRestDTOReverseMapper.convertCollection(request.getValidatorSets());
            Collection<TwinValidatorSetEntity> twinValidatorSetEntityList = twinValidatorSetService.createTwinValidatorSet(createList);
            rs
                    .setValidatorSets(twinValidatorSetRestDTOMapper.convertCollection(twinValidatorSetEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
