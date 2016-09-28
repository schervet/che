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
 * GDB version.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdVersion {

    private static final Pattern GDBOPENOCD_VERSION = Pattern.compile("(GNU gdb \\(.*\\)) (.*)\n.*");

    private final String name;
    private final String version;

    public GdbOpenocdVersion(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdVersion parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_VERSION.matcher(output);
        if (matcher.find()) {
            String name = matcher.group(1);
            String version = matcher.group(2);
            return new GdbOpenocdVersion(name, version);
        }

        throw new GdbOpenocdParseException(GdbOpenocdVersion.class, output);
    }
}
