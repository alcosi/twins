package org.twins.core.dao.search;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.query.SortDirection;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_search_sort")
public class TwinSearchSortEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "twin_search_id")
    private UUID twinSearchId;

    @Column(name = "`order`")
    @Basic
    private Integer order; // use wrapper to avoid primitive default

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private SortDirection direction;

    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinSearchSort[" + id + "]";
            default:
                return "twinSearchSort[id:" + id + ", twinSearchId:" + twinSearchId + ", twinClassFieldId:" + twinClassFieldId + ", dir:" + direction + "]";
        }
    }
}
