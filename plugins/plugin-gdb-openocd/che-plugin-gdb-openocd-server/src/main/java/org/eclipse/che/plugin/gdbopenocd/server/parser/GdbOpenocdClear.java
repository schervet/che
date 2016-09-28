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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'clear' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdClear {

    private static final Pattern GDBOPENOCD_CLEAR = Pattern.compile(".*Deleted breakpoint ([0-9]*).*");

    private GdbOpenocdClear() {
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdClear parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_CLEAR.matcher(output);
        if (matcher.find()) {
            return new GdbOpenocdClear();
        }

        throw new GdbOpenocdParseException(GdbOpenocdClear.class, output);
    }
}
