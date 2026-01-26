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
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldConditionCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldConditionRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldConditionService;

import java.util.List;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_RULE_MANAGE, Permissions.TWIN_CLASS_FIELD_RULE_UPDATE})
public class TwinClassFieldConditionCreateController extends ApiController {

    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TwinClassFieldConditionCreateRestDTOReverseMapper twinClassFieldConditionCreateRestDTOReverseMapper;
    private final TwinClassFieldConditionRestDTOMapper twinClassFieldConditionRestDTOMapper;
    private final TwinClassFieldConditionService twinClassFieldConditionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldConditionCreateV1", summary = "Create new conditions for twin class rule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conditions successfully created", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldConditionRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field_condition/v1")
    public ResponseEntity<?> twinClassFieldConditionCreateV1(
            @MapperContextBinding(roots = TwinClassFieldConditionRestDTOMapper.class, response = TwinClassFieldConditionRsDTOv1.class) MapperContext mapperContext,
            @RequestBody TwinClassFieldConditionCreateRqDTOv1 request) {
        TwinClassFieldConditionRsDTOv1 rs = new TwinClassFieldConditionRsDTOv1();
        try {
            List<TwinClassFieldConditionEntity> conditionEntities = twinClassFieldConditionService.createConditions(twinClassFieldConditionCreateRestDTOReverseMapper.convertCollection(request.getConditions(), mapperContext));

            rs
                    .setConditions(twinClassFieldConditionRestDTOMapper.convertCollection(conditionEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
