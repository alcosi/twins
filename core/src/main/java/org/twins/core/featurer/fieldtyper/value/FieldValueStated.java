package org.twins.core.featurer.fieldtyper.value;

import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;


@Accessors(chain = true)
public abstract class FieldValueStated extends FieldValue {
    protected State state = State.UNDEFINED;

    public FieldValueStated(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }


    @Override
    public void copyValueTo(FieldValue dst) {
        var dstValue = (FieldValueStated) dst;
        copyValueTo(dstValue);
        dstValue.state = this.state;
    }

    public abstract void copyValueTo(FieldValueStated dst);

    public enum State {
        UNDEFINED,
        PRESENT,
        CLEARED
    }

    public FieldValueStated undefine() {
        this.state = State.UNDEFINED;
        onUndefine();
        return this;
    }

    public abstract void onUndefine();

    public boolean isUndefined() {
        updateMutableValueState();
        return this.state == State.UNDEFINED;
    }

    public FieldValueStated clear() {
        this.state =  State.CLEARED;
        onClear();
        return this;
    }

    public abstract void onClear();

    public boolean isCleared() {
        updateMutableValueState();
        return this.state == State.CLEARED;
    }

    protected void updateMutableValueState() {
        //override me if state is depend upon some mutable data
    }
}
