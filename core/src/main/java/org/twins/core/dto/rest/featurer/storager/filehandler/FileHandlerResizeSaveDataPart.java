package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.enums.featurer.storager.StorageType;

import java.util.List;

public record FileHandlerResizeSaveDataPart(String id, String type, List<ResizeTaskDTO> tasks, StorageType storageType, String storageDir, boolean saveOriginal) {}
