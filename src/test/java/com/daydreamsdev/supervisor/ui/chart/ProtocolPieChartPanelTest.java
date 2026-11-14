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

import java.util.Map;

import com.daydreamsdev.supervisor.domain.CaptureStatistics;
import com.daydreamsdev.supervisor.domain.ProtocolType;
import org.jfree.chart.ChartPanel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProtocolPieChartPanelTest {

    @Test
    void update_givenCaptureStats_setsFourSlicesFromProtocolCounts() {
        ProtocolPieChartPanel chartPanel = new ProtocolPieChartPanel("Protocol distribution");
        CaptureStatistics captureStatistics = CaptureStatistics.of(Map.of(
                ProtocolType.TCP, 3L,
                ProtocolType.UDP, 2L,
                ProtocolType.ARP, 1L,
                ProtocolType.ICMP, 5L));

        chartPanel.update(captureStatistics);

        assertThat(chartPanel.sliceCount()).isEqualTo(4);
        assertThat(chartPanel.sliceValueFor(ProtocolType.TCP)).isEqualTo(3L);
        assertThat(chartPanel.sliceValueFor(ProtocolType.UDP)).isEqualTo(2L);
        assertThat(chartPanel.sliceValueFor(ProtocolType.ARP)).isEqualTo(1L);
        assertThat(chartPanel.sliceValueFor(ProtocolType.ICMP)).isEqualTo(5L);
    }

    @Test
    void update_ignoresOtherProtocolCount() {
        ProtocolPieChartPanel chartPanel = new ProtocolPieChartPanel("Protocol distribution");

        chartPanel.update(CaptureStatistics.of(Map.of(
                ProtocolType.TCP, 1L,
                ProtocolType.OTHER, 99L)));

        assertThat(chartPanel.sliceCount()).isEqualTo(4);
        assertThat(chartPanel.sliceValueFor(ProtocolType.OTHER)).isZero();
    }

    @Test
    void update_givenZeroCounts_keepsSlicesAsZero() {
        ProtocolPieChartPanel chartPanel = new ProtocolPieChartPanel("Protocol distribution");

        chartPanel.update(CaptureStatistics.of(Map.of(
                ProtocolType.TCP, 3L,
                ProtocolType.UDP, 0L)));

        assertThat(chartPanel.sliceCount()).isEqualTo(4);
        assertThat(chartPanel.sliceValueFor(ProtocolType.UDP)).isZero();
        assertThat(chartPanel.sliceValueFor(ProtocolType.ICMP)).isZero();
    }

    @Test
    void update_givenSecondSnapshot_replacesPreviousSliceValues() {
        ProtocolPieChartPanel chartPanel = new ProtocolPieChartPanel("Protocol distribution");

        chartPanel.update(CaptureStatistics.of(Map.of(ProtocolType.TCP, 3L, ProtocolType.UDP, 9L)));
        chartPanel.update(CaptureStatistics.of(Map.of(ProtocolType.TCP, 1L)));

        assertThat(chartPanel.sliceValueFor(ProtocolType.TCP)).isEqualTo(1L);
        assertThat(chartPanel.sliceValueFor(ProtocolType.UDP)).isZero();
    }

    @Test
    void update_givenEmptyStatistics_setsAllSlicesToZero() {
        ProtocolPieChartPanel chartPanel = new ProtocolPieChartPanel("Protocol distribution");

        chartPanel.update(CaptureStatistics.empty());

        assertThat(chartPanel.sliceCount()).isEqualTo(4);
        assertThat(chartPanel.sliceValueFor(ProtocolType.TCP)).isZero();
        assertThat(chartPanel.sliceValueFor(ProtocolType.UDP)).isZero();
        assertThat(chartPanel.sliceValueFor(ProtocolType.ARP)).isZero();
        assertThat(chartPanel.sliceValueFor(ProtocolType.ICMP)).isZero();
    }

    @Test
    void constructor_givenTitle_setsChartTitleOnEmbeddedChart() {
        ProtocolPieChartPanel chartPanel = new ProtocolPieChartPanel("Protocol distribution");

        ChartPanel embeddedChartPanel = (ChartPanel) chartPanel.getComponent(0);
        assertThat(embeddedChartPanel.getChart().getTitle().getText())
                .isEqualTo("Protocol distribution");
    }

    @Test
    void constructor_embedsChartPanelForDisplay() {
        ProtocolPieChartPanel chartPanel = new ProtocolPieChartPanel("Protocol distribution");

        assertThat(chartPanel.getComponentCount()).isEqualTo(1);
        assertThat(chartPanel.getComponent(0)).isInstanceOf(ChartPanel.class);
    }
}
