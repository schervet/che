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

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdException;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdTerminatedException;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdBreak;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdClear;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdContinue;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdDelete;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdDirectory;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdFile;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoArgs;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoBreak;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoLine;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoLocals;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoProgram;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdOutput;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdPType;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdPrint;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdRun;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdTargetRemote;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

/**
 * GDBOpenocd.
 *
 */
public class GdbOpenocd extends GdbOpenocdProcess {
    private static final Logger LOG              = LoggerFactory.getLogger(GdbOpenocdProcess.class);
    private static final String PROCESS_NAME     = "arm-none-eabi-gdb";
    private static final String OUTPUT_SEPARATOR = "(gdb) ";

    private GdbOpenocdVersion gdbOpenocdVersion;
    
    /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code for On The Fly Breakpoint */
    private volatile boolean gdbOpenocdIsRunning = false;
 
    /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code to output GDB Console */
    public volatile String gdbOpenocdConsole = "";

    GdbOpenocd() throws IOException {
        super(OUTPUT_SEPARATOR, PROCESS_NAME);

        try {
            gdbOpenocdConsole = "";
            gdbOpenocdVersion = GdbOpenocdVersion.parse(grabGdbOpenocdOutput());
        } catch (InterruptedException | DebuggerException e) {
            LOG.error(e.getMessage(), e);
            gdbOpenocdVersion = new GdbOpenocdVersion("Unknown", "Unknown");
        }
    }

    /**
     * Starts GDBOpenocd.
     */
    public static GdbOpenocd start() throws IOException {
        return new GdbOpenocd();
    }

    public GdbOpenocdVersion getGdbOpenocdVersion() {
        return gdbOpenocdVersion;
    }

    /**
     * `run` command.
     */
    public GdbOpenocdRun run() throws IOException, InterruptedException, DebuggerException {
        /* CHE-GdbOpenocd: specific command for OpenOCD */
        sendCommand("monitor reset halt");
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("continue");
        /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code for On The Fly Breakpoint */
        while(false == gdbOpenocdIsRunning){
            /* CTRL-C received to enter new On The Fly breakpoint. resume again */
            Thread.sleep(200);
            gdbOpenocdIsRunning = true;
            gdbOpenocdOutput = sendCommand("continue");
        }
        gdbOpenocdIsRunning = false;
        return GdbOpenocdRun.parse(gdbOpenocdOutput);
    }
    
    /**
     * `suspend` command.
     */
    public GdbOpenocdInfoLine suspend() throws IOException, InterruptedException, DebuggerException {
        if(true == gdbOpenocdIsRunning){
            kill(2); /* send CTRL-C to stop GDB */
            sendCommand("monitor halt");
        }
        else{
            /* call infoLine twice will result in display current location + 1 */
            sendCommand("info line -1");
        }
        return infoLine();
    }

    /**
     * `set var` command.
     */
    public void setVar(String varName, String value) throws IOException, InterruptedException, DebuggerException {
        String command = "set var " + varName + "=" + value;
        sendCommand(command);
    }

