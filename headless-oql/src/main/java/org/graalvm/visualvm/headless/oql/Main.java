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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.graalvm.visualvm.lib.jfluid.heap.Heap;
import org.graalvm.visualvm.lib.jfluid.heap.HeapFactory;
import org.graalvm.visualvm.lib.profiler.oql.engine.api.OQLEngine;
import org.graalvm.visualvm.lib.profiler.oql.engine.api.OQLException;

/**
 * Headless runner for VisualVM OQL queries against HPROF heap dumps.
 */
public final class Main {

    private static final String HPROF_HEADER = "JAVA PROFILE 1.0"; // NOI18N
    private static final long MIN_HPROF_SIZE = 1024 * 1024L;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true"); // NOI18N
        int exit = run(args);
        System.exit(exit);
    }

    static int run(String[] args) {
        try {
            Config config = parseArgs(args);
            if (!OQLEngine.isOQLSupported()) {
                System.err.println("OQL is not supported: no JavaScript engine found. "
                        + "Ensure org.openjdk.nashorn:nashorn-core is on the classpath."); // NOI18N
                return 2;
            }
            validateHeapDump(config.heapFile());
            String query = loadQuery(config);
            runQuery(config.heapFile(), query);
            return 0;
        } catch (UsageException e) {
            System.err.println(e.getMessage());
            printUsage();
            return 1;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage()); // NOI18N
            e.printStackTrace(System.err);
            return 2;
        }
    }

    private static void runQuery(File heapFile, String query) throws IOException, OQLException {
        Heap heap = HeapFactory.createHeap(heapFile);
        OQLEngine engine = new OQLEngine(heap);
        engine.executeQuery(query, o -> {
            if (o != null) {
                System.out.println(o);
            }
            return false;
        });
    }

    private static String loadQuery(Config config) throws IOException {
        if (config.inlineQuery() != null) {
            return config.inlineQuery();
        }
        return Files.readString(config.queryFile().toPath(), StandardCharsets.UTF_8);
    }

    private static void validateHeapDump(File file) throws IOException {
        if (!file.isFile() || !file.canRead()) {
            throw new IOException("Heap dump is not a readable file: " + file.getAbsolutePath()); // NOI18N
        }
        if (file.length() < MIN_HPROF_SIZE) {
            throw new IOException("Heap dump is too small (minimum 1 MiB): " + file.getAbsolutePath()); // NOI18N
        }
        byte[] prefix = new byte[HPROF_HEADER.length() + 4];
        try (var raf = new java.io.RandomAccessFile(file, "r")) { // NOI18N
            raf.readFully(prefix);
        }
        if (!new String(prefix, StandardCharsets.US_ASCII).startsWith(HPROF_HEADER)) {
            throw new IOException("Not a valid HPROF heap dump (missing JAVA PROFILE header): " // NOI18N
                    + file.getAbsolutePath());
        }
    }

    private static Config parseArgs(String[] args) {
        File heap = null;
        File queryFile = null;
        String inlineQuery = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--heap" -> {
                    heap = requireFile(args, ++i, "--heap");
                }
                case "--query" -> {
                    queryFile = requireFile(args, ++i, "--query");
                }
                case "-e", "--expression" -> {
                    inlineQuery = requireString(args, ++i, arg);
                }
                case "-h", "--help" -> {
                    throw new UsageException("");
                }
                default -> {
                    throw new UsageException("Unknown argument: " + arg); // NOI18N
                }
            }
        }

        if (heap == null) {
            throw new UsageException("Missing required --heap <file.hprof>"); // NOI18N
        }
        if (queryFile == null && inlineQuery == null) {
            throw new UsageException("Provide --query <file.oql> or -e \"<oql>\""); // NOI18N
        }
        if (queryFile != null && inlineQuery != null) {
            throw new UsageException("Use only one of --query or -e"); // NOI18N
        }
        return new Config(heap, queryFile, inlineQuery);
    }

    private static File requireFile(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new UsageException("Missing value for " + option); // NOI18N
        }
        return new File(args[index]);
    }

    private static String requireString(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new UsageException("Missing value for " + option); // NOI18N
        }
        return args[index];
    }

    private static void printUsage() {
        System.err.println("""
                headless-oql — run OQL on an HPROF heap dump (headless)

                Usage:
                  headless-oql --heap <file.hprof> --query <file.oql>
                  headless-oql --heap <file.hprof> -e "<oql expression>"

                Options:
                  --heap <path>     HPROF heap dump file (required)
                  --query <path>    File containing the OQL query
                  -e, --expression  Inline OQL query string
                  -h, --help        Show this help

                Example:
                  headless-oql --heap app.hprof --query AllFiles.oql
                  headless-oql --heap app.hprof -e "select s from java.lang.String s"
                """); // NOI18N
    }

    private record Config(File heapFile, File queryFile, String inlineQuery) {}

    private static final class UsageException extends RuntimeException {
        UsageException(String message) {
            super(message);
        }
    }
}
