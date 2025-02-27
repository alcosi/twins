package org.twins.core.controller.rest.priv.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.twins.core.i18n.dao.I18nEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkCreateDTOv1;
import org.twins.core.dto.rest.link.LinkCreateRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.LinkCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOV3Mapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkService;


@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class LinkCreateController extends ApiController {
    private final LinkService linkService;
    private final LinkCreateRestDTOReverseMapper linkCreateRestDTOReverseMapper;
    private final I18nRestDTOReverseMapper i18NRestDTOReverseMapper;
    private final LinkForwardRestDTOV3Mapper linkForwardRestDTOV3Mapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "linkCreateV1", summary = "Create new link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created link", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkCreateRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/link/v1")
    public ResponseEntity<?> linkCreateV1(
            @MapperContextBinding(roots = {LinkForwardRestDTOV3Mapper.class}, response = LinkCreateRsDTOv1.class) MapperContext mapperContext,
            @RequestBody LinkCreateDTOv1 request) {
        LinkCreateRsDTOv1 rs = new LinkCreateRsDTOv1();
        try {
            I18nEntity forwardNameI18n = i18NRestDTOReverseMapper.convert(request.getForwardNameI18n());
            I18nEntity backwardNameI18n = i18NRestDTOReverseMapper.convert(request.getBackwardNameI18n());
            LinkEntity linkEntity = linkCreateRestDTOReverseMapper.convert(request);
            linkEntity = linkService.createLink(linkEntity, forwardNameI18n, backwardNameI18n);
            rs
                    .setLink(linkForwardRestDTOV3Mapper.convert(linkService.findEntitySafe(linkEntity.getId()), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
