package org.twins.core.controller.rest.priv.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.system.MemoryAllInfoRsDTOv1;
import org.twins.core.mappers.rest.system.MemoryInfoDTOMapper;
import org.twins.core.mappers.rest.system.MemoryPoolInfoDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.system.MemoryService;

@Tag(description = "Memory information", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@ProtectedBy(Permissions.SYSTEM_APP_INFO_VIEW)
public class MemoryInfoController extends ApiController {
    private final MemoryService memoryService;
    private final MemoryInfoDTOMapper memoryInfoDTOMapper;
    private final MemoryPoolInfoDTOMapper memoryPoolInfoDTOMapper;


    @ParametersApiUserHeaders
    @Operation(operationId = "memoryAllInfoV1", summary = "Returns all memory information")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All memory information retrieved successfully", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MemoryAllInfoRsDTOv1.class))}), @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/system/memory/all/info")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> getAllMemoryInfo() {
        MemoryAllInfoRsDTOv1 rs = new MemoryAllInfoRsDTOv1();
        try {
            rs
                    .setMemoryInfo(memoryInfoDTOMapper.convertCollection(memoryService.getMemoryInfo()))
                    .setMemoryPoolInfo(memoryPoolInfoDTOMapper.convertCollection(memoryService.getMemoryPoolInfo()));
            return new ResponseEntity<>(rs, HttpStatus.OK);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
    }
}
