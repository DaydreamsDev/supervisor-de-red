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

import java.util.ArrayList;
import java.util.List;

import com.daydreamsdev.supervisor.capture.CaptureException;
import com.daydreamsdev.supervisor.capture.CaptureService;
import com.daydreamsdev.supervisor.capture.DeviceInformation;
import com.daydreamsdev.supervisor.capture.PacketListener;
import com.daydreamsdev.supervisor.domain.PacketRecord;

final class FakeCaptureService implements CaptureService {

    private final List<DeviceInformation> devices = new ArrayList<>();

    private PacketListener packetListener;
    private String lastFilterExpression;
    private DeviceInformation lastDevice;
    private CaptureException listingException;
    private CaptureException startException;
    private CaptureException stopException;
    private boolean running;

    void setDevices(List<DeviceInformation> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
    }

    void setListingException(CaptureException listingException) {
        this.listingException = listingException;
    }

    void setStartException(CaptureException startException) {
        this.startException = startException;
    }

    void setStopException(CaptureException stopException) {
        this.stopException = stopException;
    }

    void emitPacket(PacketRecord packetRecord) {
        packetListener.onPacket(packetRecord);
    }

    String lastFilterExpression() {
        return lastFilterExpression;
    }

    DeviceInformation lastDevice() {
        return lastDevice;
    }

    @Override
    public List<DeviceInformation> listDevices() throws CaptureException {
        if (listingException != null) {
            throw listingException;
        }
        return List.copyOf(devices);
    }

    @Override
    public void startCapture(DeviceInformation device, String filterExpression,
            PacketListener packetListener) throws CaptureException {
        if (startException != null) {
            throw startException;
        }
        lastDevice = device;
        lastFilterExpression = filterExpression;
        this.packetListener = packetListener;
        running = true;
    }

    @Override
    public void stopCapture() throws CaptureException {
        if (stopException != null) {
            throw stopException;
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
