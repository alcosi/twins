package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list_subset_option")
@FieldNameConstants
@IdClass(DataListOptionSubsetId.class)
public class DataListSubsetOptionEntity {
    @Id
    @Column(name = "data_list_subset_id")
    private UUID dataListSubsetId;

    @Column(name = "data_list_option_id")
    private UUID dataListOptionId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_list_subset_id", insertable = false, updatable = false)
    private DataListSubsetEntity dataListSubset;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_list_option_id", insertable = false, updatable = false)
    private DataListOptionEntity dataListOption;
}
