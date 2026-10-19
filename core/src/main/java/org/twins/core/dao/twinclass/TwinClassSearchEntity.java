package org.twins.core.dao.twinclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_search")
@FieldNameConstants
public class TwinClassSearchEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinClassSearch[" + id + "]";
            case NORMAL:
                return "TwinClassSearch[id:" + id + ", name:" + name + "]";
            case DETAILED:
                return "TwinClassSearch[id:" + id + ", name:" + name + ", domainId:" + domainId + "]";
            default:
                return "TwinClassSearch[" + id + "]";
        }
    }
}
