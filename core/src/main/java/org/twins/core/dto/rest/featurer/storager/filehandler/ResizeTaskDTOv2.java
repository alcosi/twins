package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.enums.featurer.storager.Format;

public record ResizeTaskDTOv2(int width, int height, String type, Format outputFormat, boolean keepAspectRatio) {}

