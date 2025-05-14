package org.twins.core.controller.rest.priv.domain;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainUpdateRqDTOv1;
import org.twins.core.dto.rest.domain.DomainViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.permission.Permissions;


@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.DOMAIN_UPDATE)
public class DomainUpdateController extends ApiController {
    private final DomainUpdateRestDTOReverseMapper domainUpdateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DomainViewRestDTOMapper domainViewRestDTOMapper;
    private final DomainService domainService;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainUpdateV1", summary = "Domain update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "domain was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/domain/v1")
    public ResponseEntity<?> domainUpdateV1(
            @MapperContextBinding(roots = DomainViewRestDTOMapper.class, response = DomainViewRsDTOv1.class) MapperContext mapperContext,
            @RequestBody DomainUpdateRqDTOv1 request) {
        DomainViewRsDTOv1 rs = new DomainViewRsDTOv1();
        try {
            DomainEntity domain = domainUpdateRestDTOReverseMapper.convert(request.getDomain());
            domain = domainService.updateDomain(domain);
            rs
                    .setDomain(domainViewRestDTOMapper.convert(domain, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
