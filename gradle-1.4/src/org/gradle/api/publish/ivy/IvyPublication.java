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

package org.gradle.api.publish.ivy;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.internal.HasInternalProtocol;
import org.gradle.api.publish.Publication;

/**
 * An {@code IvyPublication} is the representation/configuration of how Gradle should publish something in Ivy format.
 *
 * <h3>The “{@code ivy-publish}” plugin and the default publication</h3>
 *
 * The “{@code ivy-publish}” plugin creates one {@code IvyPublication} named “{@code ivy}” in the project's
 * {@code publishing.publications} container. This publication is configured to publish all of the project's
 * <i>visible</i> configurations (i.e. {@link org.gradle.api.Project#getConfigurations()}).
 * <p>
 * The Ivy module identifying attributes of the publication are mapped to:
 * <ul>
 * <li>{@code module} - {@code project.name}</li>
 * <li>{@code organisation} - {@code project.group}</li>
 * <li>{@code revision} - {@code project.version}</li>
 * <li>{@code status} - {@code project.status}</li>
 * </ul>
 * <p>
 * The ability to add multiple publications and finely configure publications will be added in future Gradle versions.
 *
 * <h3>Publishing the publication</h3>
 *
 * The “{@code ivy-publish}” plugin will automatically create a {@link org.gradle.api.publish.ivy.tasks.PublishToIvyRepository} task
 * for each {@code IvyPublication} and {@link org.gradle.api.artifacts.repositories.IvyArtifactRepository} combination in
 * {@code publishing.publications} and {@code publishing.repositories} respectively.
 * <p>
 * Given the following…
 * <pre autoTested="true">
 * apply plugin: 'ivy-publish'
 *
 * publishing {
 *   repositories {
 *     ivy { url "http://my.org/repo1" }
 *     ivy {
 *       name "other"
 *       url "http://my.org/repo2"
 *     }
 *   }
 * }
 * </pre>
 *
 * The following tasks will be created automatically by the plugin:
 *
 * <ul>
 * <li>{@code publishIvyPublicationToIvyRepository} - publishes to the first repository (repository default name is “{@code ivy}”) defined</li>
 * <li>{@code publishIvyPublicationToOtherRepository} - publishes to the second repository defined</li>
 * </ul>
 *
 * These tasks are of type {@link org.gradle.api.publish.ivy.tasks.PublishToIvyRepository}. Executing the task will publish the publication
 * to the associated repository.
 *
 * <h4>The “{@code publish}” task</h4>
 *
 * The “{@code publish}” plugin (that the “{@code ivy-publish}” plugin implicitly applies) adds a lifecycle task named “{@code publish}”.
 * All {@link org.gradle.api.publish.ivy.tasks.PublishToIvyRepository} tasks added by this plugin automatically become dependencies of this
 * lifecycle task, which means that often the most convenient way to publish your project is to just run the “{@code publish}” task.
 *
 * <h4>Generating the ivy module descriptor</h4>
 *
 * A {@link org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor} task will be created for each {@code IvyPublication} in {@code publishing.publications}.
 * Each {@code GenerateIvyTask} is automatically a dependency of the respective {@code PublishToIvyRepository} task, so this task is only required for
 * generating the ivy.xml file without also publishing your module.
 *
 * <pre autoTested="true">
 * apply plugin: 'ivy-publish'

 * generateIvyModuleDescriptor {
 *     destination = file('generated-ivy.xml') // Override the default file that will contain the descriptor
 * }
 * </pre>
 *
 * @since 1.3
 */
@Incubating
@HasInternalProtocol
public interface IvyPublication extends Publication {

    /**
     * The module descriptor that will be published.
     * <p>
     *
     * @return The module descriptor that will be published.
     */
    IvyModuleDescriptor getDescriptor();

    /**
     * Configures the descriptor that will be published.
     * <p>
     * The descriptor XML can be modified by using the {@link IvyModuleDescriptor#withXml(org.gradle.api.Action)} method.
     *
     * @param configure The configuration action.
     */
    void descriptor(Action<? super IvyModuleDescriptor> configure);

}
