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
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineLimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent.MachineAction.DESTROYED;

/**
 * Manager for machine operations.
 *
 * @author Artem Zatsarynnyi
 * @author Roman Nikitenko
 */
@Singleton
public class MachineManagerImpl implements MachineManager {

    private final MachineServiceClient   machineServiceClient;
    private final WorkspaceServiceClient workspaceServiceClient;
    private final AppContext             appContext;
    private final EntityFactory          entityFactory;
    private final DtoFactory             dtoFactory;
    private final EventBus               eventBus;

    @Inject
    public MachineManagerImpl(final MachineServiceClient machineServiceClient,
                              final WorkspaceServiceClient workspaceServiceClient,
                              final EventBus eventBus,
                              final AppContext appContext,
                              final EntityFactory entityFactory,
                              final DtoFactory dtoFactory) {
        this.machineServiceClient = machineServiceClient;
        this.workspaceServiceClient = workspaceServiceClient;
        this.appContext = appContext;
        this.entityFactory = entityFactory;
        this.dtoFactory = dtoFactory;
        this.eventBus = eventBus;
    }

    @Override
    public Promise<MachineEntity> getMachine(final String workspaceId, final String machineId) {
        return workspaceServiceClient.getWorkspace(workspaceId).then(new Function<WorkspaceDto, MachineEntity>() {
            @Override
            public MachineEntity apply(WorkspaceDto workspace) throws FunctionException {
                for (MachineDto machineDto : workspace.getRuntime().getMachines()) {
                    if (machineId.equals(machineDto.getId())) {
                        return entityFactory.createMachine(machineDto);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Promise<List<MachineEntity>> getMachines(String workspaceId) {
        return workspaceServiceClient.getWorkspace(workspaceId).then(new Function<WorkspaceDto, List<MachineEntity>>() {
            @Override
            public List<MachineEntity> apply(WorkspaceDto workspace) throws FunctionException {
                List<MachineEntity> machines = new ArrayList<>();
                WorkspaceRuntimeDto workspaceRuntime = workspace.getRuntime();
                if (workspaceRuntime == null) {
                    return machines;
                }

                for (MachineDto machineDto : workspaceRuntime.getMachines()) {
                    MachineEntity machineEntity = entityFactory.createMachine(machineDto);
                    machines.add(machineEntity);
                }
                return machines;
            }
        });
    }

    @Override
    public void restartMachine(final Machine machineState) {
        destroyMachine(machineState).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                final MachineConfig machineConfig = machineState.getConfig();
                final MachineSource machineSource = machineConfig.getSource();
                final String displayName = machineConfig.getName();
                final boolean isDev = machineConfig.isDev();

                startMachine(asDto(machineSource), displayName, isDev, "docker");
            }
        });
    }

    /**
     * Converts {@link MachineSource} to {@link MachineSourceDto}.
     */
    public MachineSourceDto asDto(MachineSource source) {
        return this.dtoFactory.createDto(MachineSourceDto.class)
                              .withType(source.getType())
                              .withLocation(source.getLocation())
                              .withContent(source.getContent());
    }

    @Override
    public void startMachine(String recipeURL, String displayName) {
        startMachine(recipeURL, displayName, false, "dockerfile", "docker");
    }

    @Override
    public void startDevMachine(String recipeURL, String displayName) {
        startMachine(recipeURL, displayName, true, "dockerfile", "docker");
    }

    /**
     * Start new machine in workspace.
     *
     * @param recipeURL
     *         special recipe url to get docker image.
     * @param displayName
     *         display name for machine
     * @param isDev
     * @param sourceType
     *         "dockerfile" or "ssh-config"
     * @param machineType
     *         "docker" or "ssh"
     */
    private void startMachine(final String recipeURL,
                              final String displayName,
                              final boolean isDev,
                              final String sourceType,
                              final String machineType) {
        MachineSourceDto sourceDto = dtoFactory.createDto(MachineSourceDto.class).withType(sourceType).withLocation(recipeURL);
        startMachine(sourceDto, displayName, isDev, machineType);
    }

    /**
     * @param machineSourceDto
     * @param displayName
     * @param isDev
     * @param machineType
     *         "docker" or "ssh"
     */
    private void startMachine(final MachineSourceDto machineSourceDto,
                              final String displayName,
                              final boolean isDev,
                              final String machineType) {

        MachineLimitsDto limitsDto = dtoFactory.createDto(MachineLimitsDto.class).withRam(1024);
        if (isDev) {
            limitsDto.withRam(3072);
        }

        MachineConfigDto configDto = dtoFactory.createDto(MachineConfigDto.class)
                                               .withDev(isDev)
                                               .withName(displayName)
                                               .withSource(machineSourceDto)
                                               .withLimits(limitsDto)
                                               .withType(machineType);
        workspaceServiceClient.createMachine(appContext.getWorkspaceId(), configDto);
    }

    @Override
    public Promise<Void> destroyMachine(final Machine machineState) {
        return machineServiceClient.destroyMachine(machineState.getWorkspaceId(),
                                                   machineState.getId()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                eventBus.fireEvent(new MachineStateEvent(machineState, DESTROYED));

                final DevMachine devMachine = appContext.getDevMachine();
                if (devMachine != null && machineState.getId().equals(devMachine.getId()) && appContext instanceof AppContextImpl) {
                    ((AppContextImpl)appContext).setDevMachine(null);
                }
            }
        });
    }
}
