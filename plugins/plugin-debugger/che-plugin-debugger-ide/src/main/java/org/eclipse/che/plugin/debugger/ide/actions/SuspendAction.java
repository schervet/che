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
package org.eclipse.che.plugin.debugger.ide.actions;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/* CHE-2508: Create "suspend" Debug action */

/**
 * Action which allows suspend debugger from running process
 */
public class SuspendAction extends AbstractPerspectiveAction {
    private final DebuggerManager debuggerManager;

    @Inject
    public SuspendAction(DebuggerManager debuggerManager,
                                    DebuggerLocalizationConstant locale,
                                    DebuggerResources resources) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.suspend(),
              locale.suspendDescription(),
              null,
              resources.suspend());
        this.debuggerManager = debuggerManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
            debugger.suspend();
        }
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        Debugger debugger = debuggerManager.getActiveDebugger();
        event.getPresentation().setEnabled((debugger != null) && (!debugger.isSuspended()));
    }
}
