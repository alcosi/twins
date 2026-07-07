package org.twins.core.controller.rest.priv.twinstatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.twins.core.dto.rest.twinstatus.TwinStatusExportSqlRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusExportService;
import org.twins.core.service.twin.TwinStatusService;

import java.nio.charset.StandardCharsets;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_STATUS_MANAGE})
public class TwinStatusExportSqlController extends ApiController {
    private final TwinStatusExportService twinStatusExportService;
    private final TwinStatusService twinStatusService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusExportSqlV1", summary = "Exports twin statuses as SQL INSERT statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SQL file"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/export/sql/v1", produces = "text/sql;charset=UTF-8")
    public ResponseEntity<byte[]> twinStatusExportSqlV1(
            @RequestBody TwinStatusExportSqlRqDTOv1 request) throws ServiceException {
        var statuses = twinStatusService.findEntitiesSafe(request.getStatusIds());
        String sql = twinStatusExportService.exportToSql(statuses.getCollection());
        String filename = "twin_statuses_" + System.currentTimeMillis() + ".sql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(sql.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }
}
