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

/**
 * Runtime exception used to denote when a decoder or encoder has not been configured, but was requested.
 */
public class CodecUnavailableException extends RuntimeException {

    /**
     * Creates the exception with the specified message.
     *
     * @param message the message
     */
    public CodecUnavailableException(final String message) {
        super(message);
    }
}
