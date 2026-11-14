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

import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.daydreamsdev.supervisor.capture.DeviceInformation;
import com.daydreamsdev.supervisor.filter.FilterPreset;
import org.jfree.chart.ChartPanel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MainFrameTest {

    @BeforeAll
    static void requireDisplay() {
        Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(),
                "Swing view tests require a display");
    }

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    private MainFrame mainFrame;

    @BeforeEach
    void setUp() {
        Messages.setLocale(SPANISH);
        mainFrame = new MainFrame();
    }

    @AfterEach
    void restoreSpanishLocale() {
        Messages.setLocale(SPANISH);
    }

    @Test
    void constructor_givenInitialState_disablesUnavailableActions() {
        assertThat(mainFrame.getStartCaptureButton().isEnabled()).isFalse();
        assertThat(mainFrame.getStopCaptureButton().isEnabled()).isFalse();
        assertThat(mainFrame.getSaveSessionButton().isEnabled()).isFalse();
        assertThat(mainFrame.getClearOutputButton().isEnabled()).isFalse();
        assertThat(mainFrame.getRefreshInterfacesButton().isEnabled()).isTrue();
        assertThat(mainFrame.getLoadSessionButton().isEnabled()).isTrue();
        assertThat(mainFrame.getOpenChartsButton().isEnabled()).isTrue();
    }

    @Test
    void constructor_groupsControlsIntoFiveTitledPanels() {
        assertThat(panelTitle(mainFrame.getCapturePanel())).isEqualTo("Captura");
        assertThat(panelTitle(mainFrame.getInterfacePanel())).isEqualTo("Interfaz");
        assertThat(panelTitle(mainFrame.getFiltersPanel())).isEqualTo("Filtros");
        assertThat(panelTitle(mainFrame.getChartsPanel())).isEqualTo("Gráficas");
        assertThat(panelTitle(mainFrame.getOutputPanel())).isEqualTo("Salida");
    }

    @Test
    void constructor_givesHeroButtonsLargestFont() {
        assertThat(mainFrame.getStartCaptureButton().getFont().getSize()).isEqualTo(20);
        assertThat(mainFrame.getStopCaptureButton().getFont().getSize()).isEqualTo(20);
        assertThat(mainFrame.getStartCaptureButton().getFont().getSize())
                .isGreaterThan(mainFrame.getRefreshInterfacesButton().getFont().getSize());
        assertThat(mainFrame.getStartCaptureButton().getPreferredSize().height)
                .isGreaterThan(mainFrame.getRefreshInterfacesButton().getPreferredSize().height);
    }

    @Test
    void constructor_usesSpanishDefaultLabels() {
        assertThat(mainFrame.getStartCaptureButton().getText()).isEqualTo("CAPTURA");
        assertThat(mainFrame.getStopCaptureButton().getText()).isEqualTo("DETENER");
        assertThat(mainFrame.getTitle()).isEqualTo("Supervisor de Red");
        assertThat(mainFrame.getSpanishLanguageMenuItem().isSelected()).isTrue();
        assertThat(mainFrame.getEnglishLanguageMenuItem().isSelected()).isFalse();
        assertThat(mainFrame.getLightThemeMenuItem().isSelected()).isTrue();
        assertThat(mainFrame.getDarkThemeMenuItem().isSelected()).isFalse();
    }

    @Test
    void applyMessages_givenEnglishLocale_relabelsEveryComponent() {
        Messages.setLocale(Locale.ENGLISH);

        mainFrame.applyMessages();

        assertThat(mainFrame.getStartCaptureButton().getText()).isEqualTo("CAPTURE");
        assertThat(mainFrame.getStopCaptureButton().getText()).isEqualTo("STOP");
        assertThat(mainFrame.getTitle()).isEqualTo("Network Supervisor");
        assertThat(mainFrame.getChartsFrame().getTitle()).isEqualTo("Traffic charts");
        assertThat(panelTitle(mainFrame.getFiltersPanel())).isEqualTo("Filters");
        assertThat(chartTitle(mainFrame.getTimeSeriesChartPanel()))
                .isEqualTo("Packets over time");
        assertThat(chartTitle(mainFrame.getProtocolPieChartPanel()))
                .isEqualTo("Protocol distribution");
        assertThat(mainFrame.getExitMenuItem().getText()).isEqualTo("Exit");
        assertThat(mainFrame.getLanguageMenu().getText()).isEqualTo("Language");
    }

    @Test
    void setDevices_givenDeviceList_populatesComboBoxAndSelectsFirstDevice() {
        DeviceInformation firstDevice = DeviceFixtures.device("eth0");
        DeviceInformation secondDevice = DeviceFixtures.device("wlan0");

        mainFrame.setDevices(List.of(firstDevice, secondDevice));

        assertThat(mainFrame.getDeviceComboBox().getItemCount()).isEqualTo(2);
        assertThat(mainFrame.getSelectedDevice()).isEqualTo(firstDevice);
        assertThat(mainFrame.getDeviceDetailsArea().getText())
                .contains("Descripción: Test eth0")
                .contains("Dirección MAC: 00:11:22:33:44:55");
    }

    @Test
    void setDevices_givenEmptyList_showsNoDevicesMessageAndSelectsNothing() {
        mainFrame.setDevices(List.of());

        assertThat(mainFrame.getSelectedDevice()).isNull();
        assertThat(mainFrame.getDeviceDetailsArea().getText())
                .isEqualTo("No hay interfaces de red disponibles.");
    }

    @Test
    void setDevices_comboBoxRendersDeviceNameOnly() {
        DeviceInformation device = DeviceFixtures.device("eth0");

        mainFrame.setDevices(List.of(device));

        JLabel renderedLabel = (JLabel) mainFrame.getDeviceComboBox().getRenderer()
                .getListCellRendererComponent(new JList<>(), device, 0, false, false);
        assertThat(renderedLabel.getText()).isEqualTo("eth0");
    }

    @Test
    void specialFilterField_hiddenUntilSpecialFilterSelected() {
        assertThat(mainFrame.isSpecialFilterFieldVisible()).isFalse();

        mainFrame.getSpecialFilterRadioButton().setSelected(true);

        assertThat(mainFrame.isSpecialFilterFieldVisible()).isTrue();
        assertThat(mainFrame.getSpecialFilterField().isEnabled()).isTrue();

        mainFrame.getFilterRadioButtons().get(0).setSelected(true);

        assertThat(mainFrame.isSpecialFilterFieldVisible()).isFalse();
    }

    @Test
    void setFilterControlsEnabled_false_disablesAllFilterControls() {
        mainFrame.getSpecialFilterRadioButton().setSelected(true);

        mainFrame.setFilterControlsEnabled(false);

        assertThat(mainFrame.getFilterRadioButtons())
                .allSatisfy(radioButton -> assertThat(radioButton.isEnabled()).isFalse());
        assertThat(mainFrame.getSpecialFilterField().isEnabled()).isFalse();

        mainFrame.setFilterControlsEnabled(true);

        assertThat(mainFrame.getFilterRadioButtons())
                .allSatisfy(radioButton -> assertThat(radioButton.isEnabled()).isTrue());
        assertThat(mainFrame.getSpecialFilterField().isEnabled()).isTrue();
    }

    @Test
    void getSelectedFilterPreset_perFilterSelection_returnsMatchingPreset() {
        assertThat(mainFrame.getSelectedFilterPreset()).isNull();

        mainFrame.getFilterRadioButtons().get(1).setSelected(true);
        assertThat(mainFrame.getSelectedFilterPreset()).isEqualTo(FilterPreset.HTTP);

        mainFrame.getFilterRadioButtons().get(2).setSelected(true);
        assertThat(mainFrame.getSelectedFilterPreset()).isEqualTo(FilterPreset.HTTPS);

        mainFrame.getFilterRadioButtons().get(3).setSelected(true);
        assertThat(mainFrame.getSelectedFilterPreset()).isEqualTo(FilterPreset.FTP);

        mainFrame.getFilterRadioButtons().get(4).setSelected(true);
        assertThat(mainFrame.getSelectedFilterPreset()).isEqualTo(FilterPreset.SMTP);

        mainFrame.getFilterRadioButtons().get(5).setSelected(true);
        assertThat(mainFrame.getSelectedFilterPreset()).isEqualTo(FilterPreset.ICMP);

        mainFrame.getFilterRadioButtons().get(6).setSelected(true);
        assertThat(mainFrame.getSelectedFilterPreset()).isEqualTo(FilterPreset.ARP);

        mainFrame.getFilterRadioButtons().get(7).setSelected(true);
        assertThat(mainFrame.getSelectedFilterPreset()).isNull();
    }

    @Test
    void getSpecialFilterExpression_trimsWhitespace() {
        mainFrame.getSpecialFilterField().setText("  tcp port 8080  ");

        assertThat(mainFrame.getSpecialFilterExpression()).isEqualTo("tcp port 8080");
        assertThat(mainFrame.isSpecialFilterSelected()).isFalse();

        mainFrame.getSpecialFilterRadioButton().setSelected(true);

        assertThat(mainFrame.isSpecialFilterSelected()).isTrue();
    }

    @Test
    void chartsFrame_constructedHiddenWithSideBySideChartPanels() {
        assertThat(mainFrame.getChartsFrame().isVisible()).isFalse();

        JPanel chartsContentPanel = (JPanel) mainFrame.getChartsFrame()
                .getContentPane().getComponent(0);
        GridLayout chartsLayout = (GridLayout) chartsContentPanel.getLayout();
        assertThat(chartsLayout.getRows()).isEqualTo(1);
        assertThat(chartsLayout.getColumns()).isEqualTo(2);
        assertThat(mainFrame.getTimeSeriesChartPanel()).isNotNull();
        assertThat(mainFrame.getProtocolPieChartPanel()).isNotNull();
    }

    @Test
    void setStatusBarTextAndSetOutputText_roundTrip() {
        mainFrame.setStatusBarText("Interfaz: eth0");
        mainFrame.setOutputText("line one\nline two");

        assertThat(mainFrame.getStatusBarText()).isEqualTo("Interfaz: eth0");
        assertThat(mainFrame.getOutputText()).isEqualTo("line one\nline two");
    }

    @Test
    void dispose_disposesChartsFrameToo() {
        mainFrame.dispose();

        assertThat(mainFrame.isDisplayable()).isFalse();
        assertThat(mainFrame.getChartsFrame().isDisplayable()).isFalse();
    }

    private static String panelTitle(JPanel panel) {
        return ((TitledBorder) panel.getBorder()).getTitle();
    }

    private static String chartTitle(JPanel chartPanel) {
        ChartPanel embeddedChartPanel = (ChartPanel) chartPanel.getComponent(0);
        return embeddedChartPanel.getChart().getTitle().getText();
    }
}
