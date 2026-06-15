package org.twins.core.controller.rest.priv.transition;

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
import org.twins.core.dto.rest.transition.TransitionExportSqlRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TransitionExportService;

import java.nio.charset.StandardCharsets;

@Tag(name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequiredArgsConstructor
@ProtectedBy({Permissions.TRANSITION_MANAGE})
public class TransitionExportSqlController extends ApiController {
    private final TransitionExportService transitionExportService;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionExportSqlV1", summary = "Exports twinflow transitions as SQL INSERT statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SQL file"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition/export/sql/v1", produces = "text/sql;charset=UTF-8")
    public ResponseEntity<byte[]> transitionExportSqlV1(
            @RequestBody TransitionExportSqlRqDTOv1 request) throws ServiceException {
        String sql = transitionExportService.exportToSql(
                request.getTransitionIds(),
                request.isIncludeFactory(),
                request.isIncludeStatuses(),
                request.isIncludePermission(),
                request.isIncludeTriggers(),
                request.isIncludeValidatorRules()
        );

        String filename = "transitions_" + System.currentTimeMillis() + ".sql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(sql.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }
}
