package org.twins.core.controller.rest.priv.domain;

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
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventRsDTOv1;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventUpdateRqDTOv1;
import org.twins.core.mappers.rest.domain.DomainSubscriptionEventBaseRestDTOMapper;
import org.twins.core.mappers.rest.domain.DomainSubscriptionEventUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainSubscriptionEventService;

import java.util.UUID;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
//@ProtectedBy({Permissions.DOMAIN_SUBSCRIPTION_EVENT_MANAGE, Permissions.DOMAIN_SUBSCRIPTION_EVENT_UPDATE})
public class DomainSubscriptionEventUpdateController extends ApiController {

    private final DomainSubscriptionEventService domainSubscriptionEventService;
    private final DomainSubscriptionEventUpdateRestDTOReverseMapper domainSubscriptionEventUpdateRestDTOReverseMapper;
    private final DomainSubscriptionEventBaseRestDTOMapper domainSubscriptionEventBaseRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainSubscriptionEventUpdateV1", summary = "Domain subscription event update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DomainSubscriptionEvent was updated successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DomainSubscriptionEventRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/domain/subscription_event/{domainSubscriptionEventId}/v1")
    public ResponseEntity<?> domainSubscriptionEventUpdateV1(
            @MapperContextBinding(roots = DomainSubscriptionEventBaseRestDTOMapper.class, response = DomainSubscriptionEventRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainSubscriptionEventId,
            @RequestBody DomainSubscriptionEventUpdateRqDTOv1 request
    ) {
        DomainSubscriptionEventRsDTOv1 rs = new DomainSubscriptionEventRsDTOv1();

        try {
            DomainSubscriptionEventEntity updatedEntity = domainSubscriptionEventUpdateRestDTOReverseMapper.convert(request).setId(domainSubscriptionEventId);
            updatedEntity = domainSubscriptionEventService.updateDomainSubscriptionEvent(updatedEntity);

            rs
                    .setDomainSubscriptionEvent(domainSubscriptionEventBaseRestDTOMapper.convert(updatedEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}