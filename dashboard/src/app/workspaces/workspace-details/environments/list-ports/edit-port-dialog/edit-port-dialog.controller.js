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
 * @name list.environment.variables.controller:EditPortDialogController
 * @description This class is handling the controller for the dialog box about editing the environment variable.
 * @author Oleksii Kurinnyi
 */
export class EditPortDialogController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, $scope) {
    this.$mdDialog = $mdDialog;
    this.$scope = $scope;

    this.updateInProgress = false;
    this.port = parseInt(this.servers[this.serverName].port,10);
    this.protocol = this.servers[this.serverName].protocol;

    this.copyServers = angular.copy(this.servers);
    delete this.copyServers[this.serverName];
  }

  /**
   * It will hide the dialog box.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Update port
   */
  updatePort() {
    this.updateInProgress = true;

    this.callbackController.updatePort(this.serverName, this.port, this.protocol).finally(() => {
      this.updateInProgress = false;
      this.hide();
    });
  }
}
