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

package com.daydreamsdev.supervisor.ui.chart;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import com.daydreamsdev.supervisor.domain.CaptureStatistics;
import com.daydreamsdev.supervisor.domain.ProtocolType;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public final class ProtocolPieChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final List<ProtocolType> DISPLAYED_PROTOCOLS = List.of(
            ProtocolType.TCP,
            ProtocolType.UDP,
            ProtocolType.ARP,
            ProtocolType.ICMP);

    private final DefaultPieDataset<ProtocolType> protocolDistribution = new DefaultPieDataset<>();

    public ProtocolPieChartPanel(String title) {
        setLayout(new BorderLayout());
        PiePlot<ProtocolType> piePlot = new PiePlot<>(protocolDistribution);
        piePlot.setIgnoreZeroValues(true);
        JFreeChart chart = new JFreeChart(
                title, JFreeChart.DEFAULT_TITLE_FONT, piePlot, true);
        add(new ChartPanel(chart), BorderLayout.CENTER);
    }

    public void update(CaptureStatistics captureStatistics) {
        for (ProtocolType protocolType : DISPLAYED_PROTOCOLS) {
            protocolDistribution.setValue(protocolType, captureStatistics.countFor(protocolType));
        }
    }

    int sliceCount() {
        return protocolDistribution.getItemCount();
    }

    long sliceValueFor(ProtocolType protocolType) {
        int sliceIndex = protocolDistribution.getIndex(protocolType);
        if (sliceIndex < 0) {
            return 0L;
        }
        return protocolDistribution.getValue(sliceIndex).longValue();
    }
}
