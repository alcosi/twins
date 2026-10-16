package org.twins.core.controller.rest.priv.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.link.LinkValidatorUpdate;
import org.twins.core.dto.rest.link.LinkValidatorListRsDTOv1;
import org.twins.core.dto.rest.link.LinkValidatorUpdateRqDTOv1;
import org.twins.core.mappers.rest.link.LinkValidatorRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkValidatorUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkValidatorService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.LINK_UPDATE)
public class LinkValidatorUpdateController extends ApiController {
    private final LinkValidatorService linkValidatorService;
    private final LinkValidatorUpdateRestDTOReverseMapper linkValidatorUpdateRestDTOReverseMapper;
    private final LinkValidatorRestDTOMapper linkValidatorRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "linkValidatorUpdateV1", summary = "Link validator batch update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link validator batch update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkValidatorListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/link_validator/v1")
    public ResponseEntity<?> linkValidatorUpdateV1(
            @MapperContextBinding(roots = LinkValidatorRestDTOMapper.class, response = LinkValidatorListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid LinkValidatorUpdateRqDTOv1 request) {
        LinkValidatorListRsDTOv1 rs = new LinkValidatorListRsDTOv1();
        try {
            List<LinkValidatorUpdate> updateList = linkValidatorUpdateRestDTOReverseMapper.convertCollection(request.getLinkValidators());
            var entities = linkValidatorService.updateLinkValidators(updateList);
            rs
                    .setLinkValidators(linkValidatorRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
