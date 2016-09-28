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

/**
 * I18n constants for the Debugger extension.
 *
 * @author Artem Zatsarynnyi
 */
public interface GdbOpenocdLocalizationConstant extends com.google.gwt.i18n.client.Messages {

    /* GdbOpenocdConfigurationPage */
    @Key("view.gdbOpenocdConfigurationPage.hostLabel")
    String gdbOpenocdConfigurationPageViewHostLabel();

    @Key("view.gdbOpenocdConfigurationPage.portLabel")
    String gdbOpenocdConfigurationPageViewPortLabel();

    @Key("view.gdbOpenocdConfigurationPage.binPathLabel")
    String gdbOpenocdConfigurationPageViewBinPathLabel();

    @Key("view.gdbOpenocdConfigurationPage.binPathDescription")
    String gdbOpenocdConfigurationPageViewBinPathDescription();

    @Key("view.gdbOpenocdConfigurationPage.devHostCheckbox")
    String gdbOpenocdConfigurationPageViewDevMachineCheckbox();
}
