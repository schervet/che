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
 * 'info program' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdInfoProgram {

    private static final Pattern GDBOPENOCD_PROGRAM_STOPPED  = Pattern.compile(".*Program stopped at (.*)[.]\n.*");
    private static final Pattern GDBOPENOCD_PROGRAM_FINISHED = Pattern.compile(".*The program being debugged is not being run.*");

    private final String address;

    public GdbOpenocdInfoProgram(String address) {
        this.address = address;
    }

    public String getStoppedAddress() {
        return address;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdInfoProgram parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_PROGRAM_FINISHED.matcher(output);
        if (matcher.find()) {
            return new GdbOpenocdInfoProgram(null);
        }

        matcher = GDBOPENOCD_PROGRAM_STOPPED.matcher(output);
        if (matcher.find()) {
            String address = matcher.group(1);
            return new GdbOpenocdInfoProgram(address);
        }


        throw new GdbOpenocdParseException(GdbOpenocdInfoProgram.class, output);
    }
}
