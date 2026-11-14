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

package com.daydreamsdev.supervisor.ui;

import java.time.Instant;
import java.util.List;

import com.daydreamsdev.supervisor.capture.DeviceAddress;
import com.daydreamsdev.supervisor.capture.DeviceInformation;
import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.daydreamsdev.supervisor.domain.ProtocolType;

final class DeviceFixtures {

    private static final long FIRST_PACKET_EPOCH_SECOND = 1_700_000_000L;

    private DeviceFixtures() {
    }

    static DeviceInformation device(String name) {
        return new DeviceInformation(
                name,
                "Test " + name,
                "Ethernet",
                "00:11:22:33:44:55",
                List.of(new DeviceAddress(
                        "192.168.1.10", "255.255.255.0", "192.168.1.255")));
    }

    static PacketRecord packet(int sequence) {
        return packet(sequence, ProtocolType.TCP);
    }

    static PacketRecord packet(int sequence, ProtocolType protocolType) {
        return new PacketRecord(
                Instant.ofEpochSecond(FIRST_PACKET_EPOCH_SECOND + sequence),
                protocolType,
                64 + sequence % 100,
                "Packet " + sequence + " summary");
    }
}
