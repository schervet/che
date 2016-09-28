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

/**
 * @author Anatoliy Bazko
 */
public class GdbOpenocdVersionTest {

    @Test
    public void testParse() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("GNU gdb (Ubuntu 7.7.1-0ubuntu5~14.04.2) 7.7.1\n" +
                                           "Copyright (C) 2014 Free Software Foundation, Inc.\n" +
                                           "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>\n" +
                                           "This is free software: you are free to change and redistribute it.\n" +
                                           "There is NO WARRANTY, to the extent permitted by law.  Type \"show copying\"\n" +
                                           "and \"show warranty\" for details.\n" +
                                           "This GDB was configured as \"x86_64-linux-gnu\".\n" +
                                           "Type \"show configuration\" for configuration details.\n" +
                                           "For bug reporting instructions, please see:\n" +
                                           "<http://www.gnu.org/software/gdb/bugs/>.\n" +
                                           "Find the GDB manual and other documentation resources online at:\n" +
                                           "<http://www.gnu.org/software/gdb/documentation/>.\n" +
                                           "For help, type \"help\".\n" +
                                           "Type \"apropos word\" to search for commands related to \"word\".\n");

        GdbOpenocdVersion gdbOpenocdVersion = GdbOpenocdVersion.parse(gdbOpenocdOutput);

        assertEquals(gdbOpenocdVersion.getVersion(), "7.7.1");
        assertEquals(gdbOpenocdVersion.getName(), "GNU gdb (Ubuntu 7.7.1-0ubuntu5~14.04.2)");
    }

    @Test(expectedExceptions = GdbOpenocdParseException.class)
    public void testParseFail() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("some text");
        GdbOpenocdVersion.parse(gdbOpenocdOutput);
    }
}
