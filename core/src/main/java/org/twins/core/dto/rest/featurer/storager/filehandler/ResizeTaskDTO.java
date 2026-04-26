package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.enums.featurer.storager.Format;

public record ResizeTaskDTO(int width, int height, String type, Format outputFormat, boolean keepAspectRatio) {}
