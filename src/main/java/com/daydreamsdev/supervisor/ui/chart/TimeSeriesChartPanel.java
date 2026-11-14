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
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import javax.swing.JPanel;

import com.daydreamsdev.supervisor.domain.CaptureStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public final class TimeSeriesChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int ROLLING_WINDOW_POINT_COUNT = 60;
    private static final String PACKET_COUNT_SERIES_KEY = "Packet count";

    private final Clock clock;
    private final TimeSeries packetCountSeries = new TimeSeries(PACKET_COUNT_SERIES_KEY);

    public TimeSeriesChartPanel(String title) {
        this(title, Clock.systemUTC());
    }

    TimeSeriesChartPanel(String title, Clock clock) {
        this.clock = clock;
        setLayout(new BorderLayout());
        packetCountSeries.setMaximumItemCount(ROLLING_WINDOW_POINT_COUNT);
        TimeSeriesCollection dataset = new TimeSeriesCollection(packetCountSeries);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, "Time", "Packet count", dataset, true, true, false);
        add(new ChartPanel(chart), BorderLayout.CENTER);
    }

    public void update(CaptureStatistics captureStatistics) {
        Instant currentInstant = clock.instant();
        Millisecond currentTime = new Millisecond(Date.from(currentInstant));
        packetCountSeries.addOrUpdate(currentTime, captureStatistics.totalCount());
    }

    int dataPointCount() {
        return packetCountSeries.getItemCount();
    }

    long packetCountAt(int dataPointIndex) {
        return packetCountSeries.getValue(dataPointIndex).longValue();
    }

    long latestPacketCount() {
        return packetCountAt(dataPointCount() - 1);
    }
}
