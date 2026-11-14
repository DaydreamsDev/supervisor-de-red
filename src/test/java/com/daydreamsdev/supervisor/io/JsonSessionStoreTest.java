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
import com.daydreamsdev.supervisor.domain.ProtocolType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonSessionStoreTest {

    private static final Instant FIRST_CAPTURED_AT =
            Instant.parse("2026-11-14T18:00:00.123456789Z");
    private static final Instant SECOND_CAPTURED_AT = Instant.parse("2026-11-14T18:00:01Z");
    private static final PacketRecord FIRST_RECORD = new PacketRecord(
            FIRST_CAPTURED_AT, ProtocolType.TCP, 64, "TCP summary");
    private static final PacketRecord SECOND_RECORD = new PacketRecord(
            SECOND_CAPTURED_AT, ProtocolType.ICMP, 28, "ICMP summary");

    @TempDir
    Path temporaryDirectory;

    private JsonSessionStore sessionStore;

    @BeforeEach
    void setUp() {
        sessionStore = new JsonSessionStore();
    }

    @Test
    void saveSession_givenTwoRecords_writesJsonArrayWithIsoDatesAndAllFields()
            throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        sessionStore.saveSession(sessionFile, List.of(FIRST_RECORD, SECOND_RECORD));

        String expectedJson = """
                [{"capturedAt":"2026-11-14T18:00:00.123456789Z","protocol":"TCP",\
                "sizeInBytes":64,"summaryLine":"TCP summary"},\
                {"capturedAt":"2026-11-14T18:00:01Z","protocol":"ICMP","sizeInBytes":28,\
                "summaryLine":"ICMP summary"}]\
                """;
        assertThat(Files.readString(sessionFile)).isEqualTo(expectedJson);
    }

    @Test
    void saveSession_givenEmptyList_writesEmptyJsonArray() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        sessionStore.saveSession(sessionFile, List.of());

        assertThat(Files.readString(sessionFile)).isEqualTo("[]");
    }

    @Test
    void saveSession_givenNullSessionFile_throwsNullPointerExceptionWithClearMessage() {
        assertThatNullPointerException()
                .isThrownBy(() -> sessionStore.saveSession(null, List.of()))
                .withMessage("sessionFile must not be null");
    }

    @Test
    void saveSession_givenNullPacketRecords_throwsNullPointerExceptionWithClearMessage() {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        assertThatNullPointerException()
                .isThrownBy(() -> sessionStore.saveSession(sessionFile, null))
                .withMessage("packetRecords must not be null");
    }

    @Test
    void loadSession_givenSavedFile_reproducesRecordsExactly() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");
        sessionStore.saveSession(sessionFile, List.of(FIRST_RECORD, SECOND_RECORD));

        List<PacketRecord> loadedRecords = sessionStore.loadSession(sessionFile);

        assertThat(loadedRecords).containsExactly(FIRST_RECORD, SECOND_RECORD);
    }

    @Test
    void loadSession_givenSavedEmptyList_returnsEmptyList() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");
        sessionStore.saveSession(sessionFile, List.of());

        List<PacketRecord> loadedRecords = sessionStore.loadSession(sessionFile);

        assertThat(loadedRecords).isEmpty();
    }

    @Test
    void loadSession_givenMissingFile_throwsIOExceptionWithClearMessage() {
        Path sessionFile = temporaryDirectory.resolve("missing.json");

        assertThatIOException()
                .isThrownBy(() -> sessionStore.loadSession(sessionFile))
                .withMessageContaining("Session file does not exist")
                .withMessageContaining("missing.json")
                .withMessageNotContaining("\n")
                .withMessageNotContaining("at ");
    }

    @Test
    void loadSession_givenEmptyFile_throwsIOExceptionWithClearMessage() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");
        Files.writeString(sessionFile, "");

        assertThatIOException()
                .isThrownBy(() -> sessionStore.loadSession(sessionFile))
                .withMessageContaining("Session file is empty")
                .withMessageContaining("session.json")
                .withMessageNotContaining("\n")
                .withMessageNotContaining("at ");
    }

    @Test
    void loadSession_givenWhitespaceOnlyFile_throwsIOExceptionWithClearMessage()
            throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");
        Files.writeString(sessionFile, "   \n\t ");

        assertThatIOException()
                .isThrownBy(() -> sessionStore.loadSession(sessionFile))
                .withMessageContaining("Session file is empty")
                .withMessageContaining("session.json")
                .withMessageNotContaining("\n")
                .withMessageNotContaining("at ");
    }

    @Test
    void loadSession_givenMalformedJson_throwsIOExceptionWithClearMessage() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        assertLoadSessionRejectedAsMalformed(sessionFile, "[{\"capturedAt\": broken");
    }

    @Test
    void loadSession_givenJsonObjectInsteadOfArray_throwsIOException() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        assertLoadSessionRejectedAsMalformed(sessionFile,
                "{\"capturedAt\":\"2026-11-14T18:00:00Z\"}");
    }

    @Test
    void loadSession_givenUnknownProtocolValue_throwsIOException() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        assertLoadSessionRejectedAsMalformed(sessionFile, """
                [{"capturedAt":"2026-11-14T18:00:00Z","protocol":"BLAH","sizeInBytes":64,\
                "summaryLine":"TCP summary"}]
                """);
    }

    @Test
    void loadSession_givenMissingCapturedAtField_throwsIOException() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        assertLoadSessionRejectedAsMalformed(sessionFile,
                "[{\"protocol\":\"TCP\",\"sizeInBytes\":64,\"summaryLine\":\"TCP summary\"}]");
    }

    @Test
    void loadSession_givenUnparseableCapturedAt_throwsIOException() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        assertLoadSessionRejectedAsMalformed(sessionFile,
                "[{\"capturedAt\":\"not-a-date\",\"protocol\":\"TCP\",\"sizeInBytes\":64,"
                        + "\"summaryLine\":\"TCP summary\"}]");
    }

    @Test
    void loadSession_givenNullArrayElement_throwsIOException() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.json");

        assertLoadSessionRejectedAsMalformed(sessionFile, "[null]");
    }

    @Test
    void loadSession_givenNullSessionFile_throwsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> sessionStore.loadSession(null))
                .withMessage("sessionFile must not be null");
    }

    private void assertLoadSessionRejectedAsMalformed(Path sessionFile, String content)
            throws IOException {
        Files.writeString(sessionFile, content);

        assertThatIOException()
                .isThrownBy(() -> sessionStore.loadSession(sessionFile))
                .withMessageContaining("Session file is malformed")
                .withMessageContaining("session.json")
                .withMessageNotContaining("\n")
                .withMessageNotContaining("at ");
    }
}
