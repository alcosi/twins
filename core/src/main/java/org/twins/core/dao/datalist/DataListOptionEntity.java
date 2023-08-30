package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "data_list_option")
public class DataListOptionEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "data_list_id")
    private UUID dataListId;

    @Column(name = "option")
    private String option;

    @Column(name = "disabled")
    private boolean disabled;
}
