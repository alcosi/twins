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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainListRsDTOv1;
import org.twins.core.dto.rest.domain.DomainUpdateRqDTOv1;
import org.twins.core.mappers.rest.domain.DomainUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainService;

import java.util.List;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUpdateController extends ApiController {
    private final DomainUpdateRestDTOReverseMapper domainUpdateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DomainViewRestDTOMapper domainViewRestDTOMapper;
    private final DomainService domainService;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainUpdateBatchV1", summary = "Batch domain update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "domain was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/domain/v1")
    public ResponseEntity<?> domainUpdateBatchV1(
            @MapperContextBinding(roots = DomainViewRestDTOMapper.class, response = DomainListRsDTOv1.class) MapperContext mapperContext,
            @RequestBody DomainUpdateRqDTOv1 request) {
        DomainListRsDTOv1 rs = new DomainListRsDTOv1();
        try {
            List<DomainEntity> domainList = domainUpdateRestDTOReverseMapper.convertCollection(request.getDomains());
            domainList = domainService.updateDomain(domainList);
            rs
                    .setDomainList(domainViewRestDTOMapper.convertCollection(domainList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
