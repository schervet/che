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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.inject.DynaModule;

/**
 * @author Anatoliy Bazko
 */
@DynaModule
public class GdbOpenocdDebuggerModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), DebuggerFactory.class).addBinding().to(GdbOpenocdDebuggerFactory.class);
    }
}