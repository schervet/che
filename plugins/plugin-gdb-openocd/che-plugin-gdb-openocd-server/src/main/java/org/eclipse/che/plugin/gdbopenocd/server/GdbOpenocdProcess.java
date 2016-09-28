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
package org.eclipse.che.plugin.gdbopenocd.server;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import java.util.concurrent.TimeUnit;

import org.eclipse.che.api.core.util.ProcessUtil;

import static java.lang.Math.min;


public abstract class GdbOpenocdProcess {
    private static final Logger LOG          = LoggerFactory.getLogger(GdbOpenocdProcess.class);
    private static final int    MAX_CAPACITY = 1000;
    private static final int    MAX_OUTPUT   = 4096;

    protected final Process                  process;
    protected final String                   outputSeparator;
    protected final BlockingQueue<GdbOpenocdOutput> outputs;
    protected final Thread                   outputReader;

    public GdbOpenocdProcess(String outputSeparator, String... commands) throws IOException {
        this.outputSeparator = outputSeparator;
        this.outputs = new ArrayBlockingQueue<>(MAX_CAPACITY);

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        process = processBuilder.start();

        outputReader = new OutputReader(commands[0] + " output reader");
        
        outputReader.setDaemon(true);
        outputReader.start();
        
    }

    /**
     * Stops process.
     */
    protected void stop() {
        outputReader.interrupt();
        outputs.clear();
        process.destroyForcibly();
    }

    /* CHE-2508: add SIGINT */
    /** Call kill with needed signal:
     *   - SIGINT = 2 (same as Ctrl-C)
     *   - SIGKILL =9 (use process.destroy() instead) */
    public void kill(int signal) {
        ProcessUtil.kill(process, signal);
    }
    
    /**
     * Continuously reads process output and store in the {@code #outputs}.
     */
    private class OutputReader extends Thread {

        public OutputReader(String name) {
            super(name);
        }

        @Override
        public void run() {
            StringBuilder buf = new StringBuilder();

            while (!isInterrupted()) {
                if (!process.isAlive()) {
                    outputs.add(GdbOpenocdOutput.of(buf.toString(), true));
                    break;
                }

                try {
                    InputStream in = getInput();
                    if (in != null) {
                        String data = read(in);
                        if (!data.isEmpty()) {
                            buf.append(data);
                            if (buf.length() > MAX_OUTPUT) {
                                buf.delete(0, buf.length() - MAX_OUTPUT);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }

                if (buf.length() > 0) {
                    extractOutput(buf);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
                
                /* TODO CHE-GdbOpenocd: call gdb in polling mode to 
                        avoid lock of other process while debugger running */
                try{
                    if(process.waitFor(100, TimeUnit.MILLISECONDS)){
                        /* gdb exited */
                    } else{
                        /* Loop */
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                    break;
                }
            }

            LOG.debug(getName() + " has been stopped");
        }

        private InputStream getInput() throws IOException {
            return hasError() ? process.getErrorStream()
                              : (hasInput() ? (hasError() ? process.getErrorStream() : process.getInputStream())
                                            : null);
        }

        private void extractOutput(StringBuilder buf) {
            int indexOf;
            while ((indexOf = buf.indexOf(outputSeparator)) >= 0) {
                GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of(buf.substring(0, indexOf));
                outputs.add(gdbOpenocdOutput);

                LOG.debug(gdbOpenocdOutput.getOutput());

                buf.delete(0, indexOf + outputSeparator.length());
            }
        }

        private boolean hasError() throws IOException {
            return process.getErrorStream().available() != 0;
        }

        private boolean hasInput() throws IOException {
            return process.getInputStream().available() != 0;
        }

        @Nullable
        private String read(InputStream in) throws IOException {
            int available = min(in.available(), MAX_OUTPUT);
            byte[] buf = new byte[available];
            int read = in.read(buf, 0, available);

            return new String(buf, 0, read, StandardCharsets.UTF_8);
        }
    }

}
