/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.testing.junit.result;

import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.testing.TestOutputEvent;

import java.io.*;

public abstract class AbstractTestResultProvider implements TestResultsProvider {
    private final File resultsDir;

    protected AbstractTestResultProvider(File resultsDir) {
        this.resultsDir = resultsDir;
    }

    public File getResultsDir() {
        return resultsDir;
    }

    protected File outputsFile(String className, TestOutputEvent.Destination destination) {
        return destination == TestOutputEvent.Destination.StdOut ? standardOutputFile(className) : standardErrorFile(className);
    }

    private File standardErrorFile(String className) {
        return new File(resultsDir, className + ".stderr");
    }

    private File standardOutputFile(String className) {
        return new File(resultsDir, className + ".stdout");
    }

    public void writeOutputs(String className, TestOutputEvent.Destination destination, Writer writer) {
        final File file = outputsFile(className, destination);
        if (!file.exists()) {
            return;
        }
        try {
            Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8");
            try {
                char[] buffer = new char[2048];
                while (true) {
                    int read = reader.read(buffer);
                    if (read < 0) {
                        return;
                    }
                    writer.write(buffer, 0, read);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
