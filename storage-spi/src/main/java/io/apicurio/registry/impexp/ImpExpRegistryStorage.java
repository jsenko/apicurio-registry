/*
 * Copyright 2020 Red Hat
 * Copyright 2020 IBM
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

package io.apicurio.registry.impexp;

import io.apicurio.registry.bytes.ContentHandle;
import io.apicurio.registry.model.ArtifactReferenceDto;
import io.apicurio.registry.utils.Functional.FunctionEx;

import java.util.List;
import java.util.Map;


// TODO Storage impexp lock or check
public interface ImpExpRegistryStorage extends ImportSink, ExportSource {


    long nextContentId();


    long nextGlobalId();


    long nextCommentId();


    default <R, X extends Exception> R withTransaction(FunctionEx<R, X> runnable) throws X {
        return runnable.run();
    }


    Map<String, ContentHandle> resolveReferences(List<ArtifactReferenceDto> references); // TODO?
}
