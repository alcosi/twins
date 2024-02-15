package org.twins.core.domain.system.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.io.PrintWriter;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class LogSupportService {

    public static String PATH_LOGS = "./logs/";
    public static String PATH_SCRIPS = "./scripts/";
    public static String PATH_SCRIPS_UUID_SUBSTITUTIONS = PATH_SCRIPS + "log-uuid-substitutions/";


    final TwinClassService twinClassService;
    final TwinStatusService twinStatusService;
    final TwinClassFieldService twinClassFieldService;
    final LinkService linkService;
    final TwinflowService twinflowService;
    final TwinflowTransitionService twinflowTransitionService;

    public String generateSubstitutionsConfig(ApiUser apiUser, String filename) throws Exception {
        if(ObjectUtils.isEmpty(filename)) filename = apiUser.getDomain().getKey() + ".conf";

        var twinClasses = twinClassService.findTwinClasses(apiUser, null);
        var statusTwinClasses = twinStatusService.findByTwinClasses(twinClasses);
        var twinClassFieldEntities = twinClassFieldService.loadTwinClassFields(twinClasses);
        var linksSet = linkService.findLinksSet(twinClasses);
        var twinflowEntities = twinflowService.loadTwinflow(twinClasses);
        var transitionEntities = twinflowTransitionService.getTransitionsByTwinflows(twinflowEntities);

        StringBuilder sb = new StringBuilder();
        for (var item : twinClasses)
            sb.append(item.getId()).append(classReadable(item, false)).append(".id\n");

        for(var entry : statusTwinClasses.entrySet())
            for (var item : entry.getValue()) sb.append(item.getId()).append(classReadable(entry.getKey(), false)).append(statusReadable(item, true)).append(".id\n");

        for (var item : twinClassFieldEntities)
            sb.append(item.getId()).append(classReadable(item.getTwinClass(), false)).append(fieldReadable(item, true)).append(".id\n");


        for (var item : linksSet)
            sb.append(item.getId()).append(linkReadable(item, false)).append("].id\n");


        for (var item : twinflowEntities)
            sb.append(item.getId()).append(twinflowReadable(item, false)).append("].id\n");

        for (var item : transitionEntities)
            sb.append(item.getId()).append(transitionReadable(item, false)).append("].id\n");



        try (PrintWriter file = new PrintWriter(PATH_SCRIPS_UUID_SUBSTITUTIONS + filename)) {
            file.println(sb);
        }
        return "make cfg=" + filename;
    }

    private String classReadable(TwinClassEntity item, boolean next) {
        return (next ? "." : "=") + "class[" + item.getKey() + "]";
    }

    private String statusReadable(TwinStatusEntity item, boolean next) {
        return (next ? "." : "=") + "status[" + item.getKey() + "]";
    }

    private String fieldReadable(TwinClassFieldEntity item, boolean next) {
        return (next ? "." : "=") + "field[" + item.getKey() + "]";
    }

    private String linkReadable(LinkEntity item, boolean next) {
        return (next ? "." : "=") + "link_class[" + item.getSrcTwinClass().getKey() + " -> " + item.getDstTwinClass().getKey() + "]";
    }

    private String twinflowReadable(TwinflowEntity item, boolean next) {
        return (next ? "." : "=") + "twinflow[" + item.getName() + "]";
    }

    private String transitionReadable(TwinflowTransitionEntity item, boolean next) {
        return (next ? "." : "=") + "transition[" + item.getTwinflow().getTwinClass().getKey() + "][" + item.getSrcTwinStatus().getKey() + " -> " + item.getDstTwinStatus().getKey() + "]";
    }
}
