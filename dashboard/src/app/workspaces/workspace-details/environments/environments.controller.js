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
 * @name workspace.details.controller:WorkspaceEnvironmentsController
 * @description This class is handling the controller for details of workspace : section environments
 * @author Oleksii Kurinnyi
 */
export class WorkspaceEnvironmentsController {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($q, $log, $scope, $timeout) {
    this.$q = $q;
    this.$log = $log;
    this.$scope = $scope;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      mode: 'text/x-yaml',
      readOnly: true,
      onLoad: (editor) => {
        $timeout(() => {
          editor.refresh();
        }, 1000);
      }
    };

    this.init();
  }

  /**
   * Sets initial values
   */
  init() {
    this.newEnvironmentName = this.environmentName;
    this.environmentValue = this.workspaceConfig.environments[this.environmentName];
    this.environmentRecipe = this.parseRecipe(this.environmentValue.recipe.content, this.environmentValue.recipe.contentType);
  }

  /**
   * Parses recipe content
   * @param recipeContent {string}
   * @param recipeContentType {string}
   * @returns {object}
   */
  parseRecipe(recipeContent, recipeContentType) {
    let recipe = {};
    if (/yaml/i.test(recipeContentType)){
      try {
        recipe = jsyaml.load(recipeContent);
      } catch (e) {
        this.$log.error(e);
      }
    }
    return recipe;
  }

  /**
   * Dumps recipe object
   * @param recipe {object}
   * @param recipeContentType {string}
   * @returns {string}
   */
  stringifyRecipe(recipe, recipeContentType) {
    let recipeContent = '';
    if (/yaml/i.test(recipeContentType)) {
      try {
        recipeContent = jsyaml.dump(recipe);
      } catch (e) {
        this.$log.error(e);
      }
    }

    return recipeContent;
  }

  /**
   * Updates name of environment
   * @param isFormValid {boolean}
   */
  updateEnvironmentName(isFormValid) {
    if (!isFormValid || this.newEnvironmentName === this.environmentName) {
      return;
    }

    this.workspaceConfig.environments[this.newEnvironmentName] = this.environmentValue;
    delete this.workspaceConfig.environments[this.environmentName];

    if (this.workspaceConfig.defaultEnv === this.environmentName) {
      this.workspaceConfig.defaultEnv = this.newEnvironmentName;
    }

    this.doUpdateEnvironments();
  }

  /**
   * Callback which is called from WorkspaceMachineConfigController
   * @returns {Promise}
   */
  updateMachineConfig() {
    let newRecipeContent = this.stringifyRecipe(this.environmentRecipe, this.environmentValue.recipe.contentType);
    if (newRecipeContent) {
      this.workspaceConfig.environments[this.environmentName].recipe.content = newRecipeContent;
    }
    return this.doUpdateEnvironments().then(() => {
      this.init();
    });
  }

  /**
   * Calls parent controller's callback to update environment
   * @returns {IPromise<TResult>|*|Promise.<TResult>}
   */
  doUpdateEnvironments() {
    return this.environmentOnChange();
  }

}
