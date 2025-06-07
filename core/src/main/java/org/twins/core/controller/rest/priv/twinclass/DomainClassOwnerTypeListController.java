package org.twins.core.controller.rest.priv.twinclass;

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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twinclass.TwinClassOwnerTypeEntity;
import org.twins.core.dto.rest.twinclass.DomainClassOwnerTypeListRsDTOv1;
import org.twins.core.mappers.rest.twinclass.TwinClassOwnerTypeRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Set;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_MANAGE, Permissions.DOMAIN_VIEW})
public class DomainClassOwnerTypeListController extends ApiController {

    private final TwinClassService twinClassService;
    private final TwinClassOwnerTypeRestDTOMapper twinClassOwnerTypeRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassOwnerTypeListV1", summary = "Returns the owner type of the domain class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainClassOwnerTypeListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/domain/class_owner_type/list/v1")
    public ResponseEntity<?> twinClassOwnerTypeListV1() {
        DomainClassOwnerTypeListRsDTOv1 rs = new DomainClassOwnerTypeListRsDTOv1();
        try {
            Set<TwinClassOwnerTypeEntity> twinClassOwnerType = twinClassService.findTwinClassOwnerType();
            rs.setTwinClassOwnerTypes(twinClassOwnerTypeRestDTOMapper.convertCollection(twinClassOwnerType));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
