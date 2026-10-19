package org.twins.core.controller.rest.priv.factory;

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
import org.twins.core.dto.rest.factory.FactoryPipelineStepExportSqlRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.factory.FactoryPipelineStepExportService;

import java.nio.charset.StandardCharsets;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MANAGE})
public class FactoryPipelineStepExportSqlController extends ApiController {
    private final FactoryPipelineStepExportService pipelineStepExportService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineStepExportSqlV1", summary = "Exports factory pipeline steps as SQL INSERT statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SQL file"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_pipeline_step/export/sql/v1", produces = "text/sql;charset=UTF-8")
    public ResponseEntity<byte[]> factoryPipelineStepExportSqlV1(
            @RequestBody FactoryPipelineStepExportSqlRqDTOv1 request) throws ServiceException {
        String sql = pipelineStepExportService.exportToSql(request.getTwinFactoryPipelineStepIds());

        String filename = "factory_pipeline_steps_" + System.currentTimeMillis() + ".sql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(sql.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }
}
