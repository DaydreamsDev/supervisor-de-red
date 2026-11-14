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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.daydreamsdev.supervisor.capture.CaptureException;
import com.daydreamsdev.supervisor.capture.CaptureService;
import com.daydreamsdev.supervisor.capture.DeviceInformation;
import com.daydreamsdev.supervisor.capture.PacketListener;
import com.daydreamsdev.supervisor.domain.CaptureStatistics;
import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.daydreamsdev.supervisor.filter.FilterPreset;
import com.daydreamsdev.supervisor.filter.FilterValidationResult;
import com.daydreamsdev.supervisor.filter.FilterValidator;
import com.daydreamsdev.supervisor.io.SessionStore;
import com.daydreamsdev.supervisor.statistics.ProtocolCounter;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SupervisorController {

    static final int MAXIMUM_BUFFERED_PACKETS = 10_000;
    static final int MAXIMUM_VISIBLE_LINES = 500;
    static final int OUTPUT_REFRESH_INTERVAL_MILLIS = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorController.class);

    private final MainFrame mainFrame;
    private final CaptureService captureService;
    private final SessionStore sessionStore;
    private final ProtocolCounter protocolCounter = new ProtocolCounter();
    private final ConcurrentLinkedDeque<PacketRecord> bufferedPackets =
            new ConcurrentLinkedDeque<>();
    private final Timer outputRefreshTimer = new Timer(
            OUTPUT_REFRESH_INTERVAL_MILLIS, ignoredEvent -> refreshDashboard());
    private final FilterValidator filterValidator = new FilterValidator();
    private final ErrorDialogShower errorDialogShower;
    private final PacketListener packetListener = this::bufferPacket;

    private long lastPushedTotalCount = -1L;

    public SupervisorController(MainFrame mainFrame, CaptureService captureService,
            SessionStore sessionStore) {
        this(mainFrame, captureService, sessionStore,
                (title, message) -> JOptionPane.showMessageDialog(
                        mainFrame, message, title, JOptionPane.ERROR_MESSAGE));
    }

    SupervisorController(MainFrame mainFrame, CaptureService captureService,
            SessionStore sessionStore, ErrorDialogShower errorDialogShower) {
        this.mainFrame = mainFrame;
        this.captureService = captureService;
        this.sessionStore = sessionStore;
        this.errorDialogShower = errorDialogShower;
        wireViewEvents();
        refreshDevices();
    }

    public void start() {
        if (!outputRefreshTimer.isRunning()) {
            outputRefreshTimer.start();
        }
    }

    void refreshDashboard() {
        CaptureStatistics captureStatistics = protocolCounter.snapshot();
        long totalCount = captureStatistics.totalCount();
        boolean statisticsChanged = totalCount != lastPushedTotalCount;
        if (captureService.isRunning() || statisticsChanged) {
            lastPushedTotalCount = totalCount;
            refreshOutputArea();
            mainFrame.getTimeSeriesChartPanel().update(captureStatistics);
            mainFrame.getProtocolPieChartPanel().update(captureStatistics);
        }
        updateButtonStates();
        updateStatusBar();
    }

    void saveSessionTo(Path sessionFile) {
        try {
            sessionStore.saveSession(sessionFile, snapshotBufferedPackets());
        } catch (IOException exception) {
            LOGGER.error("Failed to save session to {}", sessionFile, exception);
            showErrorDialog(Messages.get("dialog.saveSession.title"), exception.getMessage());
        }
    }

    void loadSessionFrom(Path sessionFile) {
        List<PacketRecord> loadedPacketRecords;
        try {
            loadedPacketRecords = sessionStore.loadSession(sessionFile);
        } catch (IOException exception) {
            LOGGER.error("Failed to load session from {}", sessionFile, exception);
            showErrorDialog(Messages.get("dialog.loadSession.title"), exception.getMessage());
            return;
        }
        for (PacketRecord packetRecord : loadedPacketRecords) {
            bufferPacket(packetRecord);
        }
        refreshOutputArea();
        CaptureStatistics captureStatistics = protocolCounter.snapshot();
        mainFrame.getTimeSeriesChartPanel().update(captureStatistics);
        mainFrame.getProtocolPieChartPanel().update(captureStatistics);
        lastPushedTotalCount = captureStatistics.totalCount();
        updateButtonStates();
        updateStatusBar();
    }

    JFileChooser createSessionFileChooser() {
        JFileChooser sessionFileChooser = new JFileChooser();
        sessionFileChooser.setFileFilter(new FileNameExtensionFilter(
                Messages.get("chooser.jsonFilter"), "json"));
        return sessionFileChooser;
    }

    Timer getOutputRefreshTimer() {
        return outputRefreshTimer;
    }

    int bufferedPacketCount() {
        return bufferedPackets.size();
    }

    private void wireViewEvents() {
        mainFrame.getStartCaptureButton().addActionListener(ignoredEvent -> startCapture());
        mainFrame.getStopCaptureButton().addActionListener(ignoredEvent -> stopCapture());
        mainFrame.getClearOutputButton().addActionListener(ignoredEvent -> clearOutput());
        mainFrame.getSaveSessionButton().addActionListener(
                ignoredEvent -> chooseSessionFileAndSave());
        mainFrame.getLoadSessionButton().addActionListener(
                ignoredEvent -> chooseSessionFileAndLoad());
        mainFrame.getRefreshInterfacesButton().addActionListener(
                ignoredEvent -> refreshDevices());
        mainFrame.getOpenChartsButton().addActionListener(
                ignoredEvent -> mainFrame.openChartsWindow());
        mainFrame.getDeviceComboBox().addItemListener(
                ignoredEvent -> onDeviceSelectionChanged());
        for (JRadioButton filterRadioButton : mainFrame.getFilterRadioButtons()) {
            filterRadioButton.addItemListener(ignoredEvent -> onFilterSelectionChanged());
        }
        mainFrame.getExitMenuItem().addActionListener(ignoredEvent -> exitApplication());
        mainFrame.getSpanishLanguageMenuItem().addActionListener(
                ignoredEvent -> applyLocale(Locale.forLanguageTag("es")));
        mainFrame.getEnglishLanguageMenuItem().addActionListener(
                ignoredEvent -> applyLocale(Locale.ENGLISH));
        mainFrame.getLightThemeMenuItem().addActionListener(
                ignoredEvent -> applyLightTheme());
        mainFrame.getDarkThemeMenuItem().addActionListener(
                ignoredEvent -> applyDarkTheme());
    }

    private void bufferPacket(PacketRecord packetRecord) {
        protocolCounter.record(packetRecord);
        while (bufferedPackets.size() >= MAXIMUM_BUFFERED_PACKETS) {
            bufferedPackets.pollFirst();
        }
        bufferedPackets.offerLast(packetRecord);
    }

    private void startCapture() {
        DeviceInformation selectedDevice = mainFrame.getSelectedDevice();
        if (selectedDevice == null) {
            refreshDevices();
        }
        selectedDevice = mainFrame.getSelectedDevice();
        if (selectedDevice == null) {
            showErrorDialog(Messages.get("dialog.error.title"),
                    Messages.get("dialog.noDevices.message"));
            return;
        }
        String filterExpression = resolveActiveFilterExpression();
        if (filterExpression == null) {
            return;
        }
        try {
            captureService.startCapture(selectedDevice, filterExpression, packetListener);
        } catch (CaptureException exception) {
            LOGGER.warn("Capture could not start: {}", exception.getMessage());
            showErrorDialog(Messages.get("dialog.error.title"), exception.getMessage());
            return;
        }
        updateButtonStates();
        updateStatusBar();
    }

    private void stopCapture() {
        try {
            captureService.stopCapture();
        } catch (CaptureException exception) {
            LOGGER.warn("Capture could not stop cleanly: {}", exception.getMessage());
            showErrorDialog(Messages.get("dialog.error.title"), exception.getMessage());
        }
        refreshDashboard();
    }

    private void clearOutput() {
        bufferedPackets.clear();
        refreshOutputArea();
        updateButtonStates();
        updateStatusBar();
    }

    private void refreshDevices() {
        try {
            mainFrame.setDevices(captureService.listDevices());
        } catch (CaptureException exception) {
            LOGGER.error("Failed to list capture devices", exception);
            showErrorDialog(Messages.get("dialog.error.title"), exception.getMessage());
        }
        updateButtonStates();
        updateStatusBar();
    }

    private void chooseSessionFileAndSave() {
        JFileChooser sessionFileChooser = createSessionFileChooser();
        sessionFileChooser.setDialogTitle(Messages.get("dialog.saveSession.title"));
        if (sessionFileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            saveSessionTo(sessionFileChooser.getSelectedFile().toPath());
        }
    }

    private void chooseSessionFileAndLoad() {
        JFileChooser sessionFileChooser = createSessionFileChooser();
        sessionFileChooser.setDialogTitle(Messages.get("dialog.loadSession.title"));
        if (sessionFileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            loadSessionFrom(sessionFileChooser.getSelectedFile().toPath());
        }
    }

    private void exitApplication() {
        mainFrame.dispose();
    }

    private void applyLocale(Locale locale) {
        Messages.setLocale(locale);
        mainFrame.applyMessages();
        updateButtonStates();
        updateStatusBar();
    }

    private void applyLightTheme() {
        FlatLightLaf.setup();
        FlatLaf.updateUI();
    }

    private void applyDarkTheme() {
        FlatDarkLaf.setup();
        FlatLaf.updateUI();
    }

    private void onDeviceSelectionChanged() {
        updateButtonStates();
        updateStatusBar();
    }

    private void onFilterSelectionChanged() {
        updateStatusBar();
    }

    private void refreshOutputArea() {
        List<PacketRecord> bufferedSnapshot = snapshotBufferedPackets();
        int firstVisibleIndex = Math.max(0, bufferedSnapshot.size() - MAXIMUM_VISIBLE_LINES);
        StringBuilder outputText = new StringBuilder();
        for (int index = firstVisibleIndex; index < bufferedSnapshot.size(); index++) {
            if (index > firstVisibleIndex) {
                outputText.append('\n');
            }
            outputText.append(bufferedSnapshot.get(index).summaryLine());
        }
        mainFrame.setOutputText(outputText.toString());
    }

    private List<PacketRecord> snapshotBufferedPackets() {
        return List.copyOf(bufferedPackets);
    }

    private String resolveActiveFilterExpression() {
        if (mainFrame.isSpecialFilterSelected()) {
            String expression = mainFrame.getSpecialFilterExpression();
            FilterValidationResult validationResult = filterValidator.validate(expression);
            if (!validationResult.valid()) {
                showErrorDialog(Messages.get("dialog.invalidFilter.title"),
                        validationResult.errorMessage());
                return null;
            }
            return expression;
        }
        FilterPreset selectedPreset = mainFrame.getSelectedFilterPreset();
        return selectedPreset == null ? "" : selectedPreset.bpfExpression();
    }

    private String describeActiveFilter() {
        if (mainFrame.isSpecialFilterSelected()) {
            return Messages.get("filter.special") + ": "
                    + mainFrame.getSpecialFilterExpression();
        }
        FilterPreset selectedPreset = mainFrame.getSelectedFilterPreset();
        if (selectedPreset == null) {
            return Messages.get("status.filter.none");
        }
        return switch (selectedPreset) {
            case HTTP -> Messages.get("filter.http");
            case HTTPS -> Messages.get("filter.https");
            case FTP -> Messages.get("filter.ftp");
            case SMTP -> Messages.get("filter.smtp");
            case ICMP -> Messages.get("filter.icmp");
            case ARP -> Messages.get("filter.arp");
        };
    }

    private void updateButtonStates() {
        boolean running = captureService.isRunning();
        boolean deviceSelected = mainFrame.getSelectedDevice() != null;
        mainFrame.getStartCaptureButton().setEnabled(!running && deviceSelected);
        mainFrame.getStopCaptureButton().setEnabled(running);
        mainFrame.getDeviceComboBox().setEnabled(!running);
        mainFrame.getRefreshInterfacesButton().setEnabled(!running);
        mainFrame.getLoadSessionButton().setEnabled(!running);
        mainFrame.getSaveSessionButton().setEnabled(!bufferedPackets.isEmpty());
        mainFrame.getClearOutputButton().setEnabled(!bufferedPackets.isEmpty());
        mainFrame.setFilterControlsEnabled(!running);
    }

    private void updateStatusBar() {
        mainFrame.setStatusBarText(
                Messages.get("status.device") + ": " + selectedDeviceDescription()
                        + " | " + Messages.get("status.filter") + ": "
                        + describeActiveFilter()
                        + " | " + Messages.get("status.packets") + ": "
                        + protocolCounter.snapshot().totalCount());
    }

    private String selectedDeviceDescription() {
        DeviceInformation selectedDevice = mainFrame.getSelectedDevice();
        return selectedDevice == null
                ? Messages.get("status.device.none")
                : selectedDevice.name();
    }

    private void showErrorDialog(String title, String message) {
        errorDialogShower.show(title, message);
    }

    @FunctionalInterface
    interface ErrorDialogShower {

        void show(String title, String message);
    }
}
