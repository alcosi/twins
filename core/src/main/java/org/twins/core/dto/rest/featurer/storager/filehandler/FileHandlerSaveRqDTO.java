package org.twins.core.dto.rest.featurer.storager.filehandler;

import static org.twins.core.featurer.storager.filehandler.StoragerFileHandlerController.StorageType;

public record FileHandlerSaveRqDTO(String id, String fileName, String type,
                                   byte[] fileBytes, StorageType storageType, String storageDir) {}
