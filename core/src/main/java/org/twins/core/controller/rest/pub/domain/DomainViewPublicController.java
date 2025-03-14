package org.twins.core.controller.rest.pub.domain;

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
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.domain.DomainViewPublicRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainViewPublicRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.UUID;

@Tag(description = "Get domain public data", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainViewPublicController extends ApiController {
    private final AuthService authService;
    private final DomainService domainService;
    private final DomainViewPublicRestDTOMapper domainViewPublicRestDTOMapper;

    @Operation(operationId = "domainViewPublicV1", summary = "Returns public domain data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public domain details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainViewPublicRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/public/domain/{domainId}/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> domainViewPublicV1(
            @MapperContextBinding(roots = DomainViewPublicRestDTOMapper.class, response = DomainViewPublicRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId) {
        DomainViewPublicRsDTOv1 rs = new DomainViewPublicRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
            rs.setDomain(domainViewPublicRestDTOMapper.convert(
                    domainService.findEntityPublic(domainId), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @Operation(operationId = "domainViewByKeyPublicV1", summary = "Returns public domain data by key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public domain details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainViewPublicRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/public/domain_by_key/{domainKey}/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> domainViewByKeyPublicV1(
            @MapperContextBinding(roots = DomainViewPublicRestDTOMapper.class, response = DomainViewPublicRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DOMAIN_KEY) @PathVariable String domainKey) {
        DomainViewPublicRsDTOv1 rs = new DomainViewPublicRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
            rs
                    .setDomain(domainViewPublicRestDTOMapper.convert(
                            domainService.findEntityPublic(domainKey), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
