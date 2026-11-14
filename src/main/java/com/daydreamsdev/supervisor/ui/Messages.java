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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

public final class Messages {

    private static final String BUNDLE_BASE_NAME = "messages";
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("es");

    private static volatile ResourceBundle resourceBundle = loadBundle(DEFAULT_LOCALE);

    private Messages() {
    }

    public static synchronized void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale must not be null");
        resourceBundle = loadBundle(locale);
    }

    public static String get(String key) {
        Objects.requireNonNull(key, "key must not be null");
        if (!resourceBundle.containsKey(key)) {
            throw new IllegalStateException(
                    "Message key '" + key + "' is missing from bundle " + BUNDLE_BASE_NAME);
        }
        return resourceBundle.getString(key);
    }

    private static ResourceBundle loadBundle(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
        if (!bundle.getLocale().getLanguage().equals(locale.getLanguage())) {
            throw new MissingResourceException(
                    "No message bundle for locale '" + locale + "'", BUNDLE_BASE_NAME, "");
        }
        return bundle;
    }
}
