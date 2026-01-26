package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.enums.featurer.storager.StorageType;

import java.util.List;

public record FileHandlerDeleteRqDTO(List<String> dirs, StorageType storageType) {}
