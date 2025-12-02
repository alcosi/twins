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
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRuleCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRuleRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldRuleService;

import java.util.List;

/**
 * REST-controller that allows to create a {@link TwinClassFieldRuleEntity} together with its
 * child {@code twin_class_field_condition} rows.
 * <p>
 * Endpoint is aligned with other *Create* controllers existing in the code-base, therefore the
 * implementation closely follows {@code TwinClassFieldCreateController} and
 * {@code TransitionTriggerCreateController} patterns.
 */
@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_RULE_MANAGE, Permissions.TWIN_CLASS_FIELD_RULE_CREATE})
public class TwinClassFieldRuleCreateController extends ApiController {

    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TwinClassFieldRuleCreateRestDTOReverseMapper twinClassFieldRuleCreateDTOReverseMapper;
    private final TwinClassFieldRuleRestDTOMapper twinClassFieldRuleRestDTOMapper;
    private final TwinClassFieldRuleService twinClassFieldRuleService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldRuleCreateV1", summary = "Create a rule that defines dependent fields behaviour")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule has been successfully created", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldRuleRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field_rule/v1")
    public ResponseEntity<?> twinClassFieldRuleCreateV1(
            @MapperContextBinding(roots = TwinClassFieldRuleRestDTOMapper.class, response = TwinClassFieldRuleRsDTOv1.class) MapperContext mapperContext,
            @RequestBody TwinClassFieldRuleCreateRqDTOv1 request) {
        TwinClassFieldRuleRsDTOv1 rs = new TwinClassFieldRuleRsDTOv1();
        try {
            List<TwinClassFieldRuleEntity> ruleEntities = twinClassFieldRuleService.createRules(twinClassFieldRuleCreateDTOReverseMapper.convertCollection(request.getRules(), mapperContext));

            rs
                    .setRules(twinClassFieldRuleRestDTOMapper.convertCollection(ruleEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
