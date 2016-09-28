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
package org.eclipse.che.plugin.gdbopenocd.server;

import org.eclipse.che.plugin.gdbopenocd.server.exception.GdbOpenocdParseException;

import java.io.IOException;

/**
 * GDB server.
 *
 * @author Anatoliy Bazko
 */
public class GdbOpenocdServer extends GdbOpenocdProcess {

    private static final String PROCESS_NAME     = "gdbOpenocdserver";
    private static final String OUTPUT_SEPARATOR = "\n";

    private GdbOpenocdServer(String host, int port, String file) throws IOException,
                                                                 GdbOpenocdParseException,
                                                                 InterruptedException {
        super(OUTPUT_SEPARATOR, PROCESS_NAME, host + ":" + port, file);
    }

    /**
     * Starts gdbOpenocd server.
     */
    public static GdbOpenocdServer start(String host, int port, String file) throws InterruptedException,
                                                                             GdbOpenocdParseException,
                                                                             IOException {
        return new GdbOpenocdServer(host, port, file);
    }
}
