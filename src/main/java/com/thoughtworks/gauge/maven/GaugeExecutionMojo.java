// Copyright 2015 ThoughtWorks, Inc.

// This file is part of Gauge-maven-plugin.

// Gauge-maven-plugin is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Gauge-maven-plugin is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Gauge-maven-plugin.  If not, see <http://www.gnu.org/licenses/>.

package com.thoughtworks.gauge.maven;

import com.thoughtworks.gauge.maven.exception.GaugeExecutionFailedException;
import com.thoughtworks.gauge.maven.util.Util;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal which executes gauge specs in the project
 *
 *
 */
@Mojo( name = GaugeExecutionMojo.GAUGE_EXEC_MOJO_NAME, defaultPhase = LifecyclePhase.TEST )
public class GaugeExecutionMojo
    extends AbstractMojo
{
    public static final String GAUGE_EXEC_MOJO_NAME = "execute";
    public static final String TAGS_FLAG = "--tags";
    public static final String GAUGE = "gauge";
    public static final String PARALLEL_FLAG = "--parallel";
    private static final String NODES_FLAG = "-n";

    /**
     * Gauge spec directory path.
     */
    @Parameter( defaultValue = "${gauge.specs.directory}", property = "specsDir", required = false )
    private File specsDir;

    /**
     * Tags to execute. An expression eg. tag1 & tag2 & !tag3
     */
    @Parameter(defaultValue = "${gauge.exec.tags}", property = "tags", required = false)
    private String tags;

    /**
     * Set to true to execute specs in parallel
     */
    @Parameter(defaultValue = "${gauge.exec.inParallel}", property = "inParallel", required = false)
    private Boolean inParallel;

    /**
     * Number of parallel execution nodes to run the specs in parallel in. Specify only for parallel execution
     */
    @Parameter(defaultValue = "${gauge.exec.nodes}", property = "nodes", required = false)
    private int nodes;

    /**
     * Additional flags for gauge execution
     */
    @Parameter(defaultValue = "${gauge.exec.additionalFlags}", property = "flags", required = false)
    private List flags;

    public void execute()
        throws MojoExecutionException
    {
        try {
            executeGaugeSpecs();
        } catch (GaugeExecutionFailedException e) {
            throw new MojoExecutionException("Failed to execute gauge specs. " + e.getMessage(), e);
        }

    }

    private void executeGaugeSpecs() throws GaugeExecutionFailedException {
        try {
            ProcessBuilder builder = createProcessBuilder();
            Process process = builder.start();
            Util.InheritIO(process.getInputStream(), System.out);
            Util.InheritIO(process.getErrorStream(), System.err);
            if (process.waitFor() != 0) {
                throw new GaugeExecutionFailedException();
            }
        } catch (InterruptedException e) {
            throw new GaugeExecutionFailedException(e);
        } catch (IOException e) {
            throw new GaugeExecutionFailedException(e);
        }

    }

    private ProcessBuilder createProcessBuilder() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(createGaugeCommand());
        return builder;
    }

    public ArrayList<String> createGaugeCommand() {
        ArrayList<String> command = new ArrayList<String>();
        command.add(GAUGE);
        addTags(command);
        addParallelFlags(command);
        addAdditionalFlags(command);
        addSpecsDir(command);
        return command;
    }

    private void addAdditionalFlags(ArrayList<String> command) {
        if (flags != null) {
            command.addAll(flags);
        }
    }

    private void addParallelFlags(ArrayList<String> command) {
        if(inParallel != null && inParallel) {
            command.add(PARALLEL_FLAG);
            if(nodes != 0) {
                command.add(NODES_FLAG);
                command.add(Integer.toString(nodes));
            }
        }
    }

    private void addSpecsDir(ArrayList<String> command) {
        if (this.specsDir != null) {
            command.add(this.specsDir.getAbsolutePath());
        }
    }

    private void addTags(ArrayList<String> command) {
        if(this.tags != null && !this.tags.isEmpty()) {
            command.add(TAGS_FLAG);
            command.add(tags);
        }
    }

    public File getSpecsDir() {
        return specsDir;
    }

    public String getTags() {
        return tags;
    }
}
