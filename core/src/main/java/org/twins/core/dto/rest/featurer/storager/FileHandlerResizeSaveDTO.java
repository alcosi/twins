package org.twins.core.dto.rest.featurer.storager;

import org.twins.core.featurer.storager.filehandler.StoragerFileHandlerController;

import java.util.List;

public record FileHandlerResizeSaveDTO(String id, String fileName, byte[] fileBytes, List<ResizeTaskDTO> tasks,
                                       StoragerFileHandlerController.StorageType storageType, String storageDir, boolean saveOriginal) {}
