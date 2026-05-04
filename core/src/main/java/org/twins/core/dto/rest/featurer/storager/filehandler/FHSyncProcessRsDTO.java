package org.twins.core.dto.rest.featurer.storager.filehandler;

import java.util.List;
import java.util.UUID;

public record FHSyncProcessRsDTO(UUID fileId, List<FHPipelineOutputDTO> outputs) {}
