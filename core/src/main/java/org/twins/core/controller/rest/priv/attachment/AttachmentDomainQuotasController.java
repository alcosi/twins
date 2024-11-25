package org.twins.core.controller.rest.priv.attachment;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.attachment.AttachmentQuotasRsDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentViewRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentQuotasRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.domain.TierService;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AttachmentDomainQuotasController extends ApiController {

    private final AttachmentQuotasRestDTOMapper attachmentQuotasRestDTOMapper;

    private final TierService tierService;
    private final DomainService domainService;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentDomainQuotasV1", summary = "Get info about storage quotas(count files/disk space usage) for Domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment quotas data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/attachment/quotas/domain/v1")
    public ResponseEntity<?> attachmentDomainQuotasV1(
            @MapperContextBinding(roots = AttachmentQuotasRestDTOMapper.class, response = AttachmentQuotasRsDTOv1.class) MapperContext mapperContext) {
        AttachmentQuotasRsDTOv1 rs = new AttachmentQuotasRsDTOv1();
        try {
            rs.setQuotas(
                    attachmentQuotasRestDTOMapper.convert(
                            domainService.getDomainQuotas(), mapperContext
                    ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
