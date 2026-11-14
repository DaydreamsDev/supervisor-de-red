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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.daydreamsdev.supervisor.domain.ProtocolType;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.util.LinkLayerAddress;
import org.pcap4j.util.MacAddress;

class Pcap4jCaptureServiceTest {

    private static final DeviceInformation DEVICE =
            new DeviceInformation("eth0", "Ethernet device", "", "", List.of());

    @Test
    void toDeviceInfo_givenFullInterface_returnsAllFields() throws Exception {
        PcapNetworkInterface networkInterface = mock(PcapNetworkInterface.class);
        when(networkInterface.getName()).thenReturn("eth0");
        when(networkInterface.getDescription()).thenReturn("Ethernet device");
        when(networkInterface.getLinkLayerAddresses())
                .thenReturn(new ArrayList<>(List.of(
                        MacAddress.getByName("aa:bb:cc:dd:ee:ff"))));
        PcapAddress firstAddress = mock(PcapAddress.class);
        when(firstAddress.getAddress()).thenReturn(InetAddress.getByName("192.168.1.5"));
        when(firstAddress.getNetmask()).thenReturn(InetAddress.getByName("255.255.255.0"));
        when(firstAddress.getBroadcastAddress())
                .thenReturn(InetAddress.getByName("192.168.1.255"));
        when(networkInterface.getAddresses())
                .thenReturn(new ArrayList<>(List.of(firstAddress)));

        DeviceInformation deviceInformation = Pcap4jCaptureService.toDeviceInformation(networkInterface);

        assertThat(deviceInformation.name()).isEqualTo("eth0");
        assertThat(deviceInformation.description()).isEqualTo("Ethernet device");
        assertThat(deviceInformation.linkTypeName()).isEmpty();
        assertThat(deviceInformation.macAddress()).isEqualTo("aa:bb:cc:dd:ee:ff");
        assertThat(deviceInformation.addresses()).containsExactly(new DeviceAddress(
                "192.168.1.5", "255.255.255.0", "192.168.1.255"));
    }

    @Test
    void toDeviceInfo_givenNullName_returnsEmptyName() {
        PcapNetworkInterface networkInterface = mock(PcapNetworkInterface.class);
        when(networkInterface.getLinkLayerAddresses()).thenReturn(new ArrayList<>());
        when(networkInterface.getAddresses()).thenReturn(new ArrayList<>());

        DeviceInformation deviceInformation = Pcap4jCaptureService.toDeviceInformation(networkInterface);

        assertThat(deviceInformation.name()).isEmpty();
    }

    @Test
    void toDeviceInfo_givenMissingOptionalFields_returnsEmptyDefaults() {
        PcapNetworkInterface networkInterface = mock(PcapNetworkInterface.class);
        when(networkInterface.getName()).thenReturn("lo");
        when(networkInterface.getLinkLayerAddresses()).thenReturn(new ArrayList<>());
        when(networkInterface.getAddresses()).thenReturn(new ArrayList<>());

        DeviceInformation deviceInformation = Pcap4jCaptureService.toDeviceInformation(networkInterface);

        assertThat(deviceInformation.name()).isEqualTo("lo");
        assertThat(deviceInformation.description()).isEmpty();
        assertThat(deviceInformation.macAddress()).isEmpty();
        assertThat(deviceInformation.addresses()).isEmpty();
    }

    @Test
    void toDeviceInfo_givenNullLinkLayerAddressElement_returnsEmptyMacAddress() {
        PcapNetworkInterface networkInterface = mock(PcapNetworkInterface.class);
        when(networkInterface.getName()).thenReturn("eth0");
        when(networkInterface.getLinkLayerAddresses())
                .thenReturn(new ArrayList<>(Arrays.asList((LinkLayerAddress) null)));
        when(networkInterface.getAddresses()).thenReturn(new ArrayList<>());

        DeviceInformation deviceInformation = Pcap4jCaptureService.toDeviceInformation(networkInterface);

        assertThat(deviceInformation.macAddress()).isEmpty();
    }

    @Test
    void toDeviceAddress_givenNullFields_returnsEmptyStrings() {
        PcapAddress pcapAddress = mock(PcapAddress.class);

        DeviceAddress deviceAddress = Pcap4jCaptureService.toDeviceAddress(pcapAddress);

        assertThat(deviceAddress.address()).isEmpty();
        assertThat(deviceAddress.subnetMask()).isEmpty();
        assertThat(deviceAddress.broadcastAddress()).isEmpty();
    }

