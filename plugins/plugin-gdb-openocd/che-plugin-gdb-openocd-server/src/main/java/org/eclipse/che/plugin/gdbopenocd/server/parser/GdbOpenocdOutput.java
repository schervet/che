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

/**
 * Wrapper for GDBOpenocd output.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdOutput {
    private final String  output;
    private final boolean terminated;

    private GdbOpenocdOutput(String output, boolean terminated) {
        this.output = output;
        this.terminated = terminated;
    }

    public static GdbOpenocdOutput of(String output) {
        return new GdbOpenocdOutput(output, false);
    }

    public static GdbOpenocdOutput of(String output, boolean terminated) {
        return new GdbOpenocdOutput(output, terminated);
    }


    public String getOutput() {
        return output;
    }

    public boolean isTerminated() {
        return terminated;
    }
}
