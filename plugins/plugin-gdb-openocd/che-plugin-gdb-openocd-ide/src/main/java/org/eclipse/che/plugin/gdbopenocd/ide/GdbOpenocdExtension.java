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
package org.eclipse.che.plugin.gdbopenocd.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;

/**
 * Extension allows to debug CPP applications.
 *
 * @author Anatoliy Bazko
 */
@Singleton
@Extension(title = "GDBOpenocd", version = "4.0.0")
public class GdbOpenocdExtension {

    @Inject
    public GdbOpenocdExtension(DebuggerManager debuggerManager, GdbOpenocdDebugger gdbOpenocdDebugger) {
        debuggerManager.registeredDebugger(GdbOpenocdDebugger.ID, gdbOpenocdDebugger);
    }
}
