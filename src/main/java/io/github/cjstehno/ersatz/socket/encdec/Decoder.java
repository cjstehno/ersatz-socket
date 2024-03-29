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
package io.github.cjstehno.ersatz.socket.encdec;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a decoder for an incoming request to the server.
 *
 * @param <T> the type of object being decoded
 */
@FunctionalInterface
public interface Decoder<T> {

    /**
     * Decodes the incoming request (InputStream) into the decoded object.
     *
     * @param stream the request input stream
     * @return the decoded object
     * @throws IOException if there is a problem during decoding
     */
    T decode(final InputStream stream) throws IOException;
}
