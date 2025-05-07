package org.twins.face.controller.rest.widget;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiTag;

@Tag(description = "Get WT0002 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT002Controller {
}
