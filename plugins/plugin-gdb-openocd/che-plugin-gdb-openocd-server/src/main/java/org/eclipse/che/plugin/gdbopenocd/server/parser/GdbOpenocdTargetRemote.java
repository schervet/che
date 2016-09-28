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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'target remote' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdTargetRemote {

    private static final Pattern GDBOPENOCD_TARGET_REMOTE = Pattern.compile("Remote debugging using (.*):(.*)\n.*");
    private static final Pattern CONNECTION_TIMED_OUT = Pattern.compile(".*Connection timed out.*");

    private final String host;
    private final String port;

    public GdbOpenocdTargetRemote(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdTargetRemote parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException, DebuggerException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_TARGET_REMOTE.matcher(output);
        if (matcher.find()) {
            String host = matcher.group(1);
            String port = matcher.group(2);
            return new GdbOpenocdTargetRemote(host, port);
        } else if (CONNECTION_TIMED_OUT.matcher(output).find()) {
            throw new DebuggerException(output);
        }

        throw new GdbOpenocdParseException(GdbOpenocdTargetRemote.class, output);
    }
}
