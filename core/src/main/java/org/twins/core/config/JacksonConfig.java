package org.twins.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.twins.core.dto.rest.history.context.*;
import org.twins.core.dto.rest.transition.TwinTransitionPerformResultDTO;
import org.twins.core.dto.rest.transition.TwinTransitionPerformResultDTOMixIn;
import org.twins.core.dto.rest.transition.TwinTransitionPerformResultMajorDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformResultMinorDTOv1;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.dto.rest.twinclass.*;

@Configuration
public class JacksonConfig implements WebMvcConfigurer {

//  Jackson annotations not friendly to swagger annotations(polymorph case).
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(TwinFieldSearchDTOv1.class, TwinFieldSearchDTOv1MixIn.class);
        mapper.registerSubtypes(
                TwinFieldSearchTextDTOv1.class,
                TwinFieldSearchDateDTOv1.class,
                TwinFieldSearchNumericDTOv1.class,
                TwinFieldSearchListDTOv1.class,
                TwinFieldSearchIdDTOv1.class,
                TwinFieldSearchBooleanDTOv1.class,
                TwinFieldSearchUserDTOv1.class,
                TwinFieldSearchSpaceRoleUserDTOv1.class
        );
        mapper.addMixIn(TwinClassFieldDescriptorDTO.class, TwinClassFieldDescriptorDTOMixIn.class);
        mapper.registerSubtypes(
                TwinClassFieldDescriptorTextDTOv1.class,
                TwinClassFieldDescriptorSecretDTOv1.class,
                TwinClassFieldDescriptorDateScrollDTOv1.class,
                TwinClassFieldDescriptorColorHexDTOv1.class,
                TwinClassFieldDescriptorUrlDTOv1.class,
                TwinClassFieldDescriptorListDTOv1.class,
                TwinClassFieldDescriptorListLongDTOv1.class,
                TwinClassFieldDescriptorListSharedInHeadDTOv1.class,
                TwinClassFieldDescriptorLinkDTOv1.class,
                TwinClassFieldDescriptorLinkLongDTOv1.class,
                TwinClassFieldDescriptorI18nDTOv1.class,
                TwinClassFieldDescriptorUserDTOv1.class,
                TwinClassFieldDescriptorUserLongDTOv1.class,
                TwinClassFieldDescriptorAttachmentDTOv1.class,
                TwinClassFieldDescriptorNumericDTOv1.class,
                TwinClassFieldDescriptorImmutableDTOv1.class,
                TwinClassFieldDescriptorBooleanDTOv1.class
        );
        mapper.addMixIn(TwinTransitionPerformResultDTO.class, TwinTransitionPerformResultDTOMixIn.class);
        mapper.registerSubtypes(
                TwinTransitionPerformResultMinorDTOv1.class,
                TwinTransitionPerformResultMajorDTOv1.class
        );
        mapper.addMixIn(HistoryContextDTO.class, HistoryContextDTOMixIn.class);
        mapper.registerSubtypes(
                HistoryContextUserDTOv1.class,
                HistoryContextUserMultiDTOv1.class,
                HistoryContextStatusDTOv1.class,
                HistoryContextTwinDTOv1.class,
                HistoryContextTwinMultiDTOv1.class,
                HistoryContextAttachmentDTOv1.class,
                HistoryContextAttachmentUpdateDTOv1.class,
                HistoryContextLinkDTOv1.class,
                HistoryContextLinkUpdateDTOv1.class,
                HistoryContextListDTOv1.class,
                HistoryContextListMultiDTOv1.class
        );
        return mapper;
    }
}
