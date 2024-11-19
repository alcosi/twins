package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.UserSnapshot;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.service.history.HistoryMutableDataCollector;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextUserChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.userChange";
    private UserSnapshot fromUser;
    private UserSnapshot toUser;
    public static final String PLACEHOLDER_FROM_USER = "fromUser";
    public static final String PLACEHOLDER_TO_USER = "toUser";

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextUserChange shotFromUser(UserEntity userEntity) {
        fromUser = UserSnapshot.convertEntity(userEntity);
        return this;
    }

    public HistoryContextUserChange shotToUser(UserEntity userEntity) {
        toUser = UserSnapshot.convertEntity(userEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        UserSnapshot.extractTemplateVars(vars, fromUser, PLACEHOLDER_FROM_USER);
        UserSnapshot.extractTemplateVars(vars, toUser, PLACEHOLDER_TO_USER);
        return vars;
    }

    @Override
    public String templateFromValue() {
        return fromUser != null ? fromUser.getName() : "";
    }

    @Override
    public String templateToValue() {
        return toUser != null ? toUser.getName() : "";
    }

    @Override
    public boolean collectMutableData(String messageTemplate, HistoryMutableDataCollector mutableDataCollector) {
        boolean hasMutableData = false;
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_FROM_USER) && toUser != null) {
            mutableDataCollector.getUserIdSet().add(toUser.getId());
            hasMutableData = true;
        }
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_TO_USER) && fromUser != null) {
            mutableDataCollector.getUserIdSet().add(fromUser.getId());
            hasMutableData = true;
        }
        return super.collectMutableData(messageTemplate, mutableDataCollector) || hasMutableData;
    }

    @Override
    public void spoofSnapshots(HistoryMutableDataCollector mutableDataCollector) {
        super.spoofSnapshots(mutableDataCollector);
        if (fromUser != null && mutableDataCollector.getUserKit().getMap().containsKey(fromUser.getId()))
            fromUser = UserSnapshot.convertEntity(mutableDataCollector.getUserKit().get(fromUser.getId()));
        if (toUser != null && mutableDataCollector.getUserKit().getMap().containsKey(toUser.getId()))
            toUser = UserSnapshot.convertEntity(mutableDataCollector.getUserKit().get(toUser.getId()));
    }
}
