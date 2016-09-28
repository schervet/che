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
public class GdbOpenocdDirectoryTest {

    @Test
    public void testParse() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("Source directories searched: /home/tolusha/java/gdb/sources/1:$cdir:$cwd\n");

        GdbOpenocdDirectory gdbOpenocdDirectory = GdbOpenocdDirectory.parse(gdbOpenocdOutput);

        assertEquals(gdbOpenocdDirectory.getDirectories(), "/home/tolusha/java/gdb/sources/1:$cdir:$cwd");
    }

    @Test(expectedExceptions = GdbOpenocdParseException.class)
    public void testParseFail() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("Warning: /home/tolusha/java/gdb/343: No such file or directory.\n" +
                                           "Source directories searched: /home/tolusha/java/gdb/343:$cdir:$cwd\n");
        GdbOpenocdDirectory.parse(gdbOpenocdOutput);
    }
}
