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

import org.gradle.api.tasks.testing.TestResult;

import java.util.List;

/**
 * by Szczepan Faber, created at: 11/13/12
 */
public class TestMethodResult {
    private final String name;
    private final TestResult.ResultType resultType;
    private final long duration;
    private final long endTime;
    private final List<Throwable> exceptions;

    public TestMethodResult(String name, TestResult result) {
        this.name = name;
        resultType = result.getResultType();
        duration = result.getEndTime() - result.getStartTime();
        endTime = result.getEndTime();
        exceptions = result.getExceptions();
    }

    public TestMethodResult(String name, TestResult.ResultType resultType, long duration, long endTime, List<Throwable> exceptions) {
        this.name = name;
        this.resultType = resultType;
        this.duration = duration;
        this.endTime = endTime;
        this.exceptions = exceptions;
    }

    public String getName() {
        return name;
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    public TestResult.ResultType getResultType() {
        return resultType;
    }

    public long getDuration() {
        return duration;
    }

    public long getEndTime() {
        return endTime;
    }
}