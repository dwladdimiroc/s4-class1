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
package org.gradle.configuration;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.ProjectState;

//This one should go away once we complete the auto-apply plugins
public class ImplicitTasksConfigurer implements Action<Project>, ProjectEvaluationListener {
    public static final String HELP_GROUP = "help";
    public static final String HELP_TASK = "help";
    public static final String PROJECTS_TASK = "projects";
    public static final String TASKS_TASK = "tasks";
    public static final String PROPERTIES_TASK = "properties";
    public static final String DEPENDENCIES_TASK = "dependencies";
    public static final String DEPENDENCY_INSIGHT_TASK = "dependencyInsight";

    public void beforeEvaluate(Project project) {}

    public void afterEvaluate(Project project, ProjectState state) {
        execute(project);
    }

    public void execute(Project project) {
        project.getPlugins().apply("help-tasks");
    }
}
