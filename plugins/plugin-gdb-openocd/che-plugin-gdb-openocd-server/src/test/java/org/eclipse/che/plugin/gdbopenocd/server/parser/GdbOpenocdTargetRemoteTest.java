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

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdParseException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class GdbOpenocdTargetRemoteTest {

    @Test
    public void testParse() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("Remote debugging using localhost:1111\n" +
                                           "warning: Could not load vsyscall page because no executable was specified\n" +
                                           "try using the \"file\" command first.\n" +
                                           "0x00007ffff7ddb2d0 in ?? ()\n.");

        GdbOpenocdTargetRemote gdbOpenocdTargetRemote = GdbOpenocdTargetRemote.parse(gdbOpenocdOutput);

        assertEquals(gdbOpenocdTargetRemote.getHost(), "localhost");
        assertEquals(gdbOpenocdTargetRemote.getPort(), "1111");
    }

    @Test(expectedExceptions = GdbOpenocdParseException.class)
    public void testParseFail() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("some text");
        GdbOpenocdTargetRemote.parse(gdbOpenocdOutput);
    }

    @Test(expectedExceptions = DebuggerException.class, expectedExceptionsMessageRegExp = "localhost:1223: Connection timed out.")
    public void testShouldThrowDebuggerExceptionIfConnectionTimedOut() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("localhost:1223: Connection timed out.");

        GdbOpenocdTargetRemote.parse(gdbOpenocdOutput);
    }
}
