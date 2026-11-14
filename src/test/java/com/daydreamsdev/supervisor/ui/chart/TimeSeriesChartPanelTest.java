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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import com.daydreamsdev.supervisor.domain.CaptureStatistics;
import com.daydreamsdev.supervisor.domain.ProtocolType;
import org.jfree.chart.ChartPanel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSeriesChartPanelTest {

    private static final Instant CAPTURE_START_INSTANT = Instant.parse("2026-11-14T10:00:00Z");
    private static final int ROLLING_WINDOW_POINT_COUNT = 60;

    @Test
    void update_givenCaptureStats_appendsOnePointWithTotalCount() {
        AdjustableClock adjustableClock = new AdjustableClock(CAPTURE_START_INSTANT);
        TimeSeriesChartPanel chartPanel = new TimeSeriesChartPanel(
                "Packets over time", adjustableClock);
        CaptureStatistics captureStatistics = CaptureStatistics.of(Map.of(
                ProtocolType.TCP, 3L,
                ProtocolType.UDP, 2L));

        chartPanel.update(captureStatistics);

        assertThat(chartPanel.dataPointCount()).isEqualTo(1);
        assertThat(chartPanel.latestPacketCount()).isEqualTo(5L);
    }

    @Test
    void update_givenTwoSnapshotsAtDifferentInstants_appendsBothPointsInOrder() {
        AdjustableClock adjustableClock = new AdjustableClock(CAPTURE_START_INSTANT);
        TimeSeriesChartPanel chartPanel = new TimeSeriesChartPanel(
                "Packets over time", adjustableClock);

        chartPanel.update(CaptureStatistics.of(Map.of(ProtocolType.TCP, 3L)));
        adjustableClock.advanceBy(Duration.ofMillis(1));
        chartPanel.update(CaptureStatistics.of(Map.of(ProtocolType.TCP, 3L, ProtocolType.ARP, 2L)));

        assertThat(chartPanel.dataPointCount()).isEqualTo(2);
        assertThat(chartPanel.packetCountAt(0)).isEqualTo(3L);
        assertThat(chartPanel.packetCountAt(1)).isEqualTo(5L);
    }

    @Test
    void update_givenSecondSnapshotInSameMillisecond_replacesLatestPoint() {
        AdjustableClock adjustableClock = new AdjustableClock(CAPTURE_START_INSTANT);
        TimeSeriesChartPanel chartPanel = new TimeSeriesChartPanel(
                "Packets over time", adjustableClock);

        chartPanel.update(CaptureStatistics.of(Map.of(ProtocolType.TCP, 1L)));
        chartPanel.update(CaptureStatistics.of(Map.of(ProtocolType.TCP, 4L)));

        assertThat(chartPanel.dataPointCount()).isEqualTo(1);
        assertThat(chartPanel.latestPacketCount()).isEqualTo(4L);
    }

    @Test
    void update_givenMoreUpdatesThanWindowSize_keepsOnlyLatestWindowPoints() {
        AdjustableClock adjustableClock = new AdjustableClock(CAPTURE_START_INSTANT);
        TimeSeriesChartPanel chartPanel = new TimeSeriesChartPanel(
                "Packets over time", adjustableClock);
        int totalUpdateCount = ROLLING_WINDOW_POINT_COUNT + 5;

        for (int updateIndex = 0; updateIndex < totalUpdateCount; updateIndex++) {
            chartPanel.update(CaptureStatistics.of(
                    Map.of(ProtocolType.TCP, (long) updateIndex + 1)));
            adjustableClock.advanceBy(Duration.ofMillis(1));
        }

        assertThat(chartPanel.dataPointCount()).isEqualTo(ROLLING_WINDOW_POINT_COUNT);
        assertThat(chartPanel.packetCountAt(0)).isEqualTo(6L);
        assertThat(chartPanel.latestPacketCount()).isEqualTo(65L);
    }

    @Test
    void constructor_givenTitle_setsChartTitleOnEmbeddedChart() {
        TimeSeriesChartPanel chartPanel = new TimeSeriesChartPanel("Packets over time");

        ChartPanel embeddedChartPanel = (ChartPanel) chartPanel.getComponent(0);
        assertThat(embeddedChartPanel.getChart().getTitle().getText())
                .isEqualTo("Packets over time");
    }

    @Test
    void constructor_embedsChartPanelForDisplay() {
        TimeSeriesChartPanel chartPanel = new TimeSeriesChartPanel("Packets over time");

        assertThat(chartPanel.getComponentCount()).isEqualTo(1);
        assertThat(chartPanel.getComponent(0)).isInstanceOf(ChartPanel.class);
    }

    private static final class AdjustableClock extends Clock {

        private Instant currentInstant;

        private AdjustableClock(Instant initialInstant) {
            this.currentInstant = initialInstant;
        }

        private void advanceBy(Duration duration) {
            this.currentInstant = this.currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return this.currentInstant;
        }
    }
}
