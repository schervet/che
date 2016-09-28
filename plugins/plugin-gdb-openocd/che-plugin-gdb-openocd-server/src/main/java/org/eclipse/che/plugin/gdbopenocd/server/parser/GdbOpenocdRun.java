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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'run' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdRun {

    private static final Pattern GDBOPENOCD_BREAKPOINT = Pattern.compile("Breakpoint .* at (.*):([0-9]*).*");

    private final Breakpoint breakpoint;

    public GdbOpenocdRun(Breakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }

    @Nullable
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdRun parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        for (String line : output.split("\n")) {
            Matcher matcher = GDBOPENOCD_BREAKPOINT.matcher(line);
            if (matcher.find()) {
                String file = matcher.group(1);
                String lineNumber = matcher.group(2);

                Location location = new LocationImpl(file, Integer.parseInt(lineNumber));
                return new GdbOpenocdRun(new BreakpointImpl(location));
            }
        }

        return new GdbOpenocdRun(null);
    }
}
