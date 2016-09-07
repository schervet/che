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
 * @ngdoc directive
 * @name workspaces.details.directive:portValidation
 * @restrict A
 * @element
 *
 * @author Oleksii Kurinnyi
 */
export class PortValidation {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor (lodash) {
    this.lodash = lodash;

    this.restrict = 'A';
    this.require = 'ngModel';
  }

  link($scope, element, attrs, ctrl) {
    // validate only input element
    if ('input' === element[0].localName) {
      ctrl.$validators.portValidation = (modelValue) => {
        if (!modelValue) {
          return;
        }

        let servers = $scope.$parent.$eval(attrs.portValidation);
        let isUsed = this.lodash.some(servers, (server) => {
          return parseInt(server.port, 10) === modelValue;
        });
        return !isUsed;
      }
    }
  }
}

