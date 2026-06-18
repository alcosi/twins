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
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.LinkViewRsDTOv1;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOV2Mapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.LINK_MANAGE, Permissions.LINK_VIEW})
public class LinkViewController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final LinkForwardRestDTOV2Mapper linkForwardRestDTOV2Mapper;
    private final LinkService linkService;

    @ParametersApiUserHeaders
    @Operation(operationId = "linkViewV1", summary = "Link view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/link/{linkId}/v1")
    public ResponseEntity<?> linkViewV1(
            @MapperContextBinding(roots = LinkForwardRestDTOV2Mapper.class, response = LinkViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.LINK_ID) @PathVariable("linkId") UUID linkId) {
        LinkViewRsDTOv1 rs = new LinkViewRsDTOv1();
        try {
            LinkEntity link = linkService.findEntitySafe(linkId);
            rs
                    .setLink(linkForwardRestDTOV2Mapper.convert(link, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
