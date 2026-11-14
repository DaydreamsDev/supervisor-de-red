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

package com.daydreamsdev.supervisor.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PacketRecordTest {

    private static final Instant CAPTURED_AT = Instant.parse("2026-11-14T18:00:00Z");

    @Test
    void constructor_givenValidArguments_exposesEveryField() {
        PacketRecord packetRecord = new PacketRecord(CAPTURED_AT, ProtocolType.TCP, 64,
                "summary line");

        assertThat(packetRecord.capturedAt()).isEqualTo(CAPTURED_AT);
        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.TCP);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(64);
        assertThat(packetRecord.summaryLine()).isEqualTo("summary line");
    }

    @Test
    void constructor_givenNullCapturedAt_rejectsWithNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> new PacketRecord(null, ProtocolType.TCP, 64, "summary line"));
    }

    @Test
    void constructor_givenNullProtocol_rejectsWithNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> new PacketRecord(CAPTURED_AT, null, 64, "summary line"));
    }

    @Test
    void constructor_givenNullSummaryLine_rejectsWithNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> new PacketRecord(CAPTURED_AT, ProtocolType.TCP, 64, null));
    }

    @Test
    void constructor_givenNegativeSize_rejectsWithIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new PacketRecord(CAPTURED_AT, ProtocolType.TCP, -1, "summary line"));
    }
}
