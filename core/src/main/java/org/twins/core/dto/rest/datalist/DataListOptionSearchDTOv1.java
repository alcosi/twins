package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "DataListOptionSearchV1")
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

    @Schema(description = "external id like list")
    public Set<String> externalIdLikeList;

    @Schema(description = "external id not like list")
    public Set<String> externalIdNotLikeList;

    public DataListOptionSearchDTOv1 addIdListItem(UUID item) {
        CollectionUtils.safeAdd(idList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(idExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListIdListItem(UUID item) {
        CollectionUtils.safeAdd(dataListIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(dataListIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListKeyListItem(String item) {
        CollectionUtils.safeAdd(dataListKeyList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListKeyExcludeListItem(String item) {
        CollectionUtils.safeAdd(dataListKeyExcludeList, item);
        return this;
    }


    public DataListOptionSearchDTOv1 addOptionLikeListItem(String item) {
        CollectionUtils.safeAdd(optionLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addOptionNotLikeListItem(String item) {
        CollectionUtils.safeAdd(optionNotLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addOptionI18nLikeListItem(String item) {
        CollectionUtils.safeAdd(optionI18nLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addOptionI18nNotLikeListItem(String item) {
        CollectionUtils.safeAdd(optionI18nNotLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addBusinessAccountIdListItem(UUID item) {
        CollectionUtils.safeAdd(businessAccountIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addBusinessAccountIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(businessAccountIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetIdListItem(UUID item) {
        CollectionUtils.safeAdd(dataListSubsetIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(dataListSubsetIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetKeyListItem(String item) {
        CollectionUtils.safeAdd(dataListSubsetKeyList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addDataListSubsetKeyExcludeListItem(String item) {
        CollectionUtils.safeAdd(dataListSubsetKeyExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addStatusIdListItem(DataListStatus item) {
        CollectionUtils.safeAdd(statusIdList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addStatusIdExcludeListItem(DataListStatus item) {
        CollectionUtils.safeAdd(statusIdExcludeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addExternalIdLikeListItem(String item) {
        CollectionUtils.safeAdd(externalIdLikeList, item);
        return this;
    }

    public DataListOptionSearchDTOv1 addExternalIdNotLikeListItem(String item) {
        CollectionUtils.safeAdd(externalIdNotLikeList, item);
        return this;
    }

}
