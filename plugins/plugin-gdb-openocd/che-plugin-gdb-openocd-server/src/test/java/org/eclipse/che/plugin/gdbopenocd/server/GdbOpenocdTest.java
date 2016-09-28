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
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdContinue;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoBreak;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoLine;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdInfoProgram;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdPType;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdPrint;
import org.eclipse.che.plugin.gdbopenocd.server.parser.GdbOpenocdRun;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class GdbOpenocdTest {

    private String file;
    private Path   sourceDirectory;
    private GdbOpenocd    gdbOpenocd;

    @BeforeClass
    public void beforeClass() throws Exception {
        file = GdbOpenocdTest.class.getResource("/hello").getFile();
        sourceDirectory = Paths.get(GdbOpenocdTest.class.getResource("/h.cpp").getFile());
    }

    @BeforeMethod
    public void setUp() throws Exception {
        gdbOpenocd = GdbOpenocd.start();
        gdbOpenocd.directory(sourceDirectory.getParent().toString());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        gdbOpenocd.stop();
    }

    @Test
    public void testInit() throws Exception {
        assertNotNull(gdbOpenocd.getGdbOpenocdVersion());
        assertNotNull(gdbOpenocd.getGdbOpenocdVersion().getName());
        assertNotNull(gdbOpenocd.getGdbOpenocdVersion().getVersion());
    }

    @Test
    public void testQuit() throws Exception {
        gdbOpenocd.quit();
    }

    @Test
    public void testFile() throws Exception {
        gdbOpenocd.file(file);
    }

    @Test
    public void testTargetRemote() throws Exception {
        GdbOpenocdServer gdbOpenocdServer = GdbOpenocdServer.start("localhost", 1111, file);

        try {
            gdbOpenocd.file(file);
            gdbOpenocd.targetRemote("localhost", 1111);

            gdbOpenocd.breakpoint(7);

            GdbOpenocdContinue gdbOpenocdContinue = gdbOpenocd.cont();

            Breakpoint breakpoint = gdbOpenocdContinue.getBreakpoint();
            assertNotNull(breakpoint);
            assertEquals(breakpoint.getLocation().getTarget(), "h.cpp");
            assertEquals(breakpoint.getLocation().getLineNumber(), 7);
        } finally {
            gdbOpenocdServer.stop();
        }
    }

    @Test(expectedExceptions = DebuggerException.class)
    public void testTargetRemoteFailWhenNoGdbOpenocdServer() throws Exception {
        gdbOpenocd.file(file);
        gdbOpenocd.targetRemote("localhost", 1111);
    }

    @Test
    public void testBreakpoints() throws Exception {
        gdbOpenocd.file(file);

        gdbOpenocd.breakpoint(7);
        gdbOpenocd.clear(7);

        gdbOpenocd.breakpoint("h.cpp", 8);
        gdbOpenocd.clear("h.cpp", 8);

        gdbOpenocd.breakpoint(7);
        gdbOpenocd.breakpoint(8);

        GdbOpenocdInfoBreak gdbOpenocdInfoBreak = gdbOpenocd.infoBreak();
        List<Breakpoint> breakpoints = gdbOpenocdInfoBreak.getBreakpoints();

        assertEquals(breakpoints.size(), 2);

        gdbOpenocd.delete();

        gdbOpenocdInfoBreak = gdbOpenocd.infoBreak();
        breakpoints = gdbOpenocdInfoBreak.getBreakpoints();

        assertTrue(breakpoints.isEmpty());
    }

    @Test
    public void testRun() throws Exception {
        gdbOpenocd.file(file);
        gdbOpenocd.breakpoint(7);

        GdbOpenocdRun gdbOpenocdRun = gdbOpenocd.run();

        assertNotNull(gdbOpenocdRun.getBreakpoint());
    }

    @Test
    public void testInfoLine() throws Exception {
        gdbOpenocd.file(file);
        gdbOpenocd.breakpoint(7);
        gdbOpenocd.run();

        GdbOpenocdInfoLine gdbOpenocdInfoLine = gdbOpenocd.infoLine();

        assertNotNull(gdbOpenocdInfoLine.getLocation());
        assertEquals(gdbOpenocdInfoLine.getLocation().getLineNumber(), 7);
        assertEquals(gdbOpenocdInfoLine.getLocation().getTarget(), "h.cpp");
    }

    @Test
    public void testStep() throws Exception {
        gdbOpenocd.file(file);
        gdbOpenocd.breakpoint(7);
        gdbOpenocd.run();

        GdbOpenocdInfoLine gdbOpenocdInfoLine = gdbOpenocd.step();
        assertNotNull(gdbOpenocdInfoLine.getLocation());

        gdbOpenocdInfoLine = gdbOpenocd.step();
        assertNotNull(gdbOpenocdInfoLine.getLocation());
    }

    @Test
    public void testNext() throws Exception {
        gdbOpenocd.file(file);
        gdbOpenocd.breakpoint(7);
        gdbOpenocd.run();

        GdbOpenocdInfoLine gdbOpenocdInfoLine = gdbOpenocd.next();

        assertNotNull(gdbOpenocdInfoLine.getLocation());
        assertEquals(gdbOpenocdInfoLine.getLocation().getLineNumber(), 5);
        assertEquals(gdbOpenocdInfoLine.getLocation().getTarget(), "h.cpp");

        gdbOpenocdInfoLine = gdbOpenocd.next();

        assertNotNull(gdbOpenocdInfoLine.getLocation());
        assertEquals(gdbOpenocdInfoLine.getLocation().getLineNumber(), 6);
        assertEquals(gdbOpenocdInfoLine.getLocation().getTarget(), "h.cpp");
    }

    @Test
    public void testVariables() throws Exception {
        gdbOpenocd.file(file);
        gdbOpenocd.breakpoint(7);
        gdbOpenocd.run();

        GdbOpenocdPrint gdbOpenocdPrint = gdbOpenocd.print("i");
        assertEquals(gdbOpenocdPrint.getValue(), "0");

        gdbOpenocd.setVar("i", "1");

        gdbOpenocdPrint = gdbOpenocd.print("i");
        assertEquals(gdbOpenocdPrint.getValue(), "1");

        GdbOpenocdPType gdbOpenocdPType = gdbOpenocd.ptype("i");
        assertEquals(gdbOpenocdPType.getType(), "int");
    }

    @Test
    public void testInfoProgram() throws Exception {
        gdbOpenocd.file(file);

        GdbOpenocdInfoProgram gdbOpenocdInfoProgram = gdbOpenocd.infoProgram();
        assertNull(gdbOpenocdInfoProgram.getStoppedAddress());

        gdbOpenocd.breakpoint(4);
        gdbOpenocd.run();

        gdbOpenocdInfoProgram = gdbOpenocd.infoProgram();
        assertNotNull(gdbOpenocdInfoProgram.getStoppedAddress());

        GdbOpenocdContinue gdbOpenocdContinue = gdbOpenocd.cont();
        assertNull(gdbOpenocdContinue.getBreakpoint());

        gdbOpenocdInfoProgram = gdbOpenocd.infoProgram();
        assertNull(gdbOpenocdInfoProgram.getStoppedAddress());
    }
}
