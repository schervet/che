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

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.SuspendAction;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.impl.DebuggerInfoImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.DisconnectEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdException;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdTerminatedException;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdContinue;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdDirectory;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoBreak;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoLine;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoProgram;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdParseException;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdPrint;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdRun;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.exists;
import static java.util.Collections.singletonList;

/**
 * Connects to GDBOpenocd.
 *
 */
public class GdbOpenocdDebugger implements Debugger {
    private static final Logger LOG                 = LoggerFactory.getLogger(GdbOpenocdDebugger.class);
    private static final int    CONNECTION_ATTEMPTS = 5;

    private final String host;
    private final int    port;
    private final String name;
    private final String version;
    private final String file;

    private Location currentLocation;

    private final GdbOpenocd              gdbOpenocd;
    private final DebuggerCallback debuggerCallback;

    GdbOpenocdDebugger(String host,
                int port,
                String name,
                String version,
                String file,
                GdbOpenocd gdbOpenocd,
                DebuggerCallback debuggerCallback) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.version = version;
        this.file = file;
        this.gdbOpenocd = gdbOpenocd;
        this.debuggerCallback = debuggerCallback;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getFile() {
        return file;
    }

    public static GdbOpenocdDebugger newInstance(String host,
                                          int port,
                                          String file,
                                          String srcDirectory,
                                          DebuggerCallback debuggerCallback) throws DebuggerException {
        if (!exists(Paths.get(file))) {
            throw new DebuggerException("Can't start GDB: binary " + file + " not found");
        }

        if (!exists(Paths.get(srcDirectory))) {
            throw new DebuggerException("Can't start GDB: source directory " + srcDirectory + " does not exist");
        }

        for (int i = 0; i < CONNECTION_ATTEMPTS - 1; i++) {
            try {
                return init(host, port, file, srcDirectory, debuggerCallback);
            } catch (DebuggerException e) {
                LOG.error("Connection attempt " + i + ": " + e.getMessage(), e);
            }
        }

        return init(host, port, file, srcDirectory, debuggerCallback);
    }

    private static GdbOpenocdDebugger init(String host,
                                    int port,
                                    String file,
                                    String srcDirectory,
                                    DebuggerCallback debuggerCallback) throws DebuggerException {

        GdbOpenocd gdbOpenocd;
        try {
            gdbOpenocd = GdbOpenocd.start();
        } catch (IOException e) {
            throw new DebuggerException("Can't start GDBOpenocd: " + e.getMessage(), e);
        }

        try {
            GdbOpenocdDirectory directory = gdbOpenocd.directory(srcDirectory);
            LOG.debug("Source directories: " + directory.getDirectories());

            gdbOpenocd.file(file);
            if (port > 0) {
                gdbOpenocd.targetRemote(host, port);
            }
        } catch (DebuggerException | IOException | InterruptedException e) {
            try {
                gdbOpenocd.quit();
            } catch (IOException | InterruptedException | GdbOpenocdException e1) {
                LOG.error("Can't stop GDB: " + e1.getMessage(), e1);
            }
            throw new DebuggerException("Can't initialize GDB: " + e.getMessage(), e);
        }

        GdbOpenocdVersion gdbOpenocdVersion = gdbOpenocd.getGdbOpenocdVersion();
        return new GdbOpenocdDebugger(host,
                               port,
                               gdbOpenocdVersion.getVersion(),
                               gdbOpenocdVersion.getName(),
                               file,
                               gdbOpenocd,
                               debuggerCallback);
    }

    @Override
    public DebuggerInfo getInfo() throws DebuggerException {
        return new DebuggerInfoImpl(host, port, name, version, 0, file);
    }

