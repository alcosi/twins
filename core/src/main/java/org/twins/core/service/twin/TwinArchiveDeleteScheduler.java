package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinArchiveRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class TwinArchiveDeleteScheduler {

    private final TwinArchiveRepository twinArchiveRepository;

    @Scheduled(cron = "0 0 0 * * *")
    private void deleteTwinArchives() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("twinArchiveDeleteScheduler");
            long size = twinArchiveRepository.count();
            if (size > 0) {
                log.info("Deleting {} twin archive records from database", size);
                twinArchiveRepository.deleteAll();
            } else {
                log.info("No twin archive records to be deleted from database");
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
