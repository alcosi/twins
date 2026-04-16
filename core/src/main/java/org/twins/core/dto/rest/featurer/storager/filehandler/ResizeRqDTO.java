package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.enums.featurer.storager.StorageType;

import java.util.List;
import java.util.UUID;

public record ResizeRqDTO(UUID fileId, List<ResizeTaskDTOv2> resizeTasks, StorageType storageType, String storageDir) {}
