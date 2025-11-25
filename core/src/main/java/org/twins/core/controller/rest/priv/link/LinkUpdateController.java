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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.domain.LinkUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.LinkUpdateDTOv1;
import org.twins.core.dto.rest.link.LinkUpdateRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOV2Mapper;
import org.twins.core.mappers.rest.link.LinkUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;


@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.LINK_MANAGE, Permissions.LINK_UPDATE})
public class LinkUpdateController extends ApiController {
    private final LinkService linkService;
    private final LinkUpdateRestDTOReverseMapper linkUpdateRestDTOReverseMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final LinkForwardRestDTOV2Mapper linkForwardRestDTOV2Mapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "linkUpdateV1", summary = "Update link by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated link", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkUpdateRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/link/{linkId}/v1")
    public ResponseEntity<?> linkUpdateV1(
            @MapperContextBinding(roots = {LinkForwardRestDTOV2Mapper.class}, response = LinkUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.LINK_ID) @PathVariable UUID linkId,
            @RequestBody LinkUpdateDTOv1 request) {
        LinkUpdateRsDTOv1 rs = new LinkUpdateRsDTOv1();
        try {
            I18nEntity forwardNameI18n = i18NSaveRestDTOReverseMapper.convert(request.getForwardNameI18n());
            I18nEntity backwardNameI18n = i18NSaveRestDTOReverseMapper.convert(request.getBackwardNameI18n());
            LinkUpdate linkUpdate = linkUpdateRestDTOReverseMapper.convert(request).setId(linkId);
            LinkEntity linkEntity = linkService.updateLink(linkUpdate, forwardNameI18n, backwardNameI18n);
            rs
                    .setLink(linkForwardRestDTOV2Mapper.convert(linkEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
