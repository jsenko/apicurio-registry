/*
 * Copyright 2023 JBoss Inc
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

package io.apicurio.registry.utils.migration;

import picocli.CommandLine.Command;

import java.nio.file.Path;

import static picocli.CommandLine.Option;

@Command(name = "migrate", version = "0.1", mixinStandardHelpOptions = true)
public class MigrateCommand implements Runnable {

    @Option(names = {"-f", "--file"}, description = "Path to the file to migrate.", required = true)
    private String exportFilePath;

    public void run() {

        var parts = exportFilePath.split("\\.");
        if (parts.length == 1) {
            parts[0] = parts[0] + "-v2";
        } else {
            parts[parts.length - 2] = parts[parts.length - 2] + "-v2";
        }
        Migrate.migrate(Path.of(exportFilePath), Path.of(String.join(".", parts)));
    }
}
