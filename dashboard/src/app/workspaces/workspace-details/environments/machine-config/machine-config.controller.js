/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc controller
 * @name workspace.details.controller:WorkspaceMachineConfigController
 * @description This class is handling the controller for machine config
 * @author Oleksii Kurinnyi
 */
export class WorkspaceMachineConfigController {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, $log, $scope, $timeout) {
    this.$mdDialog = $mdDialog;
    this.$log = $log;
    this.$timeout = $timeout;

    this.timeoutPromise;
    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });

    this.init();
  }

  /**
   * Sets initial values
   */
  init() {
    this.devMachineAgentName = 'ws-agent';
    this.isDev = this.machineConfigs[this.machineName].agents.includes(this.devMachineAgentName);
    this.newDev = this.isDev;

    let machineRecipe = this.environmentRecipe.services[this.machineName];
    this.source = machineRecipe.image;
    this.ram = machineRecipe.mem_limit;
    this.newRam = this.ram;

    this.envVariables = machineRecipe.environment || {};
  }

  /**
   * Modifies agents list in machine: adds or removes 'ws-agent'
   */
  updateDev() {
    this.$timeout.cancel(this.timeoutPromise);

    if (this.isDev === this.newDev) {
      return;
    }

    this.timeoutPromise = this.$timeout(() => {
      let agentIndex = this.machineConfigs[this.machineName].agents.indexOf(this.devMachineAgentName);
      if (this.newDev && agentIndex === -1) {
        this.machineConfigs[this.machineName].agents.push(this.devMachineAgentName);
      } else if (!this.newDev && agentIndex > -1) {
        this.machineConfigs[this.machineName].agents.splice(agentIndex, 1);
      }

      this.doUpdateConfig().then(() => {
        this.init();
      });
    }, 1000);
  }

  /**
   * Updates amount of RAM for machine after a delay
   * @param isFormValid {boolean}
   */
  updateRam(isFormValid) {
    this.$timeout.cancel(this.timeoutPromise);

    if (!isFormValid || this.ram === this.newRam) {
      return;
    }

    this.timeoutPromise = this.$timeout(() => {
      this.environmentRecipe.services[this.machineName].mem_limit = this.newRam;
      this.doUpdateConfig().then(() => {
        this.init();
      });
    }, 1000);
  }

  /**
   * Callback which is called from ListPortsController
   * @returns {Promise}
   */
  updateServers() {
    return this.doUpdateConfig();
  }

  /**
   * Callback which is called from ListEnvVariablesController
   * @returns {Promise}
   */
  updateEnvVariables() {
    if (Object.keys(this.envVariables).length) {
      this.environmentRecipe.services[this.machineName].environment = this.envVariables;
    } else {
      delete this.environmentRecipe.services[this.machineName].environment;
    }

    return this.doUpdateConfig().then(() => {
      this.init();
    });
  }

  /**
   * Calls parent controller's callback to update machine config
   * @returns {IPromise<TResult>|*|Promise.<TResult>}
   */
  doUpdateConfig() {
    return this.machineConfigOnChange();
  }

  /**
   * Show dialog to edit machine name
   * @param $event
   */
  showEditDialog($event) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditMachineNameDialogController',
      controllerAs: 'editMachineNameDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        machineName: this.machineName,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/machine-config/edit-machine-name-dialog/edit-machine-name-dialog.html'
    });
  }

  /**
   * Updates machine name
   * @param newMachineName {string} new machine name
   * @returns {Promise}
   */
  updateMachineName(newMachineName) {
    if (this.machineName === newMachineName) {
      let defer = this.$q.defer();
      defer.resolve();
      return defer.promise;
    }

    // update config
    this.machineConfigs[newMachineName] = this.machineConfigs[this.machineName];
    delete this.machineConfigs[this.machineName];

    // update recipe
    this.environmentRecipe.services[newMachineName] = this.environmentRecipe.services[this.machineName];
    delete this.environmentRecipe.services[this.machineName];

    return this.doUpdateConfig();
  }

  // TODO
  deleteMachine() {
    this.showDeleteConfirmation().then(() => {
      // todo
    })
  }

  /**
   * Show confirmation popup before machine to delete
   * @returns {*}
   */
  showDeleteConfirmation() {
    let confirmTitle = 'Would you like to delete this machine?';
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove machine')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }
}
