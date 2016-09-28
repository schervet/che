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
 * 'file' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdFile {

    private static final Pattern GDBOPENOCD_FILE = Pattern.compile("Reading symbols from (.*)...done.*");

    private final String file;

    private GdbOpenocdFile(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdFile parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_FILE.matcher(output);
        if (matcher.find()) {
            String file = matcher.group(1);
            return new GdbOpenocdFile(file);
        }

        throw new GdbOpenocdParseException(GdbOpenocdFile.class, output);
    }
}
