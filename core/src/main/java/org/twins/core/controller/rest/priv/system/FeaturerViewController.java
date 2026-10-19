package org.twins.core.controller.rest.priv.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
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
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerViewRsDTOv1;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FEATURER_MANAGE, Permissions.FEATURER_VIEW})
public class FeaturerViewController extends ApiController {
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FeaturerService featurerService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "featurerViewV1", summary = "Featurer view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featurer data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FeaturerViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/featurer/{featurerId}/v1")
    public ResponseEntity<?> featurerViewV1(
            @MapperContextBinding(roots = FeaturerRestDTOMapper.class, response = FeaturerViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FEATURER_ID) @PathVariable Integer featurerId) {
        FeaturerViewRsDTOv1 rs = new FeaturerViewRsDTOv1();
        try {
            FeaturerEntity featurerEntity = featurerService.findEntitySafe(featurerId);
            rs
                    .setFeaturer(featurerRestDTOMapper.convert(featurerEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
