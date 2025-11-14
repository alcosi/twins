package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.featurer.storager.filehandler.StoragerFileHandler;

import java.util.List;

public record FileHandlerResizeSaveRqDTO(String id, String fileName, String type, byte[] fileBytes, List<ResizeTaskDTO> tasks,
                                         StoragerFileHandler.StorageType storageType, String storageDir, boolean saveOriginal) {}
