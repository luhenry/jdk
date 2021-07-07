/*
 * Copyright (c) 2013, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.jfr.event.profiling;

import java.util.List;

import jdk.jfr.Recording;
import jdk.jfr.RecordingContext;
import jdk.jfr.RecordingContextKey;
import jdk.jfr.consumer.RecordedContext;
import jdk.jfr.consumer.RecordedContextEntry;
import jdk.jfr.consumer.RecordedEvent;
import jdk.test.lib.Asserts;
import jdk.test.lib.jfr.EventNames;
import jdk.test.lib.jfr.Events;

/**
 * @test
 * @key jfr
 * @requires vm.hasJFR
 * @library /test/lib
 * @run main/othervm jdk.jfr.event.profiling.TestFullContext
 */
public class TestFullContext {

    private final static RecordingContextKey contextKey =
        RecordingContextKey.inheritableForName("contextKey");

    private static boolean success;

    public static void main(String[] args) throws Throwable {
        try (Recording recording = new Recording()) {
            // recording.enable(EventNames.ThreadStart);
            // recording.enable(EventNames.ThreadEnd);
            recording.enable("jdk.ThreadSleep");
            recording.start();

            Thread[] threads = new Thread[0];
            try (RecordingContext context = RecordingContext.where(contextKey, "contextValue").build()) {
                success = true;

                for (int i = 0; i < threads.length; ++i) {
                    threads[i] = new Thread(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                            success = false;
                        }
                    });
                    threads[i].setName("thread-" + i);
                    threads[i].start();
                }

                Thread.sleep(10);

                for (Thread thread : threads) {
                    thread.join();
                }
            }

            recording.stop();

            int threadSleepCount = 0;
            for (RecordedEvent event : fromRecording(recording)) {
                System.out.println("Event: " + event);

                RecordedContextEntry first = getFirstContextEntry(event);
                if ("contextValue".equals(first.getValue()) && "contextKey".equals(first.getName())) {
                    threadSleepCount += 1;
                    // checkEvent(event, currThread.totalDepth);
                }
            }

            if (threadSleepCount != threads.length + 1) throw new Exception("Failed to validate all threads.");
        }
    }

    public static RecordedContextEntry getFirstContextEntry(RecordedEvent event) throws Throwable {
        RecordedContext context = event.getContext();
        if (context == null) throw new Exception("no context on " + event);
        List<RecordedContextEntry> entries = context.getEntries();
        if (entries.isEmpty()) throw new Exception("empty context on " + event);
        return entries.get(0);
    }

    private static List<RecordedEvent> fromRecording(Recording recording) throws Throwable {
        return RecordingFile.readAllEvents(makeCopy(recording));
    }

    private static Path makeCopy(Recording recording) throws Throwable {
        Path p = recording.getDestination();
        if (p == null) {
            File directory = new File(".");
            // FIXME: Must come up with a way to give human-readable name
            // this will at least not clash when running parallel.
            ProcessHandle h = ProcessHandle.current();
            p = new File(directory.getAbsolutePath(), "recording-" + recording.getId() + "-pid" + h.pid() + ".jfr").toPath();
            recording.dump(p);
        }
        return p;
    }
}
