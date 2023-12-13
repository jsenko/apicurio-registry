/*
 * Copyright 2020 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.storage.error;

import io.apicurio.registry.model.BranchId;
import io.apicurio.registry.model.GAV;
import lombok.Getter;


public class BranchVersionAlreadyExistsException extends AlreadyExistsException {

    private static final long serialVersionUID = -2869727219770505486L;

    @Getter
    private final GAV gav;

    @Getter
    private final BranchId branchId;


    public BranchVersionAlreadyExistsException(GAV gav, BranchId branchId) {
        super(message(gav, branchId));
        this.gav = gav;
        this.branchId = branchId;
    }


    private static String message(GAV gav, BranchId branchId) {
        return "Branch '" + branchId + "' already contains version '" + gav + "'.";
    }
}
