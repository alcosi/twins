package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.enums.datalist.DataListStatus;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "data_list_option")
public class DataListOptionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.nameUUIDFromBytes((dataListId + option + optionI18nId + businessAccountId + externalId).getBytes());
        }
    }

    @Column(name = "data_list_id")
    private UUID dataListId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "option")
    private String option;

    @Column(name = "option_i18n_id")
    private UUID optionI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "icon")
    private String icon;

    @Column(name = "data_list_option_status_id")
    @Convert(converter = DataListOptionStatusConverter.class)
    private DataListStatus status;

    @Column(name = "attribute_1_value")
    private String attribute1value;

    @Column(name = "attribute_2_value")
    private String attribute2value;

    @Column(name = "attribute_3_value")
    private String attribute3value;

    @Column(name = "attribute_4_value")
    private String attribute4value;

    @Column(name = "`order`")
    private Short order;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "font_color")
    private String fontColor;

    @Column(name = "custom")
    private boolean custom;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_list_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DataListEntity dataList;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;


    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_i18n_id", insertable = false, updatable = false)
    private I18nEntity optionI18n;

    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "dataListOption", fetch = FetchType.LAZY)
    private Set<DataListSubsetOptionEntity> subsetOptions;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "option_i18n_id", insertable = false, updatable = false)
//    private I18nEntity optionI18n;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "option[" + id + "]";
            case NORMAL -> "option[id:" + id + ", dataListId:" + dataListId + "]";
            default -> "option[id:" + id + ", dataListId:" + dataListId + ", option:" + option + "]";
        };

    }

    public record AttributeAccessor(Function<DataListOptionEntity, String> getter,
                                    BiConsumer<DataListOptionEntity, String> setter) {
    }
}
