package io.apicurio.registry.operator.state.impl;

import io.apicurio.registry.operator.util.MaybeAvailable;
import io.quarkus.arc.Lock;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static io.apicurio.registry.operator.util.MaybeAvailable.unknown;

@ApplicationScoped
@Lock
@Getter
@Setter
@ToString
public class ClusterInfo {

    private MaybeAvailable<Boolean> isOCP = unknown();

    private MaybeAvailable<String> canonicalHost = unknown();
}
