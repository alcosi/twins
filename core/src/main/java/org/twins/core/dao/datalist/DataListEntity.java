package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.i18n.I18nEntity;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list")
@FieldNameConstants
public class DataListEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.nameUUIDFromBytes((key + domainId).getBytes());
        }
    }

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "key")
    private String key;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "attribute_1_key")
    private String attribute1key;

    @Column(name = "attribute_1_name_i18n_id")
    private UUID attribute1nameI18nId;

    @Column(name = "attribute_2_key")
    private String attribute2key;

    @Column(name = "attribute_2_name_i18n_id")
    private UUID attribute2nameI18nId;

    @Column(name = "attribute_3_key")
    private String attribute3key;

    @Column(name = "attribute_3_name_i18n_id")
    private UUID attribute3nameI18nId;

    @Column(name = "attribute_4_key")
    private String attribute4key;

    @Column(name = "attribute_4_name_i18n_id")
    private UUID attribute4nameI18nId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "default_data_list_option_id")
    private UUID defaultDataListOptionId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_1_name_i18n_id", insertable = false, updatable = false)
    private I18nEntity attribute1nameI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_2_name_i18n_id", insertable = false, updatable = false)
    private I18nEntity attribute2nameI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_3_name_i18n_id", insertable = false, updatable = false)
    private I18nEntity attribute3nameI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_4_name_i18n_id", insertable = false, updatable = false)
    private I18nEntity attribute4nameI18n;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<DataListOptionEntity, UUID> options;

    //needed for specification
    @Deprecated
    @OneToMany(mappedBy = "dataList", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Collection<DataListOptionEntity> dataListOptions;

    @Transient
    private Map<String, DataListOptionEntity.AttributeAccessor> attributes;

    public String easyLog(Level level) {
        return "dataList[id:" + id + ", key:" + key + "]";
    }
}
