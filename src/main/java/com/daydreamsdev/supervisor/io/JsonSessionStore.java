/*
 * MIT License
 *
 * Copyright (c) 2026 Juan Camilo Rivera
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.daydreamsdev.supervisor.io;

import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class JsonSessionStore implements SessionStore {

    private static final TypeReference<List<PacketRecord>> PACKET_RECORD_LIST_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    public JsonSessionStore() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void saveSession(Path sessionFile, List<PacketRecord> packetRecords)
            throws IOException {
        Objects.requireNonNull(sessionFile, "sessionFile must not be null");
        Objects.requireNonNull(packetRecords, "packetRecords must not be null");
        objectMapper.writeValue(sessionFile.toFile(), packetRecords);
    }

    @Override
    public List<PacketRecord> loadSession(Path sessionFile) throws IOException {
        Objects.requireNonNull(sessionFile, "sessionFile must not be null");
        if (Files.notExists(sessionFile)) {
            throw new IOException("Session file does not exist: " + sessionFile);
        }
        String fileContent;
        try {
            fileContent = Files.readString(sessionFile);
        } catch (IOException exception) {
            throw new IOException("Session file cannot be read: " + sessionFile, exception);
        }
        if (fileContent.isBlank()) {
            throw new IOException("Session file is empty: " + sessionFile);
        }
        List<PacketRecord> packetRecords;
        try {
            packetRecords = objectMapper.readValue(fileContent, PACKET_RECORD_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IOException("Session file is malformed: " + sessionFile, exception);
        }
        if (packetRecords == null || packetRecords.contains(null)) {
            throw new IOException("Session file is malformed: " + sessionFile);
        }
        return packetRecords;
    }
}
