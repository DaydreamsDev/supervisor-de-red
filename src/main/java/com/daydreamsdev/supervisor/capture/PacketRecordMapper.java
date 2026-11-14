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

package com.daydreamsdev.supervisor.capture;

import java.time.Instant;
import java.util.Objects;

import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.daydreamsdev.supervisor.domain.ProtocolType;

import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IcmpV6CommonPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

public final class PacketRecordMapper {

    private static final int MAXIMUM_SUMMARY_LENGTH = 200;

    public PacketRecord toRecord(Packet packet) {
        Objects.requireNonNull(packet, "packet must not be null");
        return new PacketRecord(
                Instant.now(),
                protocolTypeOf(packet),
                packet.length(),
                summaryLineOf(packet));
    }

    private static ProtocolType protocolTypeOf(Packet packet) {
        if (packet.contains(TcpPacket.class)) {
            return ProtocolType.TCP;
        }
        if (packet.contains(UdpPacket.class)) {
            return ProtocolType.UDP;
        }
        if (packet.contains(ArpPacket.class)) {
            return ProtocolType.ARP;
        }
        if (packet.contains(IcmpV4CommonPacket.class)
                || packet.contains(IcmpV6CommonPacket.class)) {
            return ProtocolType.ICMP;
        }
        return ProtocolType.OTHER;
    }

    private static String summaryLineOf(Packet packet) {
        String firstNonEmptyLine = packet.toString().lines()
                .filter(line -> !line.isBlank())
                .map(String::strip)
                .findFirst()
                .orElse("");
        if (firstNonEmptyLine.length() <= MAXIMUM_SUMMARY_LENGTH) {
            return firstNonEmptyLine;
        }
        return firstNonEmptyLine.substring(0, MAXIMUM_SUMMARY_LENGTH);
    }
}
