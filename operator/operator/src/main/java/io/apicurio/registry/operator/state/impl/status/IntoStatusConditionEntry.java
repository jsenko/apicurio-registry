package io.apicurio.registry.operator.state.impl.status;

public interface IntoStatusConditionEntry {

    /**
     * Turn an object into {@link StatusConditionEntry}. It is mainly intended to be used by exceptions.
     */
    StatusConditionEntry intoStatusConditionEntry();
}
