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

import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdParseException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Anatoliy Bazko
 */
public class GdbOpenocdBreakTest {

    @Test
    public void testParse() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("Note: breakpoint 1 also set at pc 0x4008ca.\n" +
                                           "Breakpoint 2 at 0x4008ca: file h.cpp, line 7.\n");

        GdbOpenocdBreak gdbOpenocdBreak = GdbOpenocdBreak.parse(gdbOpenocdOutput);

        assertEquals(gdbOpenocdBreak.getFile(), "h.cpp");
        assertEquals(gdbOpenocdBreak.getLineNumber(), "7");
        assertNotNull(gdbOpenocdBreak.getAddress());
    }

    @Test(expectedExceptions = GdbOpenocdParseException.class)
    public void testParseFail() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("some text");
        GdbOpenocdBreak.parse(gdbOpenocdOutput);
    }
}
