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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.link.LinkValidatorDuplicateRqDTOv1;
import org.twins.core.dto.rest.link.LinkValidatorListRsDTOv1;
import org.twins.core.mappers.rest.link.LinkValidatorDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.LinkValidatorRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkValidatorDuplicateService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.LINK_CREATE)
public class LinkValidatorDuplicateController extends ApiController {
    private final LinkValidatorDuplicateService linkValidatorDuplicateService;
    private final LinkValidatorRestDTOMapper linkValidatorRestDTOMapper;
    private final LinkValidatorDuplicateRestDTOReverseMapper linkValidatorDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "linkValidatorDuplicateV1", summary = "Duplicates link validators (optionally to another link)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link validators copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkValidatorListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/link_validator/duplicate/v1")
    public ResponseEntity<?> linkValidatorDuplicateV1(
            @MapperContextBinding(roots = LinkValidatorRestDTOMapper.class, response = LinkValidatorListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody LinkValidatorDuplicateRqDTOv1 request) {
        var rs = new LinkValidatorListRsDTOv1();

        try {
            var duplicates = linkValidatorDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedValidators = linkValidatorDuplicateService.duplicate(duplicates);
            rs
                    .setLinkValidators(linkValidatorRestDTOMapper.convertCollection(duplicatedValidators, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
