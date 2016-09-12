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
 * Controller for stack managment - creation or edit.
 * @author Ann Shumilova
 */
export class StackController {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($log, $route, cheStack, $mdDialog, cheNotification) {
    this.$log = $log;
    this.cheStack = cheStack;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;

    this.isCreation = $route.current.params.stackId === 'create';
    if (this.isCreation) {
      //TODO
    } else {
      this.stackId = $route.current.params.stackId;
      this.fetchStack();
    }
  }

  fetchStack() {
    this.loading = true;
    this.stack = this.cheStack.getStackById(this.stackId);

    if (this.stack) {
      this.loading = false;
      this.prepareStackData();
      return;
    }

    this.cheStack.fetchStack(this.stackId).then((stack) => {
      this.stack = stack;
      this.loading = false;
      this.prepareStackData();
    }, (error) => {
      if (error.status === 304) {
        this.loading = false;
        this.stack = this.cheStack.getStackById(this.stackId);
        this.prepareStackData();
      } else {
        this.$log.error(error);
        this.loading = false;
        this.invalidStack = error.statusText + error.status;
      }
    });
  }

  prepareStackData() {
    this.stackName = angular.copy(this.stack.name);
  }


  deleteStack(event) {
    var confirm = this.$mdDialog.confirm()
      .title('Would you like to delete the project ' + this.projectDetails.name)
      .content('Please confirm for the project removal.')
      .ariaLabel('Remove project')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      // remove it !
      let promise = this.projectService.remove(this.projectDetails.name);
      promise.then(() => {
        this.$location.path('/workspace/' + this.workspace.namespace + '/' + this.workspace.config.name + '/projects');
      }, (error) => {
        this.$log.log('error', error);
      });
    });
  }

}
