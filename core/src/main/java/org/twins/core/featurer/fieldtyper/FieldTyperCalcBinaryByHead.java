package org.twins.core.featurer.fieldtyper;

import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

public abstract class FieldTyperCalcBinaryByHead<D extends FieldDescriptor, T extends FieldValue, S extends TwinFieldStorage, A extends TwinFieldSearch> extends FieldTyper<D, T, S, A> implements FieldTyperCalcBinary, FieldTyperCalcByHead {

}
