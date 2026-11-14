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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.Instant;

import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.daydreamsdev.supervisor.domain.ProtocolType;

import org.junit.jupiter.api.Test;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IcmpV4EchoPacket;
import org.pcap4j.packet.IcmpV6CommonPacket;
import org.pcap4j.packet.IcmpV6EchoRequestPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Rfc1349Tos;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IcmpV4Code;
import org.pcap4j.packet.namednumber.IcmpV4Type;
import org.pcap4j.packet.namednumber.IcmpV6Code;
import org.pcap4j.packet.namednumber.IcmpV6Type;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.packet.namednumber.UdpPort;
import org.pcap4j.util.MacAddress;

class PacketRecordMapperTest {

    private static final int MAXIMUM_SUMMARY_LENGTH = 200;

    private final PacketRecordMapper packetRecordMapper = new PacketRecordMapper();

    @Test
    void toRecord_givenTcpPacket_returnsTcpProtocolRecord() {
        TcpPacket tcpPacket = buildTcpPacket();

        PacketRecord packetRecord = mapWithTimestampWindow(tcpPacket);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.TCP);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(tcpPacket.length());
        assertThat(packetRecord.summaryLine()).isEqualTo("[TCP Header (20 bytes)]");
    }

    @Test
    void toRecord_givenUdpPacket_returnsUdpProtocolRecord() {
        UdpPacket udpPacket = buildUdpPacket();

        PacketRecord packetRecord = mapWithTimestampWindow(udpPacket);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.UDP);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(udpPacket.length());
        assertThat(packetRecord.summaryLine()).isEqualTo("[UDP Header (8 bytes)]");
    }

    @Test
    void toRecord_givenBareArpPacket_returnsArpProtocolRecord() throws Exception {
        ArpPacket arpPacket = buildArpPacket();

        PacketRecord packetRecord = mapWithTimestampWindow(arpPacket);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.ARP);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(arpPacket.length());
        assertThat(packetRecord.summaryLine()).isEqualTo("[ARP Header (28 bytes)]");
    }

    @Test
    void toRecord_givenEthernetWrappedArpPacket_returnsArpProtocolRecord() throws Exception {
        EthernetPacket ethernetArpPacket = buildEthernetArpPacket();

        PacketRecord packetRecord = mapWithTimestampWindow(ethernetArpPacket);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.ARP);
        assertThat(packetRecord.summaryLine()).isEqualTo("[Ethernet Header (14 bytes)]");
    }

    @Test
    void toRecord_givenIcmpV4CommonPacket_returnsIcmpProtocolRecord() {
        IcmpV4CommonPacket icmpV4Packet = buildIcmpV4Packet();

        PacketRecord packetRecord = mapWithTimestampWindow(icmpV4Packet);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.ICMP);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(icmpV4Packet.length());
        assertThat(packetRecord.summaryLine())
                .isEqualTo("[ICMPv4 Common Header (4 bytes)]");
    }

    @Test
    void toRecord_givenIcmpV6CommonPacket_returnsIcmpProtocolRecord() {
        IcmpV6CommonPacket icmpV6Packet = buildIcmpV6Packet();

        PacketRecord packetRecord = mapWithTimestampWindow(icmpV6Packet);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.ICMP);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(icmpV6Packet.length());
        assertThat(packetRecord.summaryLine())
                .isEqualTo("[ICMPv6 Common Header (4 bytes)]");
    }

    @Test
    void toRecord_givenEthernetIpv4TcpPacket_returnsTcpProtocolRecord() throws Exception {
        EthernetPacket ethernetIpv4TcpPacket = buildEthernetIpv4TcpPacket();

        PacketRecord packetRecord = mapWithTimestampWindow(ethernetIpv4TcpPacket);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.TCP);
        assertThat(packetRecord.summaryLine()).isEqualTo("[Ethernet Header (14 bytes)]");
    }

    @Test
    void toRecord_givenUnknownPacket_returnsOtherProtocolRecord() {
        UnknownPacket unknownPacket = buildUnknownPacket();

        PacketRecord packetRecord = mapWithTimestampWindow(unknownPacket);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.OTHER);
        assertThat(packetRecord.summaryLine()).isNotBlank();
        assertThat(packetRecord.summaryLine().length())
                .isLessThanOrEqualTo(MAXIMUM_SUMMARY_LENGTH);
    }

    @Test
    void toRecord_givenNullPacket_rejectsWithNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> packetRecordMapper.toRecord(null));
    }

    @Test
    void toRecord_givenFirstLineLongerThanTwoHundredCharacters_capsSummaryLine() {
        Packet packet = mock(Packet.class);
        when(packet.length()).thenReturn(1_500);
        when(packet.toString())
                .thenReturn("L".repeat(MAXIMUM_SUMMARY_LENGTH + 50) + "\nsecond line");

        PacketRecord packetRecord = packetRecordMapper.toRecord(packet);

        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.OTHER);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(1_500);
        assertThat(packetRecord.summaryLine())
                .isEqualTo("L".repeat(MAXIMUM_SUMMARY_LENGTH));
    }

    @Test
    void toRecord_givenLeadingBlankLines_usesFirstNonEmptyLineAsSummary() {
        Packet packet = mock(Packet.class);
        when(packet.toString()).thenReturn("\n   \nSome header line\nremaining lines");

        PacketRecord packetRecord = packetRecordMapper.toRecord(packet);

        assertThat(packetRecord.summaryLine()).isEqualTo("Some header line");
    }

    private PacketRecord mapWithTimestampWindow(Packet packet) {
        Instant beforeMapping = Instant.now();
        PacketRecord packetRecord = packetRecordMapper.toRecord(packet);
        Instant afterMapping = Instant.now();
        assertThat(packetRecord.capturedAt()).isBetween(beforeMapping, afterMapping);
        return packetRecord;
    }

    private static TcpPacket buildTcpPacket() {
        return new TcpPacket.Builder()
                .srcPort(TcpPort.HTTP)
                .dstPort(TcpPort.HTTPS)
                .correctLengthAtBuild(true)
                .build();
    }

    private static UdpPacket buildUdpPacket() {
        return new UdpPacket.Builder()
                .srcPort(UdpPort.DOMAIN)
                .dstPort(UdpPort.DOMAIN)
                .correctLengthAtBuild(true)
                .build();
    }

    private static ArpPacket buildArpPacket() throws Exception {
        return new ArpPacket.Builder()
                .hardwareType(ArpHardwareType.ETHERNET)
                .protocolType(EtherType.IPV4)
                .hardwareAddrLength((byte) 6)
                .protocolAddrLength((byte) 4)
                .operation(ArpOperation.REQUEST)
                .srcHardwareAddr(MacAddress.getByName("aa:bb:cc:dd:ee:ff"))
                .srcProtocolAddr(ipv4Address("192.168.1.10"))
                .dstHardwareAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                .dstProtocolAddr(ipv4Address("192.168.1.20"))
                .build();
    }

    private static IcmpV4CommonPacket buildIcmpV4Packet() {
        return new IcmpV4CommonPacket.Builder()
                .type(IcmpV4Type.ECHO)
                .code(IcmpV4Code.NO_CODE)
                .payloadBuilder(new IcmpV4EchoPacket.Builder()
                        .identifier((short) 7)
                        .sequenceNumber((short) 1))
                .build();
    }

    private static IcmpV6CommonPacket buildIcmpV6Packet() {
        return new IcmpV6CommonPacket.Builder()
                .type(IcmpV6Type.ECHO_REQUEST)
                .code(IcmpV6Code.NO_CODE)
                .payloadBuilder(new IcmpV6EchoRequestPacket.Builder()
                        .identifier((short) 7)
                        .sequenceNumber((short) 1))
                .build();
    }

    private static EthernetPacket buildEthernetArpPacket() throws Exception {
        return new EthernetPacket.Builder()
                .srcAddr(MacAddress.getByName("aa:bb:cc:dd:ee:ff"))
                .dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                .type(EtherType.ARP)
                .paddingAtBuild(true)
                .payloadBuilder(buildArpPacket().getBuilder())
                .build();
    }

    private static EthernetPacket buildEthernetIpv4TcpPacket() throws Exception {
        return new EthernetPacket.Builder()
                .srcAddr(MacAddress.getByName("aa:bb:cc:dd:ee:ff"))
                .dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                .type(EtherType.IPV4)
                .paddingAtBuild(true)
                .payloadBuilder(new IpV4Packet.Builder()
                        .version(IpVersion.IPV4)
                        .tos(IpV4Rfc1349Tos.newInstance((byte) 0))
                        .protocol(IpNumber.TCP)
                        .srcAddr(ipv4Address("192.168.1.10"))
                        .dstAddr(ipv4Address("192.168.1.20"))
                        .correctLengthAtBuild(true)
                        .payloadBuilder(buildTcpPacket().getBuilder()))
                .build();
    }

    private static UnknownPacket buildUnknownPacket() {
        return UnknownPacket.newPacket(new byte[] {1, 2, 3, 4}, 0, 4);
    }

    private static Inet4Address ipv4Address(String address) throws Exception {
        return (Inet4Address) InetAddress.getByName(address);
    }
}
