package org.twins.core.service.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.domain.DomainEntity;
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

    final TwinClassService twinClassService;
    final TwinStatusService twinStatusService;
    final TwinClassFieldService twinClassFieldService;
    final LinkService linkService;
    final TwinflowService twinflowService;
    final TwinflowTransitionService twinflowTransitionService;

    public String generateSubstitutionsConfig(ApiUser apiUser, String filename) throws Exception {
        if (ObjectUtils.isEmpty(filename)) filename = apiUser.getDomain().getKey() + ".conf";

        var domain = apiUser.getDomain();
        var twinClasses = twinClassService.findTwinClasses(apiUser, null);
        twinStatusService.loadStatusesForTwinClasses(twinClasses);
        twinClassFieldService.loadTwinClassFields(twinClasses);
        linkService.loadLinksForTwinClasses(twinClasses);
        twinflowService.loadTwinflowsForTwinClasses(twinClasses);
        twinflowTransitionService.loadAllTransitions(twinClasses);

        StringBuilder sb = new StringBuilder();

        sb.append(domain.getId()).append(domainReadable(domain, false)).append("\n");

        for (var item : twinClasses)
            sb.append(item.getId()).append(classReadable(item, false)).append(".id\n");

        for (var twinClass : twinClasses)
            for (var item : twinClass.getTwinStatusKit().getList())
                sb.append(item.getId()).append(classReadable(twinClass, false)).append(statusReadable(item, true)).append(".id\n");

        for (var twinClass : twinClasses)
            for (var item : twinClass.getTwinClassFieldKit().getList())
                sb.append(item.getId()).append(classReadable(twinClass, false)).append(fieldReadable(item, true)).append(".id\n");

        for (var twinClass : twinClasses)
            for (var item : twinClass.getLinksKit().getList())
                sb.append(item.getId()).append(linkReadable(item, false)).append("].id\n");

        for (var twinClass : twinClasses)
            if (null != twinClass.getTwinflowKit() && twinClass.getTwinflowKit().isNotEmpty())
                for (var item : twinClass.getTwinflowKit().getList())
                    sb.append(item.getId()).append(twinflowReadable(item, false)).append("].id\n");

        for (var twinClass : twinClasses)
            if (null != twinClass.getTransitionsKit() && twinClass.getTransitionsKit().isNotEmpty())
                for (var item : twinClass.getTransitionsKit().getList())
                    sb.append(item.getId()).append(transitionReadable(item, false)).append("].id\n");


        try (PrintWriter file = new PrintWriter(PATH_LOGS + filename)) {
            file.println(sb);
        }
        return "tail -f twins-core.log | logSubstitution.sh " + filename;
    }

    private String domainReadable(DomainEntity item, boolean next) {
        return (next ? "." : "=") + "domain[" + item.getKey() + "]";
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
