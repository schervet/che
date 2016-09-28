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

/**
 * @author Anatoliy Bazko
 */
public class GdbOpenocdDeleteTest {

    @Test
    public void testParse() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("Delete all breakpoints? (y or n) [answered Y; input not from terminal]\n");

        GdbOpenocdDelete.parse(gdbOpenocdOutput);
    }

    @Test(expectedExceptions = GdbOpenocdParseException.class)
    public void testParseFail() throws Exception {
        GdbOpenocdOutput gdbOpenocdOutput = GdbOpenocdOutput.of("some text");

        GdbOpenocdDelete.parse(gdbOpenocdOutput);
    }
}
