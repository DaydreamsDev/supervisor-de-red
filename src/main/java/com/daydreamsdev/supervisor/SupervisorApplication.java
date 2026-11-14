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

package com.daydreamsdev.supervisor;

import javax.swing.SwingUtilities;

import com.daydreamsdev.supervisor.capture.Pcap4jCaptureService;
import com.daydreamsdev.supervisor.io.JsonSessionStore;
import com.daydreamsdev.supervisor.ui.MainFrame;
import com.daydreamsdev.supervisor.ui.SupervisorController;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public final class SupervisorApplication {

    private static final String DARK_THEME_ARGUMENT = "--dark";

    private SupervisorApplication() {
    }

    public static void main(String[] arguments) {
        applyLookAndFeel(requestsDarkTheme(arguments));
        SwingUtilities.invokeLater(SupervisorApplication::buildInterface);
    }

    private static void buildInterface() {
        MainFrame mainFrame = new MainFrame();
        SupervisorController supervisorController = new SupervisorController(
                mainFrame, new Pcap4jCaptureService(), new JsonSessionStore());
        supervisorController.start();
        mainFrame.setVisible(true);
    }

    static void applyLookAndFeel(boolean darkTheme) {
        if (darkTheme) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    private static boolean requestsDarkTheme(String[] arguments) {
        for (String argument : arguments) {
            if (DARK_THEME_ARGUMENT.equals(argument)) {
                return true;
            }
        }
        return false;
    }
}
