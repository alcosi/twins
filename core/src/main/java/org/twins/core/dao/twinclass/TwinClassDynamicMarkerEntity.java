package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_dynamic_marker")
@FieldNameConstants
public class TwinClassDynamicMarkerEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Column(name = "marker_data_list_option_id")
    private UUID markerDataListOptionId;

    @ManyToOne
    @JoinColumn(name = "marker_data_list_option_id", insertable = false, updatable = false)
    private DataListOptionEntity markerDataListOption;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @Transient
    private TwinValidatorSetEntity twinValidatorSet;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @Override
    public ContainsTwinValidatorSet setTwinValidatorSet(TwinValidatorSetEntity twinValidatorSet) {
        this.twinValidatorSet = twinValidatorSet;
        return this;
    }

    @Override
    public ContainsTwinValidatorSet setTwinValidatorKit(Kit<TwinValidatorEntity, UUID> twinValidators) {
        this.twinValidatorKit = twinValidators;
        return this;
    }

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassDynamicMarker[" + id + "]";
            case NORMAL -> "twinClassDynamicMarker[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinClassDynamicMarker[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorSetId:" + twinValidatorSetId + ", markerDataListOptionId:" + markerDataListOptionId + "]";
        };
    }
}
