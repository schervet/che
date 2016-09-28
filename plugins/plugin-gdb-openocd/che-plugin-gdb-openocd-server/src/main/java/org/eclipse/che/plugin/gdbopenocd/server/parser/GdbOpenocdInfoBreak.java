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
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'info b' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdInfoBreak {

    private static final Pattern GDBOPENOCD_INFO_B = Pattern.compile("([0-9]*)\\s*breakpoint.*at\\s*(.*):([0-9]*).*");

    private final List<Breakpoint> breakpoints;

    private GdbOpenocdInfoBreak(List<Breakpoint> breakpoints) {
        this.breakpoints = breakpoints;
    }

    public List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdInfoBreak parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        List<Breakpoint> breakpoints = new ArrayList<>();

        for (String line : output.split("\n")) {
            Matcher matcher = GDBOPENOCD_INFO_B.matcher(line);
            if (matcher.find()) {
                String file = matcher.group(2);
                String lineNumber = matcher.group(3);

                Location location = new LocationImpl(file, Integer.parseInt(lineNumber));
                breakpoints.add(new BreakpointImpl(location));
            }
        }

        return new GdbOpenocdInfoBreak(breakpoints);
    }
}
