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
import org.twins.core.dao.i18n.I18nEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.domain.LinkUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.*;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOV3Mapper;
import org.twins.core.mappers.rest.link.LinkUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkService;

import java.util.UUID;


@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class LinkUpdateController extends ApiController {
    private final LinkService linkService;
    private final LinkUpdateRestDTOReverseMapper linkUpdateRestDTOReverseMapper;
    private final I18nRestDTOReverseMapper i18NRestDTOReverseMapper;
    private final LinkForwardRestDTOV3Mapper linkForwardRestDTOV3Mapper;
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
            @MapperContextBinding(roots = {LinkForwardRestDTOV3Mapper.class}, response = LinkUpdateRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.LINK_ID) @PathVariable UUID linkId,
            @RequestBody LinkUpdateDTOv1 request) {
        LinkUpdateRsDTOv1 rs = new LinkUpdateRsDTOv1();
        try {
            I18nEntity forwardNameI18n = i18NRestDTOReverseMapper.convert(request.getForwardNameI18n());
            I18nEntity backwardNameI18n = i18NRestDTOReverseMapper.convert(request.getBackwardNameI18n());
            LinkUpdate linkUpdate = linkUpdateRestDTOReverseMapper.convert(request).setId(linkId);
            LinkEntity linkEntity = linkService.updateLink(linkUpdate, forwardNameI18n, backwardNameI18n);
            rs
                    .setLink(linkForwardRestDTOV3Mapper.convert(linkEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
