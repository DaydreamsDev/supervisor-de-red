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

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class CaptureStatistics {

    private final Map<ProtocolType, Long> packetCountsByProtocol;

    private CaptureStatistics(Map<ProtocolType, Long> packetCountsByProtocol) {
        this.packetCountsByProtocol = Map.copyOf(packetCountsByProtocol);
    }

    public static CaptureStatistics empty() {
        return new CaptureStatistics(new EnumMap<>(ProtocolType.class));
    }

    public static CaptureStatistics of(Map<ProtocolType, Long> packetCountsByProtocol) {
        Objects.requireNonNull(packetCountsByProtocol, "packetCountsByProtocol must not be null");
        EnumMap<ProtocolType, Long> defensiveCopy = new EnumMap<>(packetCountsByProtocol);
        defensiveCopy.forEach(CaptureStatistics::validateCount);
        return new CaptureStatistics(defensiveCopy);
    }

    public long countFor(ProtocolType protocolType) {
        Objects.requireNonNull(protocolType, "protocolType must not be null");
        return packetCountsByProtocol.getOrDefault(protocolType, 0L);
    }

    public long totalCount() {
        return packetCountsByProtocol.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    private static void validateCount(ProtocolType protocolType, Long packetCount) {
        if (packetCount < 0) {
            throw new IllegalArgumentException(
                    "packetCount for " + protocolType + " must not be negative");
        }
    }
}
