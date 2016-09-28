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
 * 'directory' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdDirectory {

    private static final Pattern GDBOPENOCD_DIRECTORY = Pattern.compile("^Source directories searched: (.*)\n");

    private final String directories;

    public GdbOpenocdDirectory(String directories) {this.directories = directories;}

    public String getDirectories() {
        return directories;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdDirectory parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_DIRECTORY.matcher(output);
        if (matcher.find()) {
            String directory = matcher.group(1);
            return new GdbOpenocdDirectory(directory);
        }

        throw new GdbOpenocdParseException(GdbOpenocdDirectory.class, output);
    }
}