    @Override
    public void disconnect() {
        currentLocation = null;
        debuggerCallback.onEvent(new DisconnectEventImpl());

        try {
            gdbOpenocd.quit();
        } catch (IOException | InterruptedException | GdbOpenocdException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
        try {
            Location location = breakpoint.getLocation();
            if (location.getTarget() == null) {
                gdbOpenocd.breakpoint(location.getLineNumber());
            } else {
                gdbOpenocd.breakpoint(location.getTarget(), location.getLineNumber());
            }

            debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Can't add breakpoint: " + breakpoint + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBreakpoint(Location location) throws DebuggerException {
        try {
            if (location.getTarget() == null) {
                gdbOpenocd.clear(location.getLineNumber());
            } else {
                gdbOpenocd.clear(location.getTarget(), location.getLineNumber());
            }
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Can't delete breakpoint: " + location + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAllBreakpoints() throws DebuggerException {
        try {
            gdbOpenocd.delete();
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Can't delete all breakpoints. " + e.getMessage(), e);
        }
    }

    @Override
    public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
        try {
            GdbOpenocdInfoBreak gdbOpenocdInfoBreak = gdbOpenocd.infoBreak();
            return gdbOpenocdInfoBreak.getBreakpoints();
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Can't get all breakpoints. " + e.getMessage(), e);
        }
    }

    @Override
    public void start(StartAction action) throws DebuggerException {
        try {
            for (Breakpoint b : action.getBreakpoints()) {
                try {
                    addBreakpoint(b);
                } catch (DebuggerException e) {
                    // can't add breakpoint, skip it
                }
            }

            Breakpoint breakpoint;
            if (isRemoteConnection()) {
                GdbOpenocdContinue gdbOpenocdContinue = gdbOpenocd.cont();
                breakpoint = gdbOpenocdContinue.getBreakpoint();
            } else {
                GdbOpenocdRun gdbOpenocdRun = gdbOpenocd.run();
                breakpoint = gdbOpenocdRun.getBreakpoint();
            }

            if (breakpoint != null) {
                currentLocation = breakpoint.getLocation();
                debuggerCallback.onEvent(new SuspendEventImpl(breakpoint.getLocation()));
            } else {
                GdbOpenocdInfoProgram gdbOpenocdInfoProgram = gdbOpenocd.infoProgram();
                if (gdbOpenocdInfoProgram.getStoppedAddress() == null) {
                    /* TODO CHE-GdbOpenocd: with suspend, infoProgram do not exit the same way.
                                      Do not disconnect (getStoppedAddress has to be modified. */
                    //disconnect();
                }
            }
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Error during running. " + e.getMessage(), e);
        }
    }

    private boolean isRemoteConnection() {
        return getPort() > 0;
    }

    /* CHE-2508: Create "suspend" Debug action */
    @Override
    public void suspend(SuspendAction action) throws DebuggerException {
        LOG.debug("GDB OpenOCD: suspend action");
        try {
            GdbOpenocdInfoLine gdbOpenocdInfoLine = gdbOpenocd.suspend();
            if (gdbOpenocdInfoLine == null) {
                disconnect();
                return;
            }

            currentLocation = gdbOpenocdInfoLine.getLocation();
            debuggerCallback.onEvent(new SuspendEventImpl(gdbOpenocdInfoLine.getLocation()));
            
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("suspend error. " + e.getMessage(), e);
        }
    }
    
    @Override
    public void stepOver(StepOverAction action) throws DebuggerException {
        try {
            GdbOpenocdInfoLine gdbOpenocdInfoLine = gdbOpenocd.next();
            if (gdbOpenocdInfoLine == null) {
                disconnect();
                return;
            }

            currentLocation = gdbOpenocdInfoLine.getLocation();
            debuggerCallback.onEvent(new SuspendEventImpl(gdbOpenocdInfoLine.getLocation()));
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Step into error. " + e.getMessage(), e);
        }
    }

    @Override
    public void stepInto(StepIntoAction action) throws DebuggerException {
        try {
            GdbOpenocdInfoLine gdbOpenocdInfoLine = gdbOpenocd.step();
            if (gdbOpenocdInfoLine == null) {
                disconnect();
                return;
            }

            currentLocation = gdbOpenocdInfoLine.getLocation();
            debuggerCallback.onEvent(new SuspendEventImpl(gdbOpenocdInfoLine.getLocation()));
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Step into error. " + e.getMessage(), e);
        }
    }

    @Override
    public void stepOut(StepOutAction action) throws DebuggerException {
        try {
            GdbOpenocdInfoLine gdbOpenocdInfoLine = gdbOpenocd.finish();
            if (gdbOpenocdInfoLine == null) {
                disconnect();
                return;
            }

            currentLocation = gdbOpenocdInfoLine.getLocation();
            debuggerCallback.onEvent(new SuspendEventImpl(gdbOpenocdInfoLine.getLocation()));
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Step out error. " + e.getMessage(), e);
        }
    }

    @Override
    public void resume(ResumeAction action) throws DebuggerException {
        try {
            GdbOpenocdContinue gdbOpenocdContinue = gdbOpenocd.cont();
            Breakpoint breakpoint = gdbOpenocdContinue.getBreakpoint();

            if (breakpoint != null) {
                currentLocation = breakpoint.getLocation();
                debuggerCallback.onEvent(new SuspendEventImpl(breakpoint.getLocation()));
            } else {
                GdbOpenocdInfoProgram gdbOpenocdInfoProgram = gdbOpenocd.infoProgram();
                if (gdbOpenocdInfoProgram.getStoppedAddress() == null) {
                    disconnect();
                }
            }
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Resume error. " + e.getMessage(), e);
        }
    }

    @Override
    public void setValue(Variable variable) throws DebuggerException {
        try {
            List<String> path = variable.getVariablePath().getPath();
            if (path.isEmpty()) {
                throw new DebuggerException("Variable path is empty");
            }
            gdbOpenocd.setVar(path.get(0), variable.getValue());
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Can't set value for " + variable.getName() + ". " + e.getMessage(), e);
        }
    }

    @Override
    public SimpleValue getValue(VariablePath variablePath) throws DebuggerException {
        try {
            List<String> path = variablePath.getPath();
            if (path.isEmpty()) {
                throw new DebuggerException("Variable path is empty");
            }

            GdbOpenocdPrint gdbOpenocdPrint = gdbOpenocd.print(path.get(0));
            return new SimpleValueImpl(Collections.emptyList(), gdbOpenocdPrint.getValue());
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Can't get value for " + variablePath + ". " + e.getMessage(), e);
        }
    }

    @Override
    public String evaluate(String expression) throws DebuggerException {
        try {
            /* TODO CHE-GdbOpenocd: To Clean, Quick and Dirty code to output GDB Console */
            /* CHE-GdbOpenocd: output GDB console at the end of evalution action */
            String retVal;
            GdbOpenocdPrint gdbOpenocdPrint = gdbOpenocd.print(expression);
            retVal = "----Evaluate Result----\n" + gdbOpenocdPrint.getValue() + 
                     "\n----GDB Console----\n" + gdbOpenocd.gdbOpenocdConsole;
            return retVal;
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("----Evaluate Result Error----\n"  +
                                        "Can't evaluate '" + expression + "'. " +
                                        e.getMessage() +
                                        "\n----GDB Console----\n" + gdbOpenocd.gdbOpenocdConsole, e);
        }
    }

    /**
     * Dump frame.
     */
    @Override
    public StackFrameDump dumpStackFrame() throws DebuggerException {
        try {
            Map<String, String> locals = gdbOpenocd.infoLocals().getVariables();
            locals.putAll(gdbOpenocd.infoArgs().getVariables());

            List<Variable> variables = new ArrayList<>(locals.size());
            for (Map.Entry<String, String> e : locals.entrySet()) {
                String varName = e.getKey();
                String varValue = e.getValue();
                String varType;
                try {
                    varType = gdbOpenocd.ptype(varName).getType();
                } catch (GdbOpenocdParseException pe) {
                    LOG.warn(pe.getMessage(), pe);
                    varType = "";
                }

                VariablePath variablePath = new VariablePathImpl(singletonList(varName));
                VariableImpl variable = new VariableImpl(varType, varName, varValue, true, variablePath, Collections.emptyList(), true);
                variables.add(variable);
            }

            return new StackFrameDumpImpl(Collections.emptyList(), variables);
        } catch (GdbOpenocdTerminatedException e) {
            disconnect();
            throw e;
        } catch (IOException | GdbOpenocdParseException | InterruptedException e) {
            throw new DebuggerException("Can't dump stack frame. " + e.getMessage(), e);
        }
    }
}