    @Test
    void toDeviceAddress_givenAddresses_returnsHostAddresses() throws Exception {
        PcapAddress pcapAddress = mock(PcapAddress.class);
        when(pcapAddress.getAddress()).thenReturn(InetAddress.getByName("10.0.0.2"));
        when(pcapAddress.getNetmask()).thenReturn(InetAddress.getByName("255.0.0.0"));
        when(pcapAddress.getBroadcastAddress())
                .thenReturn(InetAddress.getByName("10.255.255.255"));

        DeviceAddress deviceAddress = Pcap4jCaptureService.toDeviceAddress(pcapAddress);

        assertThat(deviceAddress.address()).isEqualTo("10.0.0.2");
        assertThat(deviceAddress.subnetMask()).isEqualTo("255.0.0.0");
        assertThat(deviceAddress.broadcastAddress()).isEqualTo("10.255.255.255");
    }

    @Test
    void toCaptureException_givenPermissionDenied_includesDeviceNameAndPrivilegeHint() {
        CaptureException captureException = Pcap4jCaptureService.toCaptureException(
                "eth0", new PcapNativeException("Permission denied"));

        assertThat(captureException.getMessage())
                .contains("eth0")
                .contains("Permission denied")
                .contains("root")
                .contains("CAP_NET_RAW")
                .contains("Npcap");
    }

    @Test
    void toCaptureException_givenNonPermissionFailure_omitsPrivilegeHint() {
        CaptureException captureException = Pcap4jCaptureService.toCaptureException(
                "eth0", new PcapNativeException("No such device"));

        assertThat(captureException.getMessage())
                .contains("eth0")
                .contains("No such device")
                .doesNotContain("CAP_NET_RAW");
    }

    @Test
    void toListingException_givenPermissionDenied_includesPrivilegeHint() {
        CaptureException captureException = Pcap4jCaptureService.toListingException(
                new PcapNativeException("Permission denied"));

        assertThat(captureException.getMessage())
                .contains("Failed to list capture devices")
                .contains("Permission denied")
                .contains("CAP_NET_RAW");
    }

    @Test
    void startCapture_givenNullDevice_rejectsWithNullPointerException() {
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> null);

