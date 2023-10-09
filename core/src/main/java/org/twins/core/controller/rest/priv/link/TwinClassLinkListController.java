package org.twins.core.controller.rest.priv.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.LinkListRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassLinkListController extends ApiController {
    final AuthService authService;
    final LinkService linkService;
    final TwinClassService twinClassService;
    final LinkForwardRestDTOMapper linkForwardRestDTOMapper;
    final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;


    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassLinkListV1", summary = "Returns twin class link list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/{twinClassId}/link/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassLinkListV1(
            @Parameter(name = "twinClassId", in = ParameterIn.PATH,  required = true, example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassBaseRestDTOMapper.ClassMode._ID_ONLY) TwinClassBaseRestDTOMapper.ClassMode showClassMode) {
        LinkListRsDTOv1 rs = new LinkListRsDTOv1();
        try {
            MapperProperties mapperProperties = new MapperProperties().setMode(showClassMode);
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(twinClassId);
            rs
                    .forwardLinkList(linkForwardRestDTOMapper.convertList(findTwinClassLinksResult.getForwardLinks(), mapperProperties))
                    .backwardLinkList(linkBackwardRestDTOMapper.convertList(findTwinClassLinksResult.getBackwardLinks(), mapperProperties));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
