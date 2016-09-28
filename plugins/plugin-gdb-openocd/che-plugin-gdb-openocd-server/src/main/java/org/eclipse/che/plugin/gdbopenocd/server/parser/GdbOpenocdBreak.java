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
 * 'break' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdBreak {

    private static final Pattern GDBOPENOCD_BREAK = Pattern.compile(".*Breakpoint ([0-9]*) at (.*): file (.*), line ([0-9]*).*");

    private final String address;
    private final String file;
    private final String lineNumber;

    private GdbOpenocdBreak(String address, String file, String lineNumber) {
        this.address = address;
        this.file = file;
        this.lineNumber = lineNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getFile() {
        return file;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdBreak parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_BREAK.matcher(output);
        if (matcher.find()) {
            String address = matcher.group(2);
            String file = matcher.group(3);
            String lineNumber = matcher.group(4);
            return new GdbOpenocdBreak(address, file, lineNumber);
        }

        throw new GdbOpenocdParseException(GdbOpenocdBreak.class, output);
    }
}