        assertThatNullPointerException().isThrownBy(
                () -> service.startCapture(null, "", record -> { }));
    }

    @Test
    void startCapture_givenNullFilterExpression_rejectsWithNullPointerException() {
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> null);

        assertThatNullPointerException().isThrownBy(
                () -> service.startCapture(DEVICE, null, record -> { }));
    }

    @Test
    void startCapture_givenNullPacketListener_rejectsWithNullPointerException() {
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> null);

        assertThatNullPointerException().isThrownBy(
                () -> service.startCapture(DEVICE, "", null));
    }

    @Test
    void startCapture_givenAlreadyRunning_rejectsWithCaptureException() throws Exception {
        PcapHandle handle = mock(PcapHandle.class);
        makeLoopBlockUntilBroken(handle);
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> handle);

        service.startCapture(DEVICE, "", record -> { });
        try {
            awaitCondition(() -> service.isRunning());

            assertThatThrownBy(() -> service.startCapture(DEVICE, "", record -> { }))
                    .isInstanceOf(CaptureException.class)
                    .hasMessageContaining("already running");
        } finally {
            service.stopCapture();
        }
    }

    @Test
    void startCapture_givenTcpFilter_appliesOptimizedFilterBeforeLooping() throws Exception {
        PcapHandle handle = mock(PcapHandle.class);
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> handle);

        service.startCapture(DEVICE, "tcp", record -> { });

        InOrder inOrder = inOrder(handle);
        inOrder.verify(handle).setFilter("tcp", BpfProgram.BpfCompileMode.OPTIMIZE);
        inOrder.verify(handle).loop(eq(-1), any(org.pcap4j.core.PacketListener.class));
    }

    @Test
    void startCapture_givenBlankFilter_skipsFilterAndStartsLoop() throws Exception {
        PcapHandle handle = mock(PcapHandle.class);
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> handle);

        service.startCapture(DEVICE, "   ", record -> { });

        verify(handle, never()).setFilter(anyString(),
                any(BpfProgram.BpfCompileMode.class));
        verify(handle).loop(eq(-1), any(org.pcap4j.core.PacketListener.class));
    }

    @Test
    void startCapture_whenLoopDeliversTcpPacket_forwardsMappedRecordToListener()
            throws Exception {
        PcapHandle handle = mock(PcapHandle.class);
        TcpPacket tcpPacket = new TcpPacket.Builder()
                .srcPort(TcpPort.HTTP)
                .dstPort(TcpPort.HTTPS)
                .correctLengthAtBuild(true)
                .build();
        doAnswer(invocation -> {
            org.pcap4j.core.PacketListener pcapPacketListener =
                    invocation.getArgument(1, org.pcap4j.core.PacketListener.class);
            pcapPacketListener.gotPacket(tcpPacket);
            return null;
        }).when(handle).loop(eq(-1), any(org.pcap4j.core.PacketListener.class));
        List<PacketRecord> receivedRecords = new CopyOnWriteArrayList<>();
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> handle);
        Instant beforeCapture = Instant.now();

        service.startCapture(DEVICE, "", receivedRecords::add);
        awaitCondition(() -> !receivedRecords.isEmpty());
        Instant afterCapture = Instant.now();

        assertThat(receivedRecords).hasSize(1);
        PacketRecord packetRecord = receivedRecords.get(0);
        assertThat(packetRecord.protocol()).isEqualTo(ProtocolType.TCP);
        assertThat(packetRecord.sizeInBytes()).isEqualTo(tcpPacket.length());
        assertThat(packetRecord.summaryLine()).isEqualTo("[TCP Header (20 bytes)]");
        assertThat(packetRecord.capturedAt()).isBetween(beforeCapture, afterCapture);
    }

    @Test
    void startCapture_givenOpenerFailure_propagatesCaptureExceptionWithoutRunning() {
        CaptureException openerFailure = new CaptureException("cannot open device");
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> {
            throw openerFailure;
        });

        assertThatThrownBy(() -> service.startCapture(DEVICE, "", record -> { }))
                .isSameAs(openerFailure);

        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void startCapture_givenSetFilterFailure_closesHandleAndPropagatesCaptureException()
            throws Exception {
        PcapHandle handle = mock(PcapHandle.class);
        doThrow(new PcapNativeException("tcpdump: unknown host 'bogus'"))
                .when(handle).setFilter(eq("bogus"),
                        eq(BpfProgram.BpfCompileMode.OPTIMIZE));
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> handle);

        assertThatThrownBy(() -> service.startCapture(DEVICE, "bogus", record -> { }))
                .isInstanceOf(CaptureException.class)
                .hasMessageContaining("eth0")
                .hasMessageContaining("bogus");

        verify(handle).close();
        verify(handle, never()).loop(anyInt(), any(org.pcap4j.core.PacketListener.class));
        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void startCapture_givenLoopFailure_marksNotRunningAndClosesHandle() throws Exception {
        PcapHandle handle = mock(PcapHandle.class);
        doThrow(new PcapNativeException("interface down"))
                .when(handle).loop(eq(-1), any(org.pcap4j.core.PacketListener.class));
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> handle);

        service.startCapture(DEVICE, "", record -> { });

        awaitCondition(() -> !service.isRunning());
        verify(handle, atLeastOnce()).close();
        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void stopCapture_givenNotStarted_isIdempotent() throws Exception {
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> null);

        service.stopCapture();
        service.stopCapture();

        assertThat(service.isRunning()).isFalse();
    }

    @Test
    void stopCapture_stopsRunningLoop_closesHandleAndShutsDown() throws Exception {
        PcapHandle handle = mock(PcapHandle.class);
        makeLoopBlockUntilBroken(handle);
        Pcap4jCaptureService service = new Pcap4jCaptureService(device -> handle);

        service.startCapture(DEVICE, "", record -> { });
        awaitCondition(() -> service.isRunning());

        service.stopCapture();

        assertThat(service.isRunning()).isFalse();
        InOrder inOrder = inOrder(handle);
        inOrder.verify(handle).breakLoop();
        inOrder.verify(handle, atLeastOnce()).close();
    }

    @Test
    void startCapture_afterStopCapture_canStartAgain() throws Exception {
        PcapHandle firstHandle = mock(PcapHandle.class);
        PcapHandle secondHandle = mock(PcapHandle.class);
        makeLoopBlockUntilBroken(firstHandle);
        AtomicInteger openCount = new AtomicInteger();
        Pcap4jCaptureService service = new Pcap4jCaptureService(device ->
                openCount.getAndIncrement() == 0 ? firstHandle : secondHandle);

        service.startCapture(DEVICE, "", record -> { });
        awaitCondition(() -> service.isRunning());
        service.stopCapture();

        service.startCapture(DEVICE, "tcp", record -> { });

        awaitCondition(() -> !service.isRunning());
        assertThat(openCount.get()).isEqualTo(2);
        verify(secondHandle).setFilter("tcp", BpfProgram.BpfCompileMode.OPTIMIZE);
    }

    private static void makeLoopBlockUntilBroken(PcapHandle handle)
            throws Exception {
        CountDownLatch loopRelease = new CountDownLatch(1);
        doAnswer(invocation -> {
            loopRelease.await();
            return null;
        }).when(handle).loop(eq(-1), any(org.pcap4j.core.PacketListener.class));
        doAnswer(invocation -> {
            loopRelease.countDown();
            return null;
        }).when(handle).breakLoop();
    }

    private static void awaitCondition(Callable<Boolean> condition) throws Exception {
        long deadlineMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
        while (System.currentTimeMillis() < deadlineMillis) {
            if (condition.call()) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Condition was not met within 5 seconds");
    }
}
