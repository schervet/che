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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'info locals' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdInfoLocals {

    private static final Pattern GDBOPENOCD_VARS = Pattern.compile("(.*) = (.*)");

    private final Map<String, String> variables;

    public GdbOpenocdInfoLocals(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * Factory method.
     */
    public static GdbOpenocdInfoLocals parse(GdbOpenocdOutput gdbOpenocdOutput) throws GdbOpenocdParseException {
        String output = gdbOpenocdOutput.getOutput();

        Map<String, String> variables = new HashMap<>();

        for (String line : output.split("\n")) {
            Matcher matcher = GDBOPENOCD_VARS.matcher(line);
            if (matcher.find()) {
                String variable = matcher.group(1);
                String value = matcher.group(2);
                variables.put(variable, value);
            }
        }

        return new GdbOpenocdInfoLocals(variables);
    }
}
