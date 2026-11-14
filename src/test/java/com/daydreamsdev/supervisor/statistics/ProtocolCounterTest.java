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

package com.daydreamsdev.supervisor.statistics;

import com.daydreamsdev.supervisor.domain.CaptureStatistics;
import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.daydreamsdev.supervisor.domain.ProtocolType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ProtocolCounterTest {

    private static final Instant CAPTURED_AT = Instant.parse("2026-11-14T18:00:00Z");

    private static PacketRecord packetRecord(ProtocolType protocolType) {
        return new PacketRecord(CAPTURED_AT, protocolType, 64, "summary line");
    }

    @Test
    void snapshot_givenNoRecords_returnsEmptyStatistics() {
        ProtocolCounter protocolCounter = new ProtocolCounter();

        assertThat(protocolCounter.snapshot().totalCount()).isZero();
    }

    @Test
    void record_givenOneTcpPacket_expectsTcpCountOne() {
        ProtocolCounter protocolCounter = new ProtocolCounter();

        protocolCounter.record(packetRecord(ProtocolType.TCP));

        CaptureStatistics captureStatistics = protocolCounter.snapshot();
        assertThat(captureStatistics.countFor(ProtocolType.TCP)).isEqualTo(1L);
        assertThat(captureStatistics.totalCount()).isEqualTo(1L);
    }

    @Test
    void record_givenMixedProtocols_expectsEachProtocolCounted() {
        ProtocolCounter protocolCounter = new ProtocolCounter();

        protocolCounter.record(packetRecord(ProtocolType.TCP));
        protocolCounter.record(packetRecord(ProtocolType.TCP));
        protocolCounter.record(packetRecord(ProtocolType.UDP));
        protocolCounter.record(packetRecord(ProtocolType.ARP));
        protocolCounter.record(packetRecord(ProtocolType.ICMP));
        protocolCounter.record(packetRecord(ProtocolType.OTHER));

        CaptureStatistics captureStatistics = protocolCounter.snapshot();
        assertThat(captureStatistics.countFor(ProtocolType.TCP)).isEqualTo(2L);
        assertThat(captureStatistics.countFor(ProtocolType.UDP)).isEqualTo(1L);
        assertThat(captureStatistics.countFor(ProtocolType.ARP)).isEqualTo(1L);
        assertThat(captureStatistics.countFor(ProtocolType.ICMP)).isEqualTo(1L);
        assertThat(captureStatistics.countFor(ProtocolType.OTHER)).isEqualTo(1L);
        assertThat(captureStatistics.totalCount()).isEqualTo(6L);
    }

    @Test
    void snapshot_doesNotReflectRecordsMadeAfterward() {
        ProtocolCounter protocolCounter = new ProtocolCounter();
        protocolCounter.record(packetRecord(ProtocolType.TCP));

        CaptureStatistics before = protocolCounter.snapshot();
        protocolCounter.record(packetRecord(ProtocolType.TCP));

        assertThat(before.totalCount()).isEqualTo(1L);
        assertThat(protocolCounter.snapshot().totalCount()).isEqualTo(2L);
    }

    @Test
    void record_givenNullPacketRecord_rejectsWithNullPointerException() {
        ProtocolCounter protocolCounter = new ProtocolCounter();

        assertThatNullPointerException().isThrownBy(() -> protocolCounter.record(null));
    }

    @Test
    void record_givenConcurrentRecordsFromFourThreads_countsEveryPacket() throws Exception {
        ProtocolCounter protocolCounter = new ProtocolCounter();
        int packetsPerThread = 1_000;
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        List<ProtocolType> protocolTypes =
                List.of(ProtocolType.TCP, ProtocolType.UDP, ProtocolType.ARP, ProtocolType.ICMP);
        for (ProtocolType protocolType : protocolTypes) {
            executorService.submit(() -> {
                for (int packetIndex = 0; packetIndex < packetsPerThread; packetIndex++) {
                    protocolCounter.record(packetRecord(protocolType));
                }
            });
        }
        executorService.shutdown();
        assertThat(executorService.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(protocolCounter.snapshot().totalCount())
                .isEqualTo((long) packetsPerThread * protocolTypes.size());
    }
}
