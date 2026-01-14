package org.twins.core.dao.statistic;

import com.github.f4b6a3.uuid.UuidCreator;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.twin.validator.TwinValidator;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_statistic")
@FieldNameConstants
public class TwinStatisticEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "statister_featurer_id")
    private Integer statisterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "statister_params", columnDefinition = "hstore")
    private HashMap<String, String> statisterParams;

    @Override
    public String easyLog(Level level) {
        return "twinStatistic[" + id + "]";
    }
}
