package org.twins.core.controller.rest.priv.link;

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
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.TwinLinkViewRsDTOv1;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_LINK_MANAGE, Permissions.TWIN_LINK_VIEW})
public class TwinLinkViewController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinLinkRestDTOMapper twinLinkRestDTOMapper;
    private final TwinLinkService twinLinkService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinLinkViewV1", summary = "Twin link view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin link data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinLinkViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_link/{twinLinkId}/v1")
    public ResponseEntity<?> twinLinkViewV1(
            @MapperContextBinding(roots = TwinLinkRestDTOMapper.class, response = TwinLinkViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.UUID_ID) @PathVariable("twinLinkId") UUID twinLinkId) {
        TwinLinkViewRsDTOv1 rs = new TwinLinkViewRsDTOv1();
        try {
            TwinLinkEntity twinLink = twinLinkService.findEntitySafe(twinLinkId);
            rs
                    .setTwinLink(twinLinkRestDTOMapper.convert(twinLink, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
