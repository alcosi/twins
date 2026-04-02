package org.twins.core.dto.rest.featurer.storager.filehandler;

public record ResizeTaskDTOv2(int width, int height, String type, String outputFormat, boolean keepAspectRatio) {}

