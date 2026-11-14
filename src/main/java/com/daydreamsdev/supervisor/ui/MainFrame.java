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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.daydreamsdev.supervisor.capture.DeviceAddress;
import com.daydreamsdev.supervisor.capture.DeviceInformation;
import com.daydreamsdev.supervisor.filter.FilterPreset;
import com.daydreamsdev.supervisor.ui.chart.ProtocolPieChartPanel;
import com.daydreamsdev.supervisor.ui.chart.TimeSeriesChartPanel;

public final class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final float HERO_BUTTON_FONT_SIZE = 20.0F;
    private static final int DEVICE_DETAILS_ROWS = 7;
    private static final int DEVICE_DETAILS_COLUMNS = 26;
    private static final int SPECIAL_FILTER_COLUMNS = 22;
    private static final int OUTPUT_PREFERRED_WIDTH = 520;
    private static final int OUTPUT_PREFERRED_HEIGHT = 340;
    private static final double CONTENT_SPLIT_WEIGHT = 0.35;
    private static final int LAYOUT_GAP = 8;
    private static final int INITIAL_WINDOW_WIDTH = 1180;
    private static final int INITIAL_WINDOW_HEIGHT = 760;
    private static final int MINIMUM_WINDOW_WIDTH = 960;
    private static final int MINIMUM_WINDOW_HEIGHT = 620;

    private final JButton startCaptureButton = new JButton(Messages.get("button.startCapture"));
    private final JButton stopCaptureButton = new JButton(Messages.get("button.stopCapture"));
    private final JButton refreshInterfacesButton =
            new JButton(Messages.get("button.refreshInterfaces"));
    private final JButton openChartsButton = new JButton(Messages.get("button.openCharts"));
    private final JButton clearOutputButton = new JButton(Messages.get("button.clearOutput"));
    private final JButton saveSessionButton = new JButton(Messages.get("button.saveSession"));
    private final JButton loadSessionButton = new JButton(Messages.get("button.loadSession"));

    private final JComboBox<DeviceInformation> deviceComboBox = new JComboBox<>();
    private final JTextArea deviceDetailsArea =
            new JTextArea(DEVICE_DETAILS_ROWS, DEVICE_DETAILS_COLUMNS);

    private final JRadioButton allPacketsFilter =
            new JRadioButton(Messages.get("filter.allPackets"));
    private final JRadioButton httpFilter = new JRadioButton(Messages.get("filter.http"));
    private final JRadioButton httpsFilter = new JRadioButton(Messages.get("filter.https"));
    private final JRadioButton ftpFilter = new JRadioButton(Messages.get("filter.ftp"));
    private final JRadioButton smtpFilter = new JRadioButton(Messages.get("filter.smtp"));
    private final JRadioButton icmpFilter = new JRadioButton(Messages.get("filter.icmp"));
    private final JRadioButton arpFilter = new JRadioButton(Messages.get("filter.arp"));
    private final JRadioButton specialFilterRadioButton =
            new JRadioButton(Messages.get("filter.special"));
    private final List<JRadioButton> filterRadioButtons = List.of(
            allPacketsFilter,
            httpFilter,
            httpsFilter,
            ftpFilter,
            smtpFilter,
            icmpFilter,
            arpFilter,
            specialFilterRadioButton);
    private final JTextField specialFilterField = new JTextField(SPECIAL_FILTER_COLUMNS);

    private final JTextArea outputArea = new JTextArea();
    private final JLabel statusBarLabel = new JLabel(" ");

    private final JMenu fileMenu = new JMenu(Messages.get("menu.file"));
    private final JMenu languageMenu = new JMenu(Messages.get("menu.language"));
    private final JMenu themeMenu = new JMenu(Messages.get("menu.theme"));
    private final JMenuItem exitMenuItem = new JMenuItem(Messages.get("menu.file.exit"));
    private final JRadioButtonMenuItem spanishLanguageMenuItem =
            new JRadioButtonMenuItem(Messages.get("menu.language.spanish"));
    private final JRadioButtonMenuItem englishLanguageMenuItem =
            new JRadioButtonMenuItem(Messages.get("menu.language.english"));
    private final JRadioButtonMenuItem lightThemeMenuItem =
            new JRadioButtonMenuItem(Messages.get("menu.theme.light"));
    private final JRadioButtonMenuItem darkThemeMenuItem =
            new JRadioButtonMenuItem(Messages.get("menu.theme.dark"));

    private final JPanel capturePanel = new JPanel(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));
    private final JPanel interfacePanel = new JPanel(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));
    private final JPanel filtersPanel = new JPanel();
    private final JPanel chartsPanel = new JPanel(new BorderLayout());
    private final JPanel outputPanel = new JPanel(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));

    private final JFrame chartsFrame = new JFrame(Messages.get("charts.title"));

    private TimeSeriesChartPanel timeSeriesChartPanel;
    private ProtocolPieChartPanel protocolPieChartPanel;
    private boolean filterControlsEnabled = true;
    private boolean specialFilterFieldVisible;

    public MainFrame() {
        setTitle(Messages.get("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));
        setJMenuBar(buildMenuBar());
        add(buildCapturePanel(), BorderLayout.NORTH);
        buildInterfacePanel();
        buildFiltersPanel();
        buildChartsPanel();
        JSplitPane contentSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, buildLeftColumnPanel(), buildOutputPanel());
        contentSplitPane.setResizeWeight(CONTENT_SPLIT_WEIGHT);
        add(contentSplitPane, BorderLayout.CENTER);
        statusBarLabel.setBorder(BorderFactory.createEtchedBorder());
        add(statusBarLabel, BorderLayout.SOUTH);
        buildChartsFrame();
        applyInitialButtonStates();
        pack();
        setSize(Math.max(getWidth(), INITIAL_WINDOW_WIDTH),
                Math.max(getHeight(), INITIAL_WINDOW_HEIGHT));
        setMinimumSize(new Dimension(MINIMUM_WINDOW_WIDTH, MINIMUM_WINDOW_HEIGHT));
        setLocationRelativeTo(null);
    }

    public void setDevices(List<DeviceInformation> devices) {
        deviceComboBox.setModel(new DefaultComboBoxModel<>(
                devices.toArray(new DeviceInformation[0])));
        refreshDeviceDetails();
    }

    public DeviceInformation getSelectedDevice() {
        return deviceComboBox.getItemAt(deviceComboBox.getSelectedIndex());
    }

    public boolean isSpecialFilterSelected() {
        return specialFilterRadioButton.isSelected();
    }

    public FilterPreset getSelectedFilterPreset() {
        if (httpFilter.isSelected()) {
            return FilterPreset.HTTP;
        }
        if (httpsFilter.isSelected()) {
            return FilterPreset.HTTPS;
        }
        if (ftpFilter.isSelected()) {
            return FilterPreset.FTP;
        }
        if (smtpFilter.isSelected()) {
            return FilterPreset.SMTP;
        }
        if (icmpFilter.isSelected()) {
            return FilterPreset.ICMP;
        }
        if (arpFilter.isSelected()) {
            return FilterPreset.ARP;
        }
        return null;
    }

    public String getSpecialFilterExpression() {
        return specialFilterField.getText().trim();
    }

    public void setFilterControlsEnabled(boolean enabled) {
        filterControlsEnabled = enabled;
        for (JRadioButton filterRadioButton : filterRadioButtons) {
            filterRadioButton.setEnabled(enabled);
        }
        updateSpecialFilterFieldState();
    }

    public boolean isSpecialFilterFieldVisible() {
        return specialFilterFieldVisible;
    }

    public void setStatusBarText(String statusBarText) {
        statusBarLabel.setText(statusBarText);
    }

    public String getStatusBarText() {
        return statusBarLabel.getText();
    }

    public void setOutputText(String outputText) {
        outputArea.setText(outputText);
    }

    public String getOutputText() {
        return outputArea.getText();
    }

    public void openChartsWindow() {
        if (!chartsFrame.isVisible()) {
            chartsFrame.setLocationRelativeTo(this);
        }
        chartsFrame.setVisible(true);
    }

    public void applyMessages() {
        setTitle(Messages.get("app.title"));
        fileMenu.setText(Messages.get("menu.file"));
        exitMenuItem.setText(Messages.get("menu.file.exit"));
        languageMenu.setText(Messages.get("menu.language"));
        spanishLanguageMenuItem.setText(Messages.get("menu.language.spanish"));
        englishLanguageMenuItem.setText(Messages.get("menu.language.english"));
        themeMenu.setText(Messages.get("menu.theme"));
        lightThemeMenuItem.setText(Messages.get("menu.theme.light"));
        darkThemeMenuItem.setText(Messages.get("menu.theme.dark"));
        startCaptureButton.setText(Messages.get("button.startCapture"));
        stopCaptureButton.setText(Messages.get("button.stopCapture"));
        refreshInterfacesButton.setText(Messages.get("button.refreshInterfaces"));
        openChartsButton.setText(Messages.get("button.openCharts"));
        clearOutputButton.setText(Messages.get("button.clearOutput"));
        saveSessionButton.setText(Messages.get("button.saveSession"));
        loadSessionButton.setText(Messages.get("button.loadSession"));
        allPacketsFilter.setText(Messages.get("filter.allPackets"));
        httpFilter.setText(Messages.get("filter.http"));
        httpsFilter.setText(Messages.get("filter.https"));
        ftpFilter.setText(Messages.get("filter.ftp"));
        smtpFilter.setText(Messages.get("filter.smtp"));
        icmpFilter.setText(Messages.get("filter.icmp"));
        arpFilter.setText(Messages.get("filter.arp"));
        specialFilterRadioButton.setText(Messages.get("filter.special"));
        specialFilterField.setToolTipText(Messages.get("filter.special.tooltip"));
        capturePanel.setBorder(createTitledBorder("panel.capture"));
        interfacePanel.setBorder(createTitledBorder("panel.interface"));
        filtersPanel.setBorder(createTitledBorder("panel.filters"));
        chartsPanel.setBorder(createTitledBorder("panel.charts"));
        outputPanel.setBorder(createTitledBorder("panel.output"));
        chartsFrame.setTitle(Messages.get("charts.title"));
        rebuildChartPanels();
        refreshDeviceDetails();
    }

    public JButton getStartCaptureButton() {
        return startCaptureButton;
    }

    public JButton getStopCaptureButton() {
        return stopCaptureButton;
    }

    public JButton getRefreshInterfacesButton() {
        return refreshInterfacesButton;
    }

    public JButton getOpenChartsButton() {
        return openChartsButton;
    }

    public JButton getClearOutputButton() {
        return clearOutputButton;
    }

    public JButton getSaveSessionButton() {
        return saveSessionButton;
    }

    public JButton getLoadSessionButton() {
        return loadSessionButton;
    }

    public JComboBox<DeviceInformation> getDeviceComboBox() {
        return deviceComboBox;
    }

    public JTextArea getDeviceDetailsArea() {
        return deviceDetailsArea;
    }

    public List<JRadioButton> getFilterRadioButtons() {
        return filterRadioButtons;
    }

    public JRadioButton getSpecialFilterRadioButton() {
        return specialFilterRadioButton;
    }

    public JTextField getSpecialFilterField() {
        return specialFilterField;
    }

    public JMenuItem getExitMenuItem() {
        return exitMenuItem;
    }

    public JRadioButtonMenuItem getSpanishLanguageMenuItem() {
        return spanishLanguageMenuItem;
    }

    public JRadioButtonMenuItem getEnglishLanguageMenuItem() {
        return englishLanguageMenuItem;
    }

    public JRadioButtonMenuItem getLightThemeMenuItem() {
        return lightThemeMenuItem;
    }

    public JRadioButtonMenuItem getDarkThemeMenuItem() {
        return darkThemeMenuItem;
    }

    public JMenu getFileMenu() {
        return fileMenu;
    }

    public JMenu getLanguageMenu() {
        return languageMenu;
    }

    public JMenu getThemeMenu() {
        return themeMenu;
    }

    public JFrame getChartsFrame() {
        return chartsFrame;
    }

    public TimeSeriesChartPanel getTimeSeriesChartPanel() {
        return timeSeriesChartPanel;
    }

    public ProtocolPieChartPanel getProtocolPieChartPanel() {
        return protocolPieChartPanel;
    }

    public JPanel getCapturePanel() {
        return capturePanel;
    }

    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    public JPanel getFiltersPanel() {
        return filtersPanel;
    }

    public JPanel getChartsPanel() {
        return chartsPanel;
    }

    public JPanel getOutputPanel() {
        return outputPanel;
    }

    @Override
    public void dispose() {
        chartsFrame.dispose();
        super.dispose();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        ButtonGroup languageButtonGroup = new ButtonGroup();
        languageButtonGroup.add(spanishLanguageMenuItem);
        languageButtonGroup.add(englishLanguageMenuItem);
        spanishLanguageMenuItem.setSelected(true);
        languageMenu.add(spanishLanguageMenuItem);
        languageMenu.add(englishLanguageMenuItem);
        menuBar.add(languageMenu);
        ButtonGroup themeButtonGroup = new ButtonGroup();
        themeButtonGroup.add(lightThemeMenuItem);
        themeButtonGroup.add(darkThemeMenuItem);
        lightThemeMenuItem.setSelected(true);
        themeMenu.add(lightThemeMenuItem);
        themeMenu.add(darkThemeMenuItem);
        menuBar.add(themeMenu);
        return menuBar;
    }

    private JPanel buildCapturePanel() {
        capturePanel.setBorder(createTitledBorder("panel.capture"));
        Font heroFont = startCaptureButton.getFont()
                .deriveFont(Font.BOLD, HERO_BUTTON_FONT_SIZE);
        startCaptureButton.setFont(heroFont);
        stopCaptureButton.setFont(heroFont);
        JPanel captureButtonsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        captureButtonsPanel.add(startCaptureButton);
        captureButtonsPanel.add(stopCaptureButton);
        capturePanel.add(captureButtonsPanel, BorderLayout.CENTER);
        return capturePanel;
    }

    private JPanel buildInterfacePanel() {
        interfacePanel.setBorder(createTitledBorder("panel.interface"));
        deviceComboBox.setRenderer(new DeviceComboBoxRenderer());
        deviceComboBox.addItemListener(ignoredEvent -> refreshDeviceDetails());
        interfacePanel.add(deviceComboBox, BorderLayout.NORTH);
        deviceDetailsArea.setEditable(false);
        deviceDetailsArea.setLineWrap(false);
        interfacePanel.add(new JScrollPane(deviceDetailsArea), BorderLayout.CENTER);
        interfacePanel.add(refreshInterfacesButton, BorderLayout.SOUTH);
        return interfacePanel;
    }

    private JPanel buildFiltersPanel() {
        filtersPanel.setBorder(createTitledBorder("panel.filters"));
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
        ButtonGroup filterButtonGroup = new ButtonGroup();
        for (JRadioButton filterRadioButton : filterRadioButtons) {
            filterButtonGroup.add(filterRadioButton);
            filterRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            filtersPanel.add(filterRadioButton);
        }
        allPacketsFilter.setSelected(true);
        specialFilterField.setAlignmentX(Component.LEFT_ALIGNMENT);
        specialFilterField.setToolTipText(Messages.get("filter.special.tooltip"));
        specialFilterRadioButton.addItemListener(
                ignoredEvent -> updateSpecialFilterFieldState());
        updateSpecialFilterFieldState();
        filtersPanel.add(specialFilterField);
        return filtersPanel;
    }

    private JPanel buildChartsPanel() {
        chartsPanel.setBorder(createTitledBorder("panel.charts"));
        chartsPanel.add(openChartsButton, BorderLayout.CENTER);
        return chartsPanel;
    }

    private JPanel buildLeftColumnPanel() {
        JPanel leftColumnPanel = new JPanel();
        leftColumnPanel.setLayout(new BoxLayout(leftColumnPanel, BoxLayout.Y_AXIS));
        for (JPanel panel : List.of(interfacePanel, filtersPanel, chartsPanel)) {
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftColumnPanel.add(panel);
        }
        leftColumnPanel.add(Box.createVerticalGlue());
        return leftColumnPanel;
    }

    private JPanel buildOutputPanel() {
        outputPanel.setBorder(createTitledBorder("panel.output"));
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setPreferredSize(
                new Dimension(OUTPUT_PREFERRED_WIDTH, OUTPUT_PREFERRED_HEIGHT));
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);
        JPanel outputButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        outputButtonsPanel.add(clearOutputButton);
        outputButtonsPanel.add(saveSessionButton);
        outputButtonsPanel.add(loadSessionButton);
        outputPanel.add(outputButtonsPanel, BorderLayout.SOUTH);
        return outputPanel;
    }

    private void buildChartsFrame() {
        chartsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        rebuildChartPanels();
        chartsFrame.pack();
    }

    private void rebuildChartPanels() {
        timeSeriesChartPanel =
                new TimeSeriesChartPanel(Messages.get("chart.timeSeries.title"));
        protocolPieChartPanel =
                new ProtocolPieChartPanel(Messages.get("chart.protocolPie.title"));
        JPanel chartsContentPanel = new JPanel(new GridLayout(1, 2));
        chartsContentPanel.add(timeSeriesChartPanel);
        chartsContentPanel.add(protocolPieChartPanel);
        chartsFrame.getContentPane().removeAll();
        chartsFrame.getContentPane().add(chartsContentPanel, BorderLayout.CENTER);
        chartsFrame.pack();
    }

    private void applyInitialButtonStates() {
        startCaptureButton.setEnabled(false);
        stopCaptureButton.setEnabled(false);
        saveSessionButton.setEnabled(false);
        clearOutputButton.setEnabled(false);
        refreshDeviceDetails();
    }

    private void updateSpecialFilterFieldState() {
        boolean specialFilterSelected = specialFilterRadioButton.isSelected();
        specialFilterFieldVisible = specialFilterSelected;
        specialFilterField.setVisible(specialFilterSelected);
        specialFilterField.setEnabled(filterControlsEnabled && specialFilterSelected);
    }

    private void refreshDeviceDetails() {
        DeviceInformation selectedDevice = getSelectedDevice();
        deviceDetailsArea.setText(selectedDevice == null
                ? Messages.get("interface.noDevices")
                : buildDeviceDetails(selectedDevice));
        deviceDetailsArea.setCaretPosition(0);
    }

    private String buildDeviceDetails(DeviceInformation device) {
        StringBuilder details = new StringBuilder();
        appendDetail(details, "interface.description", device.description());
        appendDetail(details, "interface.linkType", device.linkTypeName());
        appendDetail(details, "interface.macAddress", device.macAddress());
        if (!device.addresses().isEmpty()) {
            details.append(Messages.get("interface.addresses")).append('\n');
            for (DeviceAddress address : device.addresses()) {
                details.append("  ").append(formatDeviceAddress(address)).append('\n');
            }
        }
        return details.toString().stripTrailing();
    }

    private void appendDetail(StringBuilder details, String key, String value) {
        if (!value.isBlank()) {
            details.append(Messages.get(key)).append(": ").append(value).append('\n');
        }
    }

    private String formatDeviceAddress(DeviceAddress address) {
        StringBuilder formattedAddress = new StringBuilder(address.address());
        appendAddressPart(formattedAddress, "interface.subnetMask", address.subnetMask());
        appendAddressPart(
                formattedAddress, "interface.broadcastAddress", address.broadcastAddress());
        return formattedAddress.toString();
    }

    private void appendAddressPart(StringBuilder formattedAddress, String key, String value) {
        if (!value.isBlank()) {
            formattedAddress.append("  ")
                    .append(Messages.get(key)).append(": ").append(value);
        }
    }

    private static TitledBorder createTitledBorder(String key) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Messages.get(key));
    }

    private static final class DeviceComboBoxRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof DeviceInformation deviceInformation) {
                setText(deviceInformation.name());
            }
            return this;
        }
    }
}
