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
 * 'ptype' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdPType {

    private static final Pattern GDBOPENOCD_ARGS = Pattern.compile("type = (.*)");

    private final String type;

    public GdbOpenocdPType(String type) {this.type = type;}

    public String getType() {
        return type;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdPType parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Matcher matcher = GDBOPENOCD_ARGS.matcher(output);
        if (matcher.find()) {
            String type = matcher.group(1);
            return new GdbOpenocdPType(type);
        }

        throw new GdbOpenocdParseException(GdbOpenocdPrint.class, output);
    }
}
