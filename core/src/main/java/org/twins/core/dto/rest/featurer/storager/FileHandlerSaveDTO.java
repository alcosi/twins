package org.twins.core.dto.rest.featurer.storager;

import static org.twins.core.featurer.storager.filehandler.StoragerFileHandlerController.StorageType;

public record FileHandlerSaveDTO(String id, String fileName, byte[] fileBytes, StorageType storageType, String storageDir) {}
