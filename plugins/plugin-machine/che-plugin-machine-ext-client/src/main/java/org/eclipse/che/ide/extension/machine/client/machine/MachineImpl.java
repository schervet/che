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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The class which describes machine entity.
 *
 * @author Dmitry Shnurenko
 */
public class MachineImpl implements Machine {

    private final MachineDto    descriptor;
    private final List<Link>    machineLinks;

    private String activeTabName;

    @Inject
    public MachineImpl(MachineLocalizationConstant locale,
//                       EntityFactory entityFactory,
                       @Assisted MachineDto descriptor) {
        this.descriptor = descriptor;
        this.machineLinks = descriptor.getLinks();
        this.activeTabName = locale.tabInfo();
    }

    @Override
    public MachineConfig getConfig() {
        return descriptor.getConfig();
    }

    /** @return id of current machine */
    public String getId() {
        return descriptor.getId();
    }

    /** @return current machine's display name */
    public String getDisplayName() {
        return descriptor.getConfig().getName();
    }

    /** @return state of current machine */
    public MachineStatus getStatus() {
        return descriptor.getStatus();
    }

    @Override
    public MachineRuntimeInfo getRuntime() {
        return descriptor.getRuntime();
    }

    /** @return type of current machine */
    public String getType() {
        return descriptor.getConfig().getType();
    }

    /** @return location of machine recipe */
    public String getRecipeLocation() {
        MachineSourceDto machineSource = descriptor.getConfig().getSource();
        return machineSource.getLocation();
    }

    /** @return content of machine recipe */
    public String getRecipeContent() {
        MachineSourceDto machineSource = descriptor.getConfig().getSource();
        return machineSource.getContent();
    }

    /** @return type of machine recipe */
    public String getRecipeType() {
        MachineSourceDto machineSource = descriptor.getConfig().getSource();
        return machineSource.getType();
    }

    /**
     * Returns boolean which defines bounding workspace to current machine
     *
     * @return <code>true</code> machine is bounded to workspace,<code>false</code> machine isn't bounded to workspace
     */
    public boolean isDev() {
        return descriptor.getConfig().isDev();
    }

    /** Returns information about machine. */
    public Map<String, String> getProperties() {
        return descriptor.getRuntime().getProperties();
    }

    public void setActiveTabName(String activeTabName) {
        this.activeTabName = activeTabName;
    }

    public String getActiveTabName() {
        return activeTabName;
    }

    public String getTerminalUrl() {
        for (Link link : machineLinks) {
            if (Constants.TERMINAL_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }

        return "";
    }

    public String getWorkspaceId() {
        return descriptor.getWorkspaceId();
    }

    @Override
    public String getEnvName() {
        return descriptor.getEnvName();
    }

    @Override
    public String getOwner() {
        return descriptor.getOwner();
    }

    public List<Server> getServersList() {
        List<Server> serversList = new ArrayList<>();

        Map<String, ServerDto> servers = descriptor.getRuntime().getServers();

        for (Map.Entry<String, ServerDto> entry : servers.entrySet()) {
            String exposedPort = entry.getKey();
            ServerDto descriptor = entry.getValue();

//            Server server = entityFactory.createServer(exposedPort, descriptor);
//
//            serversList.add(server);
        }

        return serversList;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        MachineImpl otherMachine = (MachineImpl)other;

        return Objects.equals(getId(), otherMachine.getId());

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
