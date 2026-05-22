/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.graalvm.visualvm.headless.oql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.graalvm.visualvm.lib.profiler.oql.engine.api.OQLEngine;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void runsOqlOnSampleHeap() throws Exception {
        assumeTrue(OQLEngine.isOQLSupported(), "JavaScript engine not available");

        URL heapUrl = getClass().getResource("small_heap.bin");
        assumeTrue(heapUrl != null, "small_heap.bin test resource missing");

        PrintStream prevOut = System.out;
        PrintStream prevErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
            int code = Main.run(new String[] {
                "--heap", heapUrl.toURI().getPath(),
                "-e", "select heap.classes().length"
            });
            assertEquals(0, code, () -> err.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(prevOut);
            System.setErr(prevErr);
        }
    }

    @Test
    void missingHeapReturnsUsageError() {
        assertEquals(1, Main.run(new String[] { "-e", "select 1" }));
    }
}
