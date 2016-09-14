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

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent.MachineAction.CREATING;
import static org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent.MachineAction.RUNNING;

/**
 * Notifies about changing machine state.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MachineStatusNotifier implements MachineStatusChangedEvent.Handler {

    private final EventBus                    eventBus;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant locale;
    private final LoaderPresenter             loader;
    private final MachineManager              machineManager;

    @Inject
    MachineStatusNotifier(final EventBus eventBus,
                          final MachineManager machineManager,
                          final NotificationManager notificationManager,
                          final MachineLocalizationConstant locale,
                          final LoaderPresenter loader) {
        this.eventBus = eventBus;
        this.machineManager = machineManager;
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.loader = loader;

        eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
    }

    @Override
    public void onMachineStatusChanged(final MachineStatusChangedEvent event) {
        final String machineName = event.getMachineName();
        final String machineId = event.getMachineId();
        final String workspaceId = event.getWorkspaceId();

        switch (event.getEventType()) {
            case CREATING:
                machineManager.getMachine(workspaceId, machineId).then(notifyMachineCreating(machineName));
                break;
            case RUNNING:
                machineManager.getMachine(workspaceId, machineId).then(notifyMachineRunning(machineName));
                break;
            case DESTROYED:
                notificationManager.notify(locale.notificationMachineDestroyed(machineName), SUCCESS, EMERGE_MODE);
                break;
            case ERROR:
                notificationManager.notify(event.getErrorMessage(), FAIL, EMERGE_MODE);
                break;
        }
    }

    private Operation<MachineEntity> notifyMachineCreating(final String machineName) {
        return new Operation<MachineEntity>() {
            @Override
            public void apply(MachineEntity machine) throws OperationException {
                if (machine == null) {
                    notificationManager.notify(locale.failedToFindMachine(machineName));
                    return;
                }

                if (machine.isDev()) {
                    // Will be used later
                    // loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.LOADING);
                }
                eventBus.fireEvent(new MachineStateEvent(machine, CREATING));
            }
        };
    }

    private Operation<MachineEntity> notifyMachineRunning(final String machineName) {
        return new Operation<MachineEntity>() {
            @Override
            public void apply(MachineEntity machine) throws OperationException {
                if (machine == null) {
                    notificationManager.notify(locale.failedToFindMachine(machineName));
                    return;
                }

                if (machine.isDev()) {
                    // Will be used later
                    // loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.SUCCESS);
                }

                notificationManager.notify(locale.notificationMachineIsRunning(machineName), SUCCESS, EMERGE_MODE);
                eventBus.fireEvent(new MachineStateEvent(machine, RUNNING));
            }
        };
    }
}
