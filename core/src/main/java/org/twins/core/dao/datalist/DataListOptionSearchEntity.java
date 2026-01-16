package org.twins.core.dao.datalist;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list_option_search")
@FieldNameConstants
public class DataListOptionSearchEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "force_sorting")
    private Boolean forceSorting;

    @Column(name = "field_sorter_featurer_id")
    private Integer optionSorterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_sorter_params", columnDefinition = "hstore")
    private HashMap<String, String> optionSorterParams;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "dataListOptionSearchEntity[" + id + "]";
            case NORMAL:
                return "dataListOptionSearchEntity[id" + id + ", name:" + name + "]";
            default:
                return "dataListOptionSearchEntity[id" + id + ", name:" + name + ", featurerId:" + optionSorterFeaturerId + ", forseSotring:" + forceSorting + "]";
        }
    }
}
