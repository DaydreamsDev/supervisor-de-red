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

package com.daydreamsdev.supervisor.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilterPresetTest {

    @Test
    void bpfExpression_http_returnsHttpPresetExpression() {
        assertThat(FilterPreset.HTTP.bpfExpression()).isEqualTo("ip and tcp and port 80");
    }

    @Test
    void bpfExpression_https_returnsHttpsPresetExpression() {
        assertThat(FilterPreset.HTTPS.bpfExpression()).isEqualTo("ip and tcp and port 443");
    }

    @Test
    void bpfExpression_ftp_returnsFtpPresetExpression() {
        assertThat(FilterPreset.FTP.bpfExpression()).isEqualTo("port 21");
    }

    @Test
    void bpfExpression_smtp_returnsSmtpPresetExpression() {
        assertThat(FilterPreset.SMTP.bpfExpression()).isEqualTo("port 25");
    }

    @Test
    void bpfExpression_icmp_returnsIcmpPresetExpression() {
        assertThat(FilterPreset.ICMP.bpfExpression()).isEqualTo("icmp");
    }

    @Test
    void bpfExpression_arp_returnsArpPresetExpression() {
        assertThat(FilterPreset.ARP.bpfExpression()).isEqualTo("arp");
    }

    @Test
    void bpfExpression_everyPresetIsNonBlank() {
        assertThat(FilterPreset.values())
                .extracting(FilterPreset::bpfExpression)
                .allSatisfy(expression -> assertThat(expression).isNotBlank());
    }
}
