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
package org.eclipse.che.plugin.gdbopenocd.server.parser;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class GdbOpenocdInfoBreakTest {

    @Test
    public void testParse() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("Num     Type           Disp Enb Address            What\n" +
                                           "1       breakpoint     keep y   0x00000000004008ca in main() at h.cpp:7\n" +
                                           "2       breakpoint     keep y   0x00000000004008ca in main() at h.cpp:8\n");

        GdbOpenocdInfoBreak gdbOpenocdInfoBreak = GdbOpenocdInfoBreak.parse(gdbOpenocdOutput);
        List<Breakpoint> breakpoints = gdbOpenocdInfoBreak.getBreakpoints();

        assertEquals(breakpoints.size(), 2);

        Breakpoint breakpoint = breakpoints.get(0);
        assertEquals(breakpoint.getLocation().getTarget(), "h.cpp");
        assertEquals(breakpoint.getLocation().getLineNumber(), 7);

        breakpoint = breakpoints.get(1);
        assertEquals(breakpoint.getLocation().getTarget(), "h.cpp");
        assertEquals(breakpoint.getLocation().getLineNumber(), 8);
    }
}
