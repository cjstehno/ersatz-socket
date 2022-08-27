/**
 * Copyright (C) 2022 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.socket.junit;

import io.github.cjstehno.ersatz.socket.ErsatzSocketServer;
import lombok.val;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;

import static java.util.Arrays.stream;

/**
 * JUnit 5 Extension used to provide a simple means of managing an ErsatzSocketServer instance during testing.
 * <p>
 * A field of type <code>ErsatzSocketServer</code> must be specified in the test class.
 * <p>
 * BeforeEach test - the server will be started.
 * AfterEach test - the server will be stopped and the interactions will be reset.
 */
public class ErsatzSocketServerExtension implements BeforeEachCallback, AfterEachCallback {

    @Override public void beforeEach(final ExtensionContext context) throws Exception {
        findInstance(context.getRequiredTestInstance(), true).start();
    }

    @Override public void afterEach(final ExtensionContext context) throws Exception {
        val instance = findInstance(context.getRequiredTestInstance(), false);
        if (instance != null) {
            instance.close();
            instance.resetInteractions();
        }
    }

    private static ErsatzSocketServer findInstance(final Object testInstance, final boolean create) throws Exception {
        val field = findField(testInstance);
        Object instance = field.get(testInstance);

        if (instance == null && create) {
            instance = field.getType().getDeclaredConstructor().newInstance();
            field.set(testInstance, instance);
        }

        return (ErsatzSocketServer) instance;
    }

    private static Field findField(final Object testInstance) throws Exception {
        val field = stream(testInstance.getClass().getDeclaredFields())
            .filter(f -> f.getType().getSimpleName().endsWith("ErsatzSocketServer"))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("An ErsatzSocketServer field must be specified."));

        field.setAccessible(true);
        return field;
    }
}