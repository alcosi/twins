package org.twins.core.controller.rest.priv.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.link.LinkValidatorExportSqlRqDTOv1;
import org.twins.core.service.link.LinkValidatorExportService;
import org.twins.core.service.permission.Permissions;

import java.nio.charset.StandardCharsets;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.LINK_MANAGE)
public class LinkValidatorExportSqlController extends ApiController {
    private final LinkValidatorExportService linkValidatorExportService;

    @ParametersApiUserHeaders
    @Operation(operationId = "linkValidatorExportSqlV1", summary = "Exports link validators as SQL INSERT statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SQL file"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/link_validator/export/sql/v1", produces = "text/sql;charset=UTF-8")
    public ResponseEntity<byte[]> linkValidatorExportSqlV1(
            @RequestBody LinkValidatorExportSqlRqDTOv1 request) throws ServiceException {
        String sql = linkValidatorExportService.exportToSql(request.getLinkValidatorIds());

        String filename = "link_validators_" + System.currentTimeMillis() + ".sql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(sql.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }
}
