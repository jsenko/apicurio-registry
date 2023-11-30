/*
 * Copyright 2021 Red Hat
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

package io.apicurio.registry.storage.impl.sql;

import io.agroal.api.AgroalDataSource;
import io.apicurio.registry.storage.error.RegistryStorageException;
import io.apicurio.registry.storage.impl.sql.jdb.HandleAction;
import io.apicurio.registry.storage.impl.sql.jdb.HandleCallback;
import io.apicurio.registry.storage.impl.sql.jdb.HandleImpl;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author eric.wittmann@gmail.com
 * @author Jakub Senko <em>m@jsenko.net</em>
 */
public abstract class AbstractHandleFactory implements HandleFactory {

    private static final ThreadLocal<Map<String, LocalState>> local = ThreadLocal.withInitial(HashMap::new);

    private AgroalDataSource dataSource;

    private String dataSourceId;

    private Logger log;

    protected void initialize(AgroalDataSource dataSource, String dataSourceId, Logger log) {
        // CDI error if there is no no-args constructor
        this.dataSource = dataSource;
        this.dataSourceId = dataSourceId;
        this.log = log;
    }

    @Override
    public <R, X extends Exception> R withHandle(HandleCallback<R, X> callback) throws X {
        try {
            if (state().handle == null) {
                state().handle = new HandleImpl(dataSource.getConnection());
                log.trace("Acquired connection: {} #{}", state().handle.getConnection(), state().handle.getConnection().hashCode());
            } else {
                state().level++;
                log.trace("Entering nested call (level {}): {} #{}", state().level, state().handle.getConnection(), state().handle.getConnection().hashCode());
            }
            return callback.withHandle(state().handle);
        } catch (SQLException ex) {
            state().rollback = true;
            throw new RegistryStorageException(ex);
        } catch (Exception ex) {
            state().rollback = true;
            throw ex;
        } finally {
            if (state().level > 0) {
                log.trace("Exiting nested call (level {}): {} #{}", state().level, state().handle.getConnection(), state().handle.getConnection().hashCode());
                state().level--;
            } else {
                try {
                    if (state().handle != null) {
                        if (state().rollback) {
                            log.trace("Rollback: {} #{}", state().handle.getConnection(), state().handle.getConnection().hashCode());
                            state().handle.getConnection().rollback();
                        } else {
                            log.trace("Commit: {} #{}", state().handle.getConnection(), state().handle.getConnection().hashCode());
                            state().handle.getConnection().commit();
                        }
                        state().handle.close();
                        log.trace("Closed connection: {} #{}", state().handle.getConnection(), state().handle.getConnection().hashCode());
                    }
                } catch (Exception ex) {
                    // Nothing we can do
                    log.error("Could not release database handle", ex);
                } finally {
                    local.get().remove(dataSourceId);
                }
            }
        }
    }


    @Override
    public <R, X extends Exception> R withHandleNoException(HandleCallback<R, X> callback) {
        try {
            return withHandle(callback);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RegistryStorageException(ex);
        }
    }


    @Override
    public <X extends Exception> void withHandleNoException(HandleAction<X> action) {
        try {
            withHandle(handle -> {
                action.withHandle(handle);
                return null;
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RegistryStorageException(ex);
        }
    }


    private LocalState state() {
        return local.get().computeIfAbsent(dataSourceId, k -> new LocalState());
    }


    private static class LocalState {

        HandleImpl handle;

        int level;

        boolean rollback;
    }
}
