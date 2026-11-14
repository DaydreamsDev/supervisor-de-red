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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CaptureStatisticsTest {

    @Test
    void empty_hasZeroCountsForEveryProtocol() {
        CaptureStatistics captureStatistics = CaptureStatistics.empty();

        assertThat(captureStatistics.countFor(ProtocolType.TCP)).isZero();
        assertThat(captureStatistics.countFor(ProtocolType.UDP)).isZero();
        assertThat(captureStatistics.countFor(ProtocolType.ARP)).isZero();
        assertThat(captureStatistics.countFor(ProtocolType.ICMP)).isZero();
        assertThat(captureStatistics.countFor(ProtocolType.OTHER)).isZero();
        assertThat(captureStatistics.totalCount()).isZero();
    }

    @Test
    void of_givenCountsByProtocol_returnsThoseCounts() {
        CaptureStatistics captureStatistics = CaptureStatistics.of(Map.of(
                ProtocolType.TCP, 3L,
                ProtocolType.ARP, 1L));

        assertThat(captureStatistics.countFor(ProtocolType.TCP)).isEqualTo(3L);
        assertThat(captureStatistics.countFor(ProtocolType.ARP)).isEqualTo(1L);
        assertThat(captureStatistics.countFor(ProtocolType.UDP)).isZero();
        assertThat(captureStatistics.totalCount()).isEqualTo(4L);
    }

    @Test
    void of_givenNullCounts_rejectsWithNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> CaptureStatistics.of(null));
    }

    @Test
    void of_givenNegativeCount_rejectsWithIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() -> CaptureStatistics.of(
                Map.of(ProtocolType.TCP, -1L)));
    }

    @Test
    void of_doesNotRetainCallerMap_soLaterMutationIsInvisible() {
        Map<ProtocolType, Long> callerMap = new java.util.HashMap<>();
        callerMap.put(ProtocolType.TCP, 1L);

        CaptureStatistics captureStatistics = CaptureStatistics.of(callerMap);
        callerMap.put(ProtocolType.TCP, 99L);

        assertThat(captureStatistics.countFor(ProtocolType.TCP)).isEqualTo(1L);
    }
}
