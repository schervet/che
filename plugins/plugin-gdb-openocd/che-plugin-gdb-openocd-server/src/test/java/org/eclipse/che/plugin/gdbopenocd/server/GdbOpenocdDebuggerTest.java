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

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.DisconnectEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOutActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOverActionImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class GdbOpenocdDebuggerTest {

    private String                       file;
    private Path                         sourceDirectory;
    private GdbOpenocdServer                    gdbOpenocdServer;
    private Debugger                     gdbOpenocdDebugger;
    private BlockingQueue<DebuggerEvent> events;

    @BeforeClass
    public void beforeClass() throws Exception {
        file = GdbOpenocdTest.class.getResource("/hello").getFile();
        sourceDirectory = Paths.get(GdbOpenocdTest.class.getResource("/h.cpp").getFile());
        events = new ArrayBlockingQueue<>(10);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        gdbOpenocdServer = GdbOpenocdServer.start("localhost", 1111, file);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        gdbOpenocdServer.stop();
    }

    @Test
    public void testDebugger() throws Exception {
        initializeDebugger();
        addBreakpoint();
        startDebugger();
        doSetAndGetValues();
//        stepInto();
        stepOver();
        stepOut();
        resume();
        deleteAllBreakpoints();
        disconnect();
    }

    private void deleteAllBreakpoints() throws DebuggerException {
        List<Breakpoint> breakpoints = gdbOpenocdDebugger.getAllBreakpoints();
        assertEquals(breakpoints.size(), 1);

        gdbOpenocdDebugger.deleteAllBreakpoints();

        breakpoints = gdbOpenocdDebugger.getAllBreakpoints();
        assertTrue(breakpoints.isEmpty());
    }

    private void resume() throws DebuggerException, InterruptedException {
        gdbOpenocdDebugger.resume(new ResumeActionImpl());

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        SuspendEvent suspendEvent = (SuspendEvent)debuggerEvent;
        assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
        assertEquals(suspendEvent.getLocation().getLineNumber(), 7);
    }

    private void stepOut() throws DebuggerException, InterruptedException {
        try {
            gdbOpenocdDebugger.stepOut(new StepOutActionImpl());
        } catch (DebuggerException e) {
            // ignore
        }

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
    }

    private void stepOver() throws DebuggerException, InterruptedException {
        gdbOpenocdDebugger.stepOver(new StepOverActionImpl());

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        SuspendEvent suspendEvent = (SuspendEvent)debuggerEvent;
        assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
        assertEquals(suspendEvent.getLocation().getLineNumber(), 5);

        gdbOpenocdDebugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        suspendEvent = (SuspendEvent)debuggerEvent;
        assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
        assertEquals(suspendEvent.getLocation().getLineNumber(), 6);

        gdbOpenocdDebugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        suspendEvent = (SuspendEvent)debuggerEvent;
        assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
        assertEquals(suspendEvent.getLocation().getLineNumber(), 7);
    }

    private void doSetAndGetValues() throws DebuggerException {
        VariablePath variablePath = new VariablePathImpl("i");
        Variable variable = new VariableImpl("int", "i", "2", true, variablePath, Collections.emptyList(), false);

        SimpleValue value = gdbOpenocdDebugger.getValue(variablePath);
        assertEquals(value.getValue(), "0");

        gdbOpenocdDebugger.setValue(variable);

        value = gdbOpenocdDebugger.getValue(variablePath);

        assertEquals(value.getValue(), "2");

        String expression = gdbOpenocdDebugger.evaluate("i");
        assertEquals(expression, "2");

        expression = gdbOpenocdDebugger.evaluate("10 + 10");
        assertEquals(expression, "20");

        StackFrameDump stackFrameDump = gdbOpenocdDebugger.dumpStackFrame();
        assertTrue(stackFrameDump.getFields().isEmpty());
        assertEquals(stackFrameDump.getVariables().size(), 1);
        assertEquals(stackFrameDump.getVariables().get(0).getName(), "i");
        assertEquals(stackFrameDump.getVariables().get(0).getValue(), "2");
        assertEquals(stackFrameDump.getVariables().get(0).getType(), "int");
    }

    private void startDebugger() throws DebuggerException, InterruptedException {
        gdbOpenocdDebugger.start(new StartActionImpl(Collections.emptyList()));

        assertEquals(events.size(), 1);

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        SuspendEvent suspendEvent = (SuspendEvent)debuggerEvent;
        assertEquals(suspendEvent.getLocation().getTarget(), "h.cpp");
        assertEquals(suspendEvent.getLocation().getLineNumber(), 7);
    }

    private void disconnect() throws DebuggerException, InterruptedException {
        gdbOpenocdDebugger.disconnect();

        assertEquals(events.size(), 1);

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof DisconnectEvent);
    }

    private void addBreakpoint() throws DebuggerException, InterruptedException {
        Location location = new LocationImpl("h.cpp", 7);
        Breakpoint breakpoint = new BreakpointImpl(location);

        gdbOpenocdDebugger.addBreakpoint(breakpoint);

        assertEquals(events.size(), 1);

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        BreakpointActivatedEvent breakpointActivatedEvent = (BreakpointActivatedEvent)debuggerEvent;
        assertEquals(breakpointActivatedEvent.getBreakpoint().getLocation().getTarget(), "h.cpp");
        assertEquals(breakpointActivatedEvent.getBreakpoint().getLocation().getLineNumber(), 7);
    }

    private void initializeDebugger() throws DebuggerException {
        Map<String, String> properties = ImmutableMap.of("host", "localhost",
                                                         "port", "1111",
                                                         "binary", file,
                                                         "sources", sourceDirectory.getParent().toString());

        GdbOpenocdDebuggerFactory gdbOpenocdDebuggerFactory = new GdbOpenocdDebuggerFactory();
        gdbOpenocdDebugger = gdbOpenocdDebuggerFactory.create(properties, events::add);


        DebuggerInfo debuggerInfo = gdbOpenocdDebugger.getInfo();

        assertEquals(debuggerInfo.getFile(), file);
        assertEquals(debuggerInfo.getHost(), "localhost");
        assertEquals(debuggerInfo.getPort(), 1111);
        assertNotNull(debuggerInfo.getName());
        assertNotNull(debuggerInfo.getVersion());
    }
}
