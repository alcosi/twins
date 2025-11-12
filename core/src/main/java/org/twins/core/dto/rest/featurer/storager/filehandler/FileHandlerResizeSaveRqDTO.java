package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.featurer.storager.filehandler.StoragerFileHandlerController;

import java.util.List;

public record FileHandlerResizeSaveRqDTO(String id, String fileName, String type, byte[] fileBytes, List<ResizeTaskDTO> tasks,
                                         StoragerFileHandlerController.StorageType storageType, String storageDir, boolean saveOriginal) {}
