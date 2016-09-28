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
public class GdbOpenocdFileTest {

    @Test
    public void testParse() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("Reading symbols from hello...done.");

        GdbOpenocdFile gdbOpenocdFile = GdbOpenocdFile.parse(gdbOpenocdOutput);

        assertEquals(gdbOpenocdFile.getFile(), "hello");
    }

    @Test(expectedExceptions = GdbOpenocdParseException.class)
    public void testParseFail() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("some text");
        GdbOpenocdFile.parse(gdbOpenocdOutput);
    }
}
