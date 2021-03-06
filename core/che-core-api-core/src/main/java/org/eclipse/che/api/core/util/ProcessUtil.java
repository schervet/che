/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helpers to manage system processes.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public final class ProcessUtil {
    private static final ProcessManager PROCESS_MANAGER = ProcessManager.newInstance();

    /**
     * Writes stdout and stderr of the process to consumers.<br>
     * Supposes that stderr of the process is redirected to stdout.
     *
     * @param p
     *         process to read output from
     * @param stdout
     *         a consumer where stdout will be redirected
     * @param stderr
     *         a consumer where stderr will be redirected
     * @throws IOException
     */
    public static void process(Process p, LineConsumer stdout, LineConsumer stderr) throws IOException {
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            String line;
            while ((line = inputReader.readLine()) != null) {
                stdout.writeLine(line);
            }
            while ((line = errorReader.readLine()) != null) {
                stderr.writeLine(line);
            }
        }
    }

    /**
     * Writes stdout of the process to consumer.<br>
     * Supposes that stderr of the process is redirected to stdout.
     *
     * @param p
     *         process to read output from
     * @param stdout
     *         a consumer where stdout will be redirected
     * @throws IOException
     */
    public static void process(Process p, LineConsumer stdout) throws IOException {
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = inputReader.readLine()) != null) {
                stdout.writeLine(line);
            }
        }
    }

    /**
     * Start the process, writing the stdout and stderr to consumer.
     *
     * @param pb
     *         process builder to start
     * @param consumer
     *         a consumer where stdout and stderr will be redirected
     * @return the started process
     * @throws IOException
     */
    public static Process execute(ProcessBuilder pb, LineConsumer consumer) throws IOException {
        pb.redirectErrorStream(true);
        Process process = pb.start();

        process(process, consumer);

        return process;
    }

    public static boolean isAlive(Process process) {
        return PROCESS_MANAGER.isAlive(process);
    }


    public static void kill(Process process) {
        PROCESS_MANAGER.kill(process);
    }
    
    /* Temp Fix CHE-2508: Add ability to call SIGINT signal in ProcessManager */
    public static void kill(Process process, int signal) {
        PROCESS_MANAGER.kill(process, signal);
    }

    public static int system(String command) {
        return PROCESS_MANAGER.system(command);
    }

    private ProcessUtil() {
    }
}