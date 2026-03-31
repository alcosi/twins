package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.datalist.DataListStatus;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionSearchV1")
public class DataListOptionSearchDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "data list id list")
    public Set<UUID> dataListIdList;

    @Schema(description = "data list id exclude list")
    public Set<UUID> dataListIdExcludeList;

    @Schema(description = "data list key list")
    public Set<String> dataListKeyList;

    @Schema(description = "data list key exclude list")
    public Set<String> dataListKeyExcludeList;

    @Schema(description = "option like list")
    public Set<String> optionLikeList;

    @Schema(description = "option not like list")
    public Set<String> optionNotLikeList;

    @Schema(description = "option i18n like list")
    public Set<String> optionI18nLikeList;

    @Schema(description = "option i18n not like list")
    public Set<String> optionI18nNotLikeList;

    @Schema(description = "business account id list")
    public Set<UUID> businessAccountIdList;

    @Schema(description = "business account id exclude list")
    public Set<UUID> businessAccountIdExcludeList;

    @Schema(description = "data list class subset id list")
    public Set<UUID> dataListSubsetIdList;

    @Schema(description = "data list class subset id exclude list")
    public Set<UUID> dataListSubsetIdExcludeList;

    @Schema(description = "data list class subset key list")
    public Set<String> dataListSubsetKeyList;

    @Schema(description = "data list class subset key exclude list")
    public Set<String> dataListSubsetKeyExcludeList;

    @Schema(description = "data list option status id list")
    public Set<DataListStatus> statusIdList;

    @Schema(description = "data list option status id exclude list")
    public Set<DataListStatus> statusIdExcludeList;

    @Deprecated
    @Schema(description = "external id like list")
    public Set<String> externalIdLikeList;

    @Deprecated
    @Schema(description = "external id not like list")
    public Set<String> externalIdNotLikeList;

    @Schema(description = "external id list")
    public Set<String> externalIdList;

    @Schema(description = "external id exclude list")
    public Set<String> externalIdExcludeList;

    @Schema(description = "datalist linked to given twin class fields list")
    public Set<UUID> validForTwinClassFieldIdList;

    @Schema(description = "datalist linked to given twin class fields list")
    public Ternary custom;

    public DataListOptionSearchDTOv1 addIdListItem(UUID item) {
        this.idList = CollectionUtils.safeAdd(this.idList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addIdExcludeListItem(UUID item) {
        this.idExcludeList = CollectionUtils.safeAdd(this.idExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListIdListItem(UUID item) {
        this.dataListIdList = CollectionUtils.safeAdd(this.dataListIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListIdExcludeListItem(UUID item) {
        this.dataListIdExcludeList = CollectionUtils.safeAdd(this.dataListIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListKeyListItem(String item) {
        this.dataListKeyList = CollectionUtils.safeAdd(this.dataListKeyList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListKeyExcludeListItem(String item) {
        this.dataListKeyExcludeList = CollectionUtils.safeAdd(this.dataListKeyExcludeList, item);
        return this;
    }


    public DataListOptionSearchDTOv1 addOptionLikeListItem(String item) {
        this.optionLikeList = CollectionUtils.safeAdd(this.optionLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addOptionNotLikeListItem(String item) {
        this.optionNotLikeList = CollectionUtils.safeAdd(this.optionNotLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addOptionI18nLikeListItem(String item) {
        this.optionI18nLikeList = CollectionUtils.safeAdd(this.optionI18nLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addOptionI18nNotLikeListItem(String item) {
        this.optionI18nNotLikeList = CollectionUtils.safeAdd(this.optionI18nNotLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addBusinessAccountIdListItem(UUID item) {
        this.businessAccountIdList = CollectionUtils.safeAdd(this.businessAccountIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addBusinessAccountIdExcludeListItem(UUID item) {
        this.businessAccountIdExcludeList = CollectionUtils.safeAdd(this.businessAccountIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetIdListItem(UUID item) {
        this.dataListSubsetIdList = CollectionUtils.safeAdd(this.dataListSubsetIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetIdExcludeListItem(UUID item) {
        this.dataListSubsetIdExcludeList = CollectionUtils.safeAdd(this.dataListSubsetIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetKeyListItem(String item) {
        this.dataListSubsetKeyList = CollectionUtils.safeAdd(this.dataListSubsetKeyList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetKeyExcludeListItem(String item) {
        this.dataListSubsetKeyExcludeList = CollectionUtils.safeAdd(this.dataListSubsetKeyExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addStatusIdListItem(DataListStatus item) {
        this.statusIdList = CollectionUtils.safeAdd(this.statusIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addStatusIdExcludeListItem(DataListStatus item) {
        this.statusIdExcludeList = CollectionUtils.safeAdd(this.statusIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addExternalIdLikeListItem(String item) {
        this.externalIdLikeList = CollectionUtils.safeAdd(this.externalIdLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addExternalIdNotLikeListItem(String item) {
        this.externalIdNotLikeList = CollectionUtils.safeAdd(this.externalIdNotLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addLinkedToTwinClassFieldIdList(UUID item) {
        this.validForTwinClassFieldIdList = CollectionUtils.safeAdd(this.validForTwinClassFieldIdList, item);
        return this;
    }
}
