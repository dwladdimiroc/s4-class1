/*
 * Copyright 2010 the original author or authors.
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

package org.gradle.integtests.fixtures.executer;

import org.gradle.internal.Factory;
import org.gradle.internal.nativeplatform.jna.WindowsHandlesManipulator;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.launcher.daemon.registry.DaemonRegistryServices;
import org.gradle.process.internal.ExecHandleBuilder;
import org.gradle.test.fixtures.file.TestDirectoryProvider;
import org.gradle.test.fixtures.file.TestFile;

import java.io.File;
import java.util.*;

import static org.junit.Assert.fail;

class ForkingGradleExecuter extends AbstractGradleExecuter {

    public ForkingGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider) {
        super(distribution, testDirectoryProvider);
    }

    public DaemonRegistry getDaemonRegistry() {
        return new DaemonRegistryServices(getDaemonBaseDir()).get(DaemonRegistry.class);
    }

    public void assertCanExecute() throws AssertionError {
        // Can run any build
    }

    @Override
    protected List<String> getAllArgs() {
        List<String> args = new ArrayList<String>();
        args.addAll(super.getAllArgs());
        args.add("--stacktrace");
        return args;
    }

    private ExecHandleBuilder createExecHandleBuilder() {
        TestFile gradleHomeDir = getDistribution().getGradleHomeDir();
        if (!gradleHomeDir.isDirectory()) {
            fail(gradleHomeDir + " is not a directory.\n"
                    + "If you are running tests from IDE make sure that gradle tasks that prepare the test image were executed. Last time it was 'intTestImage' task.");
        }

        ExecHandleBuilder builder = new ExecHandleBuilder() {
            @Override
            public File getWorkingDir() {
                // Override this, so that the working directory is not canonicalised. Some int tests require that
                // the working directory is not canonicalised
                return ForkingGradleExecuter.this.getWorkingDir();
            }
        };

        // Override some of the user's environment
        builder.environment("GRADLE_HOME", "");
        builder.environment("JAVA_HOME", getJavaHome());
        builder.environment("GRADLE_OPTS", getGradleOptsString());
        builder.environment("JAVA_OPTS", "");

        builder.environment(getAllEnvironmentVars());
        builder.workingDir(getWorkingDir());
        builder.setStandardInput(getStdin());

        builder.args(getAllArgs());

        ExecHandlerConfigurer configurer = OperatingSystem.current().isWindows() ? new WindowsConfigurer() : new UnixConfigurer();
        configurer.configure(builder);

        getLogger().info(String.format("Execute in %s with: %s %s", builder.getWorkingDir(), builder.getExecutable(), builder.getArgs()));

        return builder;
    }

    @Override
    public GradleHandle doStart() {
        return createGradleHandle(getDefaultCharacterEncoding(), new Factory<ExecHandleBuilder>() {
            public ExecHandleBuilder create() {
                return createExecHandleBuilder();
            }
        }).start();
    }

    protected ForkingGradleHandle createGradleHandle(String encoding, Factory<ExecHandleBuilder> execHandleFactory) {
        return new ForkingGradleHandle(encoding, execHandleFactory);
    }

    protected ExecutionResult doRun() {
        return start().waitForFinish();
    }

    protected ExecutionFailure doRunWithFailure() {
        return start().waitForFailure();
    }

    @Override
    public List<String> getGradleOpts() {
        List<String> gradleOpts = new ArrayList<java.lang.String>(super.getGradleOpts());
        for (Map.Entry<String, String> entry : getImplicitJvmSystemProperties().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value.contains(" ") && !getDistribution().isSupportsSpacesInGradleAndJavaOpts()) {
                getLogger().warn("Removing '{}' from GRADLE_OPTS (value: {}) as it contains spaces, and this Gradle version ({}) cannot handle spaces in GRADLE_OPTS",
                        key, value, getDistribution().getVersion().getVersion()
                );
                continue;
            }

            gradleOpts.add(String.format("-D%s=%s", key, value));
        }

        gradleOpts.add("-ea");

        //uncomment for debugging
//        gradleOpts.add("-Xdebug");
//        gradleOpts.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");

        return gradleOpts;
    }

    @Override
    protected Map<String, String> getImplicitJvmSystemProperties() {
        Map<String, String> implicitJvmSystemProperties = new LinkedHashMap<String, String>(super.getImplicitJvmSystemProperties());
        implicitJvmSystemProperties.put("file.encoding", getDefaultCharacterEncoding());
        return implicitJvmSystemProperties;
    }

    private String getGradleOptsString() {
        StringBuilder result = new StringBuilder();
        for (String gradleOpt : getGradleOpts()) {
            if (result.length() > 0) {
                result.append(" ");
            }
            if (gradleOpt.contains(" ")) {
                assert !gradleOpt.contains("\"");
                result.append('"');
                result.append(gradleOpt);
                result.append('"');
            } else {
                result.append(gradleOpt);
            }
        }

        return result.toString();
    }

    private interface ExecHandlerConfigurer {
        void configure(ExecHandleBuilder builder);
    }

    private class WindowsConfigurer implements ExecHandlerConfigurer {
        public void configure(ExecHandleBuilder builder) {
            String cmd;
            if (getExecutable() != null) {
                cmd = getExecutable().replace('/', File.separatorChar);
            } else {
                cmd = "gradle";
            }
            builder.executable("cmd");

            List<String> allArgs = builder.getArgs();
            builder.setArgs(Arrays.asList("/c", cmd));
            builder.args(allArgs);

            String gradleHome = getDistribution().getGradleHomeDir().getAbsolutePath();

            // NOTE: Windows uses Path, but allows asking for PATH, and PATH
            //       is set within builder object for some things such
            //       as CommandLineIntegrationTest, try PATH first, and
            //       then revert to default of Path if null
            Object path = builder.getEnvironment().get("PATH");
            if (path == null) {
                path = builder.getEnvironment().get("Path");
            }
            builder.environment("Path", String.format("%s\\bin;%s", gradleHome, path));
            builder.environment("GRADLE_EXIT_CONSOLE", "true");

            getLogger().info("Initializing windows process so that child process will be fully detached...");
            new WindowsHandlesManipulator().uninheritStandardStreams();
        }
    }

    private class UnixConfigurer implements ExecHandlerConfigurer {
        public void configure(ExecHandleBuilder builder) {
            if (getExecutable() != null) {
                File exe = new File(getExecutable());
                if (exe.isAbsolute()) {
                    builder.executable(exe.getAbsolutePath());
                } else {
                    builder.executable(String.format("%s/%s", getWorkingDir().getAbsolutePath(), getExecutable()));
                }
            } else {
                builder.executable(String.format("%s/bin/gradle", getDistribution().getGradleHomeDir().getAbsolutePath()));
            }
        }
    }

}
