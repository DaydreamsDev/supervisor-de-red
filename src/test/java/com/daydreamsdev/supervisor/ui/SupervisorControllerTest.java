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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import com.daydreamsdev.supervisor.capture.CaptureException;
import com.daydreamsdev.supervisor.domain.PacketRecord;
import com.daydreamsdev.supervisor.domain.ProtocolType;
import com.daydreamsdev.supervisor.filter.FilterPreset;
import com.daydreamsdev.supervisor.io.JsonSessionStore;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class SupervisorControllerTest {

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    @BeforeAll
    static void requireDisplay() {
        Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(),
                "Swing view tests require a display");
    }

    @TempDir
    Path temporaryDirectory;

    private MainFrame mainFrame;
    private FakeCaptureService captureService;
    private JsonSessionStore sessionStore;
    private List<DialogInvocation> dialogInvocations;
    private SupervisorController controller;

    @BeforeEach
    void setUp() {
        Messages.setLocale(SPANISH);
        mainFrame = new MainFrame();
        captureService = new FakeCaptureService();
        sessionStore = new JsonSessionStore();
        dialogInvocations = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        if (controller != null) {
            controller.getOutputRefreshTimer().stop();
        }
        FlatLightLaf.setup();
        Messages.setLocale(SPANISH);
    }

    private SupervisorController createController() {
        controller = new SupervisorController(mainFrame, captureService, sessionStore,
                (title, message) -> dialogInvocations.add(new DialogInvocation(title, message)));
        return controller;
    }

    private void startCaptureWithDefaultDevice() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();
        mainFrame.getStartCaptureButton().doClick();
    }

    @Test
    void constructor_listsDevicesIntoComboBox() {
        captureService.setDevices(List.of(
                DeviceFixtures.device("eth0"), DeviceFixtures.device("wlan0")));

        createController();

        assertThat(mainFrame.getDeviceComboBox().getItemCount()).isEqualTo(2);
        assertThat(mainFrame.getSelectedDevice().name()).isEqualTo("eth0");
        assertThat(mainFrame.getStartCaptureButton().isEnabled()).isTrue();
    }

    @Test
    void constructor_givenListingFailure_showsErrorDialog() {
        captureService.setListingException(new CaptureException("cannot list devices"));

        createController();

        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).title()).isEqualTo("Error");
        assertThat(dialogInvocations.get(0).message()).isEqualTo("cannot list devices");
    }

    @Test
    void startCapture_givenAllPacketsFilter_startsWithEmptyFilterExpression() {
        startCaptureWithDefaultDevice();

        assertThat(captureService.isRunning()).isTrue();
        assertThat(captureService.lastDevice().name()).isEqualTo("eth0");
        assertThat(captureService.lastFilterExpression()).isEmpty();
    }

    @Test
    void startCapture_givenHttpPreset_startsWithPresetBpfExpression() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();
        mainFrame.getFilterRadioButtons().get(1).setSelected(true);

        mainFrame.getStartCaptureButton().doClick();

        assertThat(captureService.lastFilterExpression())
                .isEqualTo(FilterPreset.HTTP.bpfExpression());
    }

    @Test
    void startCapture_givenValidSpecialFilter_startsWithGivenExpression() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();
        mainFrame.getSpecialFilterField().setText("tcp port 8080");
        mainFrame.getSpecialFilterRadioButton().setSelected(true);

        mainFrame.getStartCaptureButton().doClick();

        assertThat(captureService.isRunning()).isTrue();
        assertThat(captureService.lastFilterExpression()).isEqualTo("tcp port 8080");
    }

    @Test
    void startCapture_givenBlankSpecialFilter_showsInvalidFilterDialogAndDoesNotStart() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();
        mainFrame.getSpecialFilterRadioButton().setSelected(true);

        mainFrame.getStartCaptureButton().doClick();

        assertThat(captureService.isRunning()).isFalse();
        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).title()).isEqualTo("Filtro inválido");
        assertThat(dialogInvocations.get(0).message())
                .isEqualTo("The filter expression must not be blank.");
    }

    @Test
    void startCapture_givenOversizedSpecialFilter_showsLengthDialogAndDoesNotStart() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();
        mainFrame.getSpecialFilterField().setText("a".repeat(513));
        mainFrame.getSpecialFilterRadioButton().setSelected(true);

        mainFrame.getStartCaptureButton().doClick();

        assertThat(captureService.isRunning()).isFalse();
        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).message())
                .isEqualTo("The filter expression must not exceed 512 characters.");
    }

    @Test
    void startCapture_givenServiceFailure_showsExceptionMessageDialog() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        captureService.setStartException(new CaptureException("permission denied"));
        createController();

        mainFrame.getStartCaptureButton().doClick();

        assertThat(captureService.isRunning()).isFalse();
        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).title()).isEqualTo("Error");
        assertThat(dialogInvocations.get(0).message()).isEqualTo("permission denied");
    }

    @Test
    void startCapture_givenNoDeviceSelected_relistsDevicesAndShowsNoDevicesDialog() {
        createController();

        mainFrame.getStartCaptureButton().setEnabled(true);
        mainFrame.getStartCaptureButton().doClick();

        assertThat(captureService.isRunning()).isFalse();
        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).message())
                .isEqualTo("No hay interfaces de red disponibles.");
    }

    @Test
    void stopCapture_givenRunningCapture_stopsServiceAndRestoresButtons() {
        startCaptureWithDefaultDevice();
        assertThat(mainFrame.getStopCaptureButton().isEnabled()).isTrue();

        mainFrame.getStopCaptureButton().doClick();

        assertThat(captureService.isRunning()).isFalse();
        assertThat(mainFrame.getStartCaptureButton().isEnabled()).isTrue();
        assertThat(mainFrame.getStopCaptureButton().isEnabled()).isFalse();
        assertThat(mainFrame.getDeviceComboBox().isEnabled()).isTrue();
        assertThat(mainFrame.getRefreshInterfacesButton().isEnabled()).isTrue();
    }

    @Test
    void stopCapture_givenServiceFailure_showsExceptionMessageDialog() {
        startCaptureWithDefaultDevice();
        captureService.setStopException(new CaptureException("stop failed"));

        mainFrame.getStopCaptureButton().doClick();

        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).message()).isEqualTo("stop failed");
    }

    @Test
    void refreshInterfaces_givenNewDeviceList_replacesComboBoxContent() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();
        captureService.setDevices(List.of(
                DeviceFixtures.device("eth0"),
                DeviceFixtures.device("eth1"),
                DeviceFixtures.device("eth2")));

        mainFrame.getRefreshInterfacesButton().doClick();

        assertThat(mainFrame.getDeviceComboBox().getItemCount()).isEqualTo(3);
        assertThat(mainFrame.getSelectedDevice().name()).isEqualTo("eth0");
    }

    @Test
    void refreshDashboard_givenBufferedPackets_movesSummaryLinesToOutputAndUpdatesStatus() {
        startCaptureWithDefaultDevice();
        captureService.emitPacket(DeviceFixtures.packet(1));
        captureService.emitPacket(DeviceFixtures.packet(2));
        captureService.emitPacket(DeviceFixtures.packet(3, ProtocolType.UDP));

        assertThat(mainFrame.getOutputText()).isEmpty();

        controller.refreshDashboard();

        assertThat(mainFrame.getOutputText())
                .isEqualTo("Packet 1 summary\nPacket 2 summary\nPacket 3 summary");
        assertThat(mainFrame.getStatusBarText())
                .isEqualTo("Interfaz: eth0 | Filtro: sin filtro | Paquetes: 3");
        assertThat(timeSeriesLatestPacketCount()).isEqualTo(3L);
    }

    @Test
    void refreshDashboard_givenMoreThanFiveHundredPackets_keepsOnlyLastFiveHundredLines() {
        startCaptureWithDefaultDevice();
        for (int sequence = 1; sequence <= 600; sequence++) {
            captureService.emitPacket(DeviceFixtures.packet(sequence));
        }

        controller.refreshDashboard();

        List<String> outputLines = List.of(mainFrame.getOutputText().split("\n"));
        assertThat(outputLines).hasSize(500);
        assertThat(outputLines.get(0)).isEqualTo("Packet 101 summary");
        assertThat(outputLines.get(499)).isEqualTo("Packet 600 summary");
    }

    @Test
    void bufferedPackets_givenMoreThanCapacity_dropsOldestPackets() {
        startCaptureWithDefaultDevice();
        for (int sequence = 1; sequence <= 10_005; sequence++) {
            captureService.emitPacket(DeviceFixtures.packet(sequence));
        }

        assertThat(controller.bufferedPacketCount())
                .isEqualTo(SupervisorController.MAXIMUM_BUFFERED_PACKETS);

        controller.refreshDashboard();

        assertThat(List.of(mainFrame.getOutputText().split("\n")).get(0))
                .isEqualTo("Packet 9506 summary");
    }

    @Test
    void refreshDashboard_updatesChartPanelsWithProtocolSnapshot() {
        startCaptureWithDefaultDevice();
        captureService.emitPacket(DeviceFixtures.packet(1));
        captureService.emitPacket(DeviceFixtures.packet(2, ProtocolType.TCP));
        captureService.emitPacket(DeviceFixtures.packet(3, ProtocolType.UDP));
        captureService.emitPacket(DeviceFixtures.packet(4, ProtocolType.ARP));

        controller.refreshDashboard();

        assertThat(timeSeriesLatestPacketCount()).isEqualTo(4L);
        assertThat(pieSliceValue(ProtocolType.TCP)).isEqualTo(2L);
        assertThat(pieSliceValue(ProtocolType.UDP)).isEqualTo(1L);
        assertThat(pieSliceValue(ProtocolType.ARP)).isEqualTo(1L);
        assertThat(pieSliceValue(ProtocolType.ICMP)).isEqualTo(0L);
    }

    @Test
    void refreshDashboard_givenNoChangesWhileIdle_doesNotAppendChartPoints() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        controller.refreshDashboard();
        int dataPointCountAfterFirstRefresh = timeSeriesDataPointCount();

        controller.refreshDashboard();

        assertThat(timeSeriesDataPointCount()).isEqualTo(dataPointCountAfterFirstRefresh);
    }

    @Test
    void outputRefreshTimer_givenConfiguration_ticksEverySecondAndRepeats() {
        createController();

        assertThat(controller.getOutputRefreshTimer().getDelay())
                .isEqualTo(SupervisorController.OUTPUT_REFRESH_INTERVAL_MILLIS);
        assertThat(controller.getOutputRefreshTimer().isRepeats()).isTrue();
        assertThat(controller.getOutputRefreshTimer().isRunning()).isFalse();
    }

    @Test
    void start_givenRepeatedCalls_startsRefreshTimerOnce() {
        createController();

        controller.start();
        controller.start();

        assertThat(controller.getOutputRefreshTimer().isRunning()).isTrue();
    }

    @Test
    void saveSessionTo_givenBufferedPackets_persistsDequeSnapshot() throws Exception {
        startCaptureWithDefaultDevice();
        captureService.emitPacket(DeviceFixtures.packet(1));
        captureService.emitPacket(DeviceFixtures.packet(2, ProtocolType.UDP));
        Path sessionFile = temporaryDirectory.resolve("session.json");

        controller.saveSessionTo(sessionFile);

        List<PacketRecord> loadedPacketRecords = sessionStore.loadSession(sessionFile);
        assertThat(loadedPacketRecords).hasSize(2);
        assertThat(loadedPacketRecords.get(0).summaryLine()).isEqualTo("Packet 1 summary");
        assertThat(loadedPacketRecords.get(1).summaryLine()).isEqualTo("Packet 2 summary");
        assertThat(loadedPacketRecords.get(1).protocol()).isEqualTo(ProtocolType.UDP);
    }

    @Test
    void saveSessionTo_givenUnwritableFile_showsErrorDialog() {
        startCaptureWithDefaultDevice();
        captureService.emitPacket(DeviceFixtures.packet(1));
        Path missingSessionFile = temporaryDirectory.resolve("no-such-folder/session.json");

        controller.saveSessionTo(missingSessionFile);

        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).title()).isEqualTo("Guardar sesión");
        assertThat(dialogInvocations.get(0).message()).isNotBlank();
    }

    @Test
    void loadSessionFrom_givenValidSessionFile_restoresPacketsIntoOutputAndCharts()
            throws Exception {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();
        Path sessionFile = temporaryDirectory.resolve("session.json");
        sessionStore.saveSession(sessionFile, List.of(
                DeviceFixtures.packet(1),
                DeviceFixtures.packet(2, ProtocolType.UDP)));

        controller.loadSessionFrom(sessionFile);

        assertThat(mainFrame.getOutputText())
                .isEqualTo("Packet 1 summary\nPacket 2 summary");
        assertThat(mainFrame.getStatusBarText())
                .isEqualTo("Interfaz: eth0 | Filtro: sin filtro | Paquetes: 2");
        assertThat(timeSeriesLatestPacketCount()).isEqualTo(2L);
    }

    @Test
    void loadSessionFrom_givenMissingFile_showsErrorDialog() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        controller.loadSessionFrom(temporaryDirectory.resolve("missing.json"));

        assertThat(dialogInvocations).hasSize(1);
        assertThat(dialogInvocations.get(0).title()).isEqualTo("Cargar sesión");
        assertThat(dialogInvocations.get(0).message()).isNotBlank();
    }

    @Test
    void clearOutput_clearsOutputAndBuffer_keepsCumulativeStatistics() {
        startCaptureWithDefaultDevice();
        for (int sequence = 1; sequence <= 5; sequence++) {
            captureService.emitPacket(DeviceFixtures.packet(sequence));
        }
        controller.refreshDashboard();
        assertThat(mainFrame.getOutputText()).isNotBlank();

        mainFrame.getClearOutputButton().doClick();

        assertThat(mainFrame.getOutputText()).isEmpty();
        assertThat(controller.bufferedPacketCount()).isZero();
        assertThat(mainFrame.getStatusBarText()).contains("Paquetes: 5");
        assertThat(mainFrame.getSaveSessionButton().isEnabled()).isFalse();
        assertThat(mainFrame.getClearOutputButton().isEnabled()).isFalse();
    }

    @Test
    void buttonStates_followCaptureLifecycle_transitionStartAndStop() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        assertThat(mainFrame.getStartCaptureButton().isEnabled()).isTrue();
        assertThat(mainFrame.getStopCaptureButton().isEnabled()).isFalse();
        assertThat(mainFrame.getSaveSessionButton().isEnabled()).isFalse();

        mainFrame.getStartCaptureButton().doClick();

        assertThat(mainFrame.getStartCaptureButton().isEnabled()).isFalse();
        assertThat(mainFrame.getStopCaptureButton().isEnabled()).isTrue();
        assertThat(mainFrame.getDeviceComboBox().isEnabled()).isFalse();
        assertThat(mainFrame.getRefreshInterfacesButton().isEnabled()).isFalse();
        assertThat(mainFrame.getLoadSessionButton().isEnabled()).isFalse();
        assertThat(mainFrame.getFilterRadioButtons())
                .allSatisfy(radioButton -> assertThat(radioButton.isEnabled()).isFalse());

        mainFrame.getStopCaptureButton().doClick();

        assertThat(mainFrame.getStartCaptureButton().isEnabled()).isTrue();
        assertThat(mainFrame.getStopCaptureButton().isEnabled()).isFalse();
        assertThat(mainFrame.getDeviceComboBox().isEnabled()).isTrue();
        assertThat(mainFrame.getLoadSessionButton().isEnabled()).isTrue();
    }

    @Test
    void languageMenu_englishSelection_relabelsViewAndStatusBar() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        mainFrame.getEnglishLanguageMenuItem().doClick();

        assertThat(mainFrame.getStartCaptureButton().getText()).isEqualTo("CAPTURE");
        assertThat(mainFrame.getStatusBarText())
                .contains("Interface: eth0")
                .contains("Filter: no filter")
                .contains("Packets: 0");

        mainFrame.getSpanishLanguageMenuItem().doClick();

        assertThat(mainFrame.getStartCaptureButton().getText()).isEqualTo("CAPTURA");
        assertThat(mainFrame.getStatusBarText())
                .contains("Interfaz: eth0")
                .contains("Filtro: sin filtro")
                .contains("Paquetes: 0");
    }

    @Test
    void themeMenu_appliesDarkAndLightFlatLaf() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        mainFrame.getDarkThemeMenuItem().doClick();

        assertThat(UIManager.getLookAndFeel()).isInstanceOf(FlatDarkLaf.class);

        mainFrame.getLightThemeMenuItem().doClick();

        assertThat(UIManager.getLookAndFeel()).isInstanceOf(FlatLightLaf.class);
    }

    @Test
    void exitMenuItem_disposesAllWindows() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        mainFrame.getExitMenuItem().doClick();

        assertThat(mainFrame.isDisplayable()).isFalse();
        assertThat(mainFrame.getChartsFrame().isDisplayable()).isFalse();
    }

    @Test
    void createSessionFileChooser_offersJsonFileFilter() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        JFileChooser sessionFileChooser = controller.createSessionFileChooser();

        assertThat(sessionFileChooser.getChoosableFileFilters())
                .anySatisfy(fileFilter -> {
                    assertThat(fileFilter.getDescription())
                            .isEqualTo("Sesiones JSON (*.json)");
                    assertThat(fileFilter.accept(new File("session.json"))).isTrue();
                });
    }

    @Test
    void filterSelection_updatesStatusBarDescription() {
        captureService.setDevices(List.of(DeviceFixtures.device("eth0")));
        createController();

        mainFrame.getFilterRadioButtons().get(1).setSelected(true);
        assertThat(mainFrame.getStatusBarText()).contains("Filtro: HTTP (80)");

        mainFrame.getSpecialFilterField().setText("tcp port 80");
        mainFrame.getSpecialFilterRadioButton().setSelected(true);
        assertThat(mainFrame.getStatusBarText()).contains("Filtro especial: tcp port 80");
    }

    private long timeSeriesLatestPacketCount() {
        ChartPanel chartPanel = (ChartPanel) mainFrame.getTimeSeriesChartPanel().getComponent(0);
        TimeSeriesCollection dataset = (TimeSeriesCollection) chartPanel.getChart()
                .getXYPlot().getDataset();
        TimeSeries series = dataset.getSeries(0);
        return series.getValue(series.getItemCount() - 1).longValue();
    }

    private int timeSeriesDataPointCount() {
        ChartPanel chartPanel = (ChartPanel) mainFrame.getTimeSeriesChartPanel().getComponent(0);
        TimeSeriesCollection dataset = (TimeSeriesCollection) chartPanel.getChart()
                .getXYPlot().getDataset();
        return dataset.getSeries(0).getItemCount();
    }

    private long pieSliceValue(ProtocolType protocolType) {
        ChartPanel chartPanel = (ChartPanel) mainFrame.getProtocolPieChartPanel().getComponent(0);
        PiePlot<?> piePlot = (PiePlot<?>) chartPanel.getChart().getPlot();
        PieDataset dataset = piePlot.getDataset();
        Number sliceValue = dataset.getValue(protocolType);
        return sliceValue == null ? 0L : sliceValue.longValue();
    }

    private record DialogInvocation(String title, String message) {
    }
}
