package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.cambium.i18n.dao.I18nEntity;

import java.util.Arrays;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list_option")
public class DataListOptionEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "data_list_id")
    private UUID dataListId;

    @Column(name = "option")
    private String option;

    @Column(name = "option_i18n_id")
    private UUID optionI18NId;

    @Column(name = "icon")
    private String icon;

    @Column(name = "disabled")
    private boolean disabled;

    @Column(name = "data_list_option_status_id")
    @Convert(converter = DataListOptionStatusConverter.class)
    private Status status;

    @Column(name = "attribute_1_value")
    private String attribute1value;

    @Column(name = "attribute_2_value")
    private String attribute2value;

    @Column(name = "attribute_3_value")
    private String attribute3value;

    @Column(name = "attribute_4_value")
    private String attribute4value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_list_id", insertable = false, updatable = false)
    private DataListEntity dataList;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "option_i18n_id", insertable = false, updatable = false)
//    private I18nEntity optionI18n;

    @Getter
    public enum Status {
        active("active"),
        disabled("disabled"),
        hidden("hidden");

        private final String id;

        Status(String id) {
            this.id = id;
        }

        public static Status valueOd(String type) {
            return Arrays.stream(Status.values()).filter(t -> t.id.equals(type)).findAny().orElse(active);
        }
    }

}
