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

import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.util.LinkLayerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Pcap4jCaptureService implements CaptureService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Pcap4jCaptureService.class);

    private static final int SNAPSHOT_LENGTH = 65_536;
    private static final int READ_TIMEOUT_MILLIS = 10;
    private static final int INFINITE_LOOP_COUNT = -1;
    private static final long STOP_TIMEOUT_SECONDS = 5;
    private static final String CAPTURE_WORKER_THREAD_NAME = "pcap-capture-worker";
    private static final String PERMISSION_HINT =
            "Capture requires root privileges or CAP_NET_RAW on Linux, or Npcap on Windows.";

    private final PcapHandleOpener pcapHandleOpener;
    private final PacketRecordMapper packetRecordMapper;

    private volatile boolean running;
    private volatile PcapHandle captureHandle;
    private volatile ExecutorService captureExecutor;
    private volatile String captureDeviceName;

    public Pcap4jCaptureService() {
        this(Pcap4jCaptureService::openCaptureHandle);
    }

    Pcap4jCaptureService(PcapHandleOpener pcapHandleOpener) {
        this.pcapHandleOpener = pcapHandleOpener;
        this.packetRecordMapper = new PacketRecordMapper();
    }

    @Override
    public List<DeviceInformation> listDevices() throws CaptureException {
        List<PcapNetworkInterface> networkInterfaces;
        try {
            networkInterfaces = Pcaps.findAllDevs();
        } catch (PcapNativeException exception) {
            throw toListingException(exception);
        }
        return networkInterfaces.stream()
                .map(Pcap4jCaptureService::toDeviceInformation)
                .toList();
    }

    @Override
    public void startCapture(DeviceInformation device, String filterExpression,
            PacketListener packetListener) throws CaptureException {
        Objects.requireNonNull(device, "device must not be null");
        Objects.requireNonNull(filterExpression, "filterExpression must not be null");
        Objects.requireNonNull(packetListener, "packetListener must not be null");
        if (running) {
            throw new CaptureException(
                    "Capture is already running for device '" + device.name() + "'.");
        }
        PcapHandle openedHandle = pcapHandleOpener.open(device);
        try {
            if (!filterExpression.isBlank()) {
                applyFilter(openedHandle, device, filterExpression);
            }
        } catch (CaptureException | RuntimeException exception) {
            openedHandle.close();
            throw exception;
        }
        captureHandle = openedHandle;
        captureDeviceName = device.name();
        ExecutorService executor = newCaptureExecutor();
        captureExecutor = executor;
        running = true;
        executor.execute(() -> runCaptureLoop(openedHandle, packetListener, executor));
    }

    @Override
    public void stopCapture() throws CaptureException {
        if (!running) {
            return;
        }
        PcapHandle handle = captureHandle;
        ExecutorService executor = captureExecutor;
        try {
            handle.breakLoop();
        } catch (NotOpenException exception) {
            LOGGER.debug("Capture handle is already closed", exception);
        }
        try {
            if (!executor.awaitTermination(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                throw new CaptureException("The capture loop did not stop within "
                        + STOP_TIMEOUT_SECONDS + " seconds.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CaptureException(
                    "Interrupted while waiting for the capture loop to stop.", exception);
        } finally {
            executor.shutdown();
            running = false;
            captureHandle = null;
            captureExecutor = null;
            captureDeviceName = null;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void runCaptureLoop(PcapHandle handle, PacketListener packetListener,
            ExecutorService executor) {
        try {
            handle.loop(INFINITE_LOOP_COUNT, (org.pcap4j.packet.Packet packet) ->
                    packetListener.onPacket(packetRecordMapper.toRecord(packet)));
        } catch (PcapNativeException | NotOpenException exception) {
            LOGGER.error("Capture loop for device '{}' failed",
                    captureDeviceName, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.debug("Capture loop for device '{}' stopped", captureDeviceName);
        } finally {
            running = false;
            handle.close();
            executor.shutdown();
        }
    }

    private static void applyFilter(PcapHandle handle, DeviceInformation device,
            String filterExpression) throws CaptureException {
        try {
            handle.setFilter(filterExpression, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException exception) {
            throw toCaptureException(device.name(), exception);
        }
    }

    private static PcapHandle openCaptureHandle(DeviceInformation device) throws CaptureException {
        try {
            return new PcapHandle.Builder(device.name())
                    .snaplen(SNAPSHOT_LENGTH)
                    .promiscuousMode(PromiscuousMode.PROMISCUOUS)
                    .timeoutMillis(READ_TIMEOUT_MILLIS)
                    .build();
        } catch (PcapNativeException exception) {
            throw toCaptureException(device.name(), exception);
        }
    }

    private static ExecutorService newCaptureExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, CAPTURE_WORKER_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
    }

    static DeviceInformation toDeviceInformation(PcapNetworkInterface networkInterface) {
        String name = Objects.requireNonNullElse(networkInterface.getName(), "");
        List<DeviceAddress> addresses = networkInterface.getAddresses().stream()
                .filter(Objects::nonNull)
                .map(Pcap4jCaptureService::toDeviceAddress)
                .toList();
        return new DeviceInformation(name, networkInterface.getDescription(), "",
                extractMacAddress(networkInterface), addresses);
    }

    private static String extractMacAddress(PcapNetworkInterface networkInterface) {
        for (LinkLayerAddress linkLayerAddress : networkInterface.getLinkLayerAddresses()) {
            if (linkLayerAddress != null) {
                return linkLayerAddress.toString();
            }
        }
        return "";
    }

    static DeviceAddress toDeviceAddress(PcapAddress pcapAddress) {
        return new DeviceAddress(
                hostAddressOrEmpty(pcapAddress.getAddress()),
                hostAddressOrEmpty(pcapAddress.getNetmask()),
                hostAddressOrEmpty(pcapAddress.getBroadcastAddress()));
    }

    private static String hostAddressOrEmpty(InetAddress inetAddress) {
        return inetAddress == null ? "" : inetAddress.getHostAddress();
    }

    static CaptureException toCaptureException(String deviceName, Exception exception) {
        StringBuilder message = new StringBuilder(
                "Capture operation for device '" + deviceName + "' failed");
        appendDetail(message, exception);
        if (isPermissionError(exception)) {
            message.append(' ').append(PERMISSION_HINT);
        }
        return new CaptureException(message.toString(), exception);
    }

    static CaptureException toListingException(PcapNativeException exception) {
        StringBuilder message = new StringBuilder("Failed to list capture devices");
        appendDetail(message, exception);
        if (isPermissionError(exception)) {
            message.append(' ').append(PERMISSION_HINT);
        }
        return new CaptureException(message.toString(), exception);
    }

    private static void appendDetail(StringBuilder message, Exception exception) {
        String detail = exception.getMessage();
        if (detail != null && !detail.isBlank()) {
            message.append(": ").append(detail);
        } else {
            message.append('.');
        }
    }

    private static boolean isPermissionError(Exception exception) {
        return exception.getMessage() != null
                && exception.getMessage().toLowerCase(Locale.ROOT).contains("permission");
    }
}
