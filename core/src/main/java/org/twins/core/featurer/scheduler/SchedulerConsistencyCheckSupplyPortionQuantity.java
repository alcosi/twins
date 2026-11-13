package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5017,
        name = "SchedulerConsistencyCheckSupplyPortionQuantity",
        description = "Scheduler to check supply portion quantity conservation (issued = used + defective + remaining) and absence of negative quantity values"
)
public class SchedulerConsistencyCheckSupplyPortionQuantity extends SchedulerConsistencyCheck {
    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    // Overridden to split the violation kinds (negative values / used on non-task flavor / conservation mismatch) in the scheduler log result
    @Override
    @Transactional(readOnly = true)
    protected String processTask(Properties properties) {
        try {
            LoggerUtils.logController(getLogSource());
            long negativeQuantityValues = twinFieldDecimalRepository.countSupplyPortionNegativeQuantity();
            long usedOnNonTaskFlavor = twinFieldDecimalRepository.countSupplyPortionUsedQuantityOnNonTaskFlavor();
            long conservationViolations = twinFieldDecimalRepository.countSupplyPortionQuantityConservationMismatch();
            long total = negativeQuantityValues + usedOnNonTaskFlavor + conservationViolations;

            if (total == 0) {
                log.info("No invalid [{}] records detected", consistencyCheckName());
                return "0 invalid [" + consistencyCheckName() + "] rows were detected";
            } else {
                // Alert carries the full breakdown, not just the total: this scheduler runs in log_enabled=false
                // mode (temporary, every-minute cadence), so twins-alert.log is the only reporting channel.
                LoggerUtils.alertLog.error("Supply portion quantity violations detected: {} negative quantity values, "
                                + "{} twins with used quantity on non-task flavor, "
                                + "{} twins with issued quantity not equal to used + defective + remaining. "
                                + "Details: scripts/sql/supply_portion_quantity_check.sql",
                        negativeQuantityValues, usedOnNonTaskFlavor, conservationViolations);
                return total + " invalid [" + consistencyCheckName() + "] rows were detected: "
                        + negativeQuantityValues + " negative quantity values, "
                        + usedOnNonTaskFlavor + " twins with used quantity on non-task flavor, "
                        + conservationViolations + " twins with issued quantity not equal to used + defective + remaining";
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
            return "Processing tasks failed with exception: " + e;
        } finally {
            LoggerUtils.cleanMDC();
        }
    }

    @Override
    protected long invalidRecordsCount() {
        return twinFieldDecimalRepository.countSupplyPortionNegativeQuantity()
                + twinFieldDecimalRepository.countSupplyPortionUsedQuantityOnNonTaskFlavor()
                + twinFieldDecimalRepository.countSupplyPortionQuantityConservationMismatch();
    }

    @Override
    protected String consistencyCheckName() {
        return "supply_portion_quantity";
    }
}
