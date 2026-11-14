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
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagesTest {

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    @AfterEach
    void restoreSpanishLocale() {
        Messages.setLocale(SPANISH);
    }

    @Test
    void get_defaultSpanishLocale_returnsSpanishValue() {
        Messages.setLocale(SPANISH);

        assertThat(Messages.get("app.title")).isEqualTo("Supervisor de Red");
    }

    @Test
    void get_givenEnglishLocale_returnsEnglishValue() {
        Messages.setLocale(Locale.ENGLISH);

        assertThat(Messages.get("app.title")).isEqualTo("Network Supervisor");
    }

    @Test
    void get_givenUnknownKey_throwsIllegalStateException() {
        assertThatThrownBy(() -> Messages.get("this.key.does.not.exist"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("this.key.does.not.exist");
    }

    @Test
    void setLocale_givenUnsupportedLocale_throwsMissingResourceException() {
        assertThatThrownBy(() -> Messages.setLocale(Locale.FRENCH))
                .isInstanceOf(MissingResourceException.class);
    }

    @Test
    void bundles_spanishAndEnglish_haveIdenticalKeySets() throws IOException {
        Properties spanishBundle = loadProperties("messages_es.properties");
        Properties englishBundle = loadProperties("messages_en.properties");

        assertThat(spanishBundle).isNotEmpty();
        assertThat(spanishBundle.keySet()).isEqualTo(englishBundle.keySet());
    }

    @Test
    void bundles_spanishAndEnglish_haveNoBlankValues() throws IOException {
        for (Properties bundle : new Properties[]{
                loadProperties("messages_es.properties"),
                loadProperties("messages_en.properties")}) {
            for (String key : bundle.stringPropertyNames()) {
                assertThat(bundle.getProperty(key))
                        .as("value for key '%s'", key)
                        .isNotBlank();
            }
        }
    }

    private static Properties loadProperties(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = MessagesTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertThat(inputStream).as("resource %s", resourceName).isNotNull();
            properties.load(inputStream);
        }
        return properties;
    }
}