    /**
     * `ptype` command.
     */
    public GdbOpenocdPType ptype(String variable) throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("ptype " + variable);
        return GdbOpenocdPType.parse(gdbOpenocdOutput);
    }

    /**
     * `print` command.
     */
    public GdbOpenocdPrint print(String variable) throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("print " + variable);
        return GdbOpenocdPrint.parse(gdbOpenocdOutput);
    }

    /**
     * `continue` command.
     */
    public GdbOpenocdContinue cont() throws IOException, InterruptedException, DebuggerException {
        /* CHE-GdbOpenocd: specific command for OpenOCD */
        gdbOpenocdIsRunning = true;
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("continue");
        /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code for On The Fly Breakpoint */
        while(false == gdbOpenocdIsRunning){
            /* CTRL-C received to enter new On The Fly breakpoint. resume again */
            Thread.sleep(200);
            gdbOpenocdIsRunning = true;
            gdbOpenocdOutput = sendCommand("continue");
        }
        gdbOpenocdIsRunning = false;
        return GdbOpenocdContinue.parse(gdbOpenocdOutput);
    }

    /**
     * `step` command.
     */
    public GdbOpenocdInfoLine step() throws IOException, InterruptedException, DebuggerException {
        sendCommand("step");
        return infoLine();
    }

    /**
     * `finish` command.
     */
    public GdbOpenocdInfoLine finish() throws IOException, InterruptedException, DebuggerException {
        sendCommand("finish");
        return infoLine();
    }

    /**
     * `next` command.
     */
    @Nullable
    public GdbOpenocdInfoLine next() throws IOException, InterruptedException, DebuggerException {
        sendCommand("next");

        GdbOpenocdInfoProgram gdbOpenocdInfoProgram = infoProgram();
        if (gdbOpenocdInfoProgram.getStoppedAddress() == null) {
            return null;
        }

        return infoLine();
    }

    /**
     * `quit` command.
     */
    public void quit() throws IOException, GdbOpenocdException, InterruptedException {
        try {
            sendCommand("quit", false);
        } finally {
            stop();
        }
    }

    /**
     * `break` command
     */
    public void breakpoint(@NotNull String file, int lineNumber) throws IOException,
                                                                        InterruptedException,
                                                                        DebuggerException {
        String command = "break " + file + ":" + lineNumber;
        /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code for On The Fly Breakpoint */
        /* CHE-GdbOpenocd: stop GDB to take new command */
        if(true == gdbOpenocdIsRunning){
            /* CHE-GdbOpenocd: Notify to call back "continue" after breakpoint set */
            gdbOpenocdIsRunning = false;
            kill(2); /* send CTRL-C to stop GDB */
        }
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand(command);
        GdbOpenocdBreak.parse(gdbOpenocdOutput);
    }

    /**
     * `break` command
     */
    public void breakpoint(int lineNumber) throws IOException, InterruptedException, DebuggerException {
        String command = "break " + lineNumber;
        /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code for On The Fly Breakpoint */
        /* CHE-GdbOpenocd: stop GDB to take new command */
        if(true == gdbOpenocdIsRunning){
            /* CHE-GdbOpenocd: Notify to call back "continue" after breakpoint set */
            gdbOpenocdIsRunning = false;
            kill(2); /* send CTRL-C to stop GDB */
        }
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand(command);
        GdbOpenocdBreak.parse(gdbOpenocdOutput);
    }

    /**
     * `directory` command.
     */
    public GdbOpenocdDirectory directory(@NotNull String directory) throws IOException,
                                                                    InterruptedException,
                                                                    DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("directory " + directory);
        return GdbOpenocdDirectory.parse(gdbOpenocdOutput);
    }

    /**
     * `file` command.
     */
    public void file(@NotNull String file) throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("file " + file);
        GdbOpenocdFile.parse(gdbOpenocdOutput);
    }

    /**
     * `clear` command.
     */
    public void clear(@NotNull String file, int lineNumber) throws IOException, InterruptedException, DebuggerException {
        String command = "clear " + file + ":" + lineNumber;
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand(command);

        GdbOpenocdClear.parse(gdbOpenocdOutput);
    }

    /**
     * `clear` command.
     */
    public void clear(int lineNumber) throws IOException, InterruptedException, DebuggerException {
        String command = "clear " + lineNumber;
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand(command);

        GdbOpenocdClear.parse(gdbOpenocdOutput);
    }

    /**
     * `delete` command.
     */
    public void delete() throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("delete");
        GdbOpenocdDelete.parse(gdbOpenocdOutput);
    }

    /**
     * `target remote` command.
     */
    public void targetRemote(String host, int port) throws IOException, InterruptedException, DebuggerException {
        String command = "target remote " + (host != null ? host : "") + ":" + port;
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand(command);
        /* CHE-GdbOpenocd: specific command for OpenOCD */
        sendCommand("monitor reset halt");
        Thread.sleep(1000);
        sendCommand("load");
        Thread.sleep(1000);
        sendCommand("monitor reset halt");

        GdbOpenocdTargetRemote.parse(gdbOpenocdOutput);
    }

    /**
     * `info break` command.
     */
    public GdbOpenocdInfoBreak infoBreak() throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("info break");
        return GdbOpenocdInfoBreak.parse(gdbOpenocdOutput);
    }

    /**
     * `info args` command.
     */
    public GdbOpenocdInfoArgs infoArgs() throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("info args");
        return GdbOpenocdInfoArgs.parse(gdbOpenocdOutput);
    }

    /**
     * `info locals` command.
     */
    public GdbOpenocdInfoLocals infoLocals() throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("info locals");
        return GdbOpenocdInfoLocals.parse(gdbOpenocdOutput);
    }

    /**
     * `info line` command.
     */
    public GdbOpenocdInfoLine infoLine() throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("info line");
        return GdbOpenocdInfoLine.parse(gdbOpenocdOutput);
    }

    /**
     * `info program` command.
     */
    public GdbOpenocdInfoProgram infoProgram() throws IOException, InterruptedException, DebuggerException {
        GdbOpenocdOutput gdbOpenocdOutput = sendCommand("info program");
        return GdbOpenocdInfoProgram.parse(gdbOpenocdOutput);
    }

    private GdbOpenocdOutput sendCommand(String command) throws IOException,
                                                         GdbOpenocdTerminatedException,
                                                         InterruptedException {
        
        return sendCommand(command, true);
    }

    private synchronized GdbOpenocdOutput sendCommand(String command, boolean grabOutput) throws IOException,
                                                                                          GdbOpenocdTerminatedException,
                                                                                          InterruptedException {
        LOG.debug(command);
        GdbOpenocdOutput ret;

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(command);
        writer.newLine();
        writer.flush();

        return grabOutput ? grabGdbOpenocdOutput() : null;
    }

    private GdbOpenocdOutput grabGdbOpenocdOutput() throws IOException, InterruptedException, GdbOpenocdTerminatedException {
        /* CHE-GdbOpenocd: call outputs in polling mode to avoid lock of other process while debugger running */
        GdbOpenocdOutput gdbOpenocdOutput = null;
        while(gdbOpenocdOutput == null){
            gdbOpenocdOutput = outputs.poll(10, TimeUnit.MILLISECONDS);
        }

        if (gdbOpenocdOutput.isTerminated()) {
            String errorMsg = "GDB has been terminated with output: " + gdbOpenocdOutput.getOutput();
            LOG.error(errorMsg);
            throw new GdbOpenocdTerminatedException(errorMsg);
        }

        /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code to output GDB Console */
        /* CHE-GdbOpenocd: Add output of GDB console */
        gdbOpenocdConsole = gdbOpenocdConsole + gdbOpenocdOutput.getOutput();
        return gdbOpenocdOutput;
    }
}
