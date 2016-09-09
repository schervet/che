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
 * Test the custom validation directive
 * @author Oleksii Kurinnyi
 */

describe('custom-validator', function() {
  var $scope, form, $compiler;

  /**
   * Backend for handling http operations
   */
  var httpBackend;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject(function($compile, $rootScope, cheHttpBackend) {
    $scope = $rootScope;
    $compiler = $compile;
    httpBackend = cheHttpBackend.getHttpBackend();

    httpBackend.whenGET(/.*/).respond(200, '');
  }));

  describe('Directive', () => {
    it('throws an error if validator is not a function', () => {
      $scope.model = {name: ''};
      $scope.test = 'some string';

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.name" name="name" custom-validator="test" />' +
        '</form>'
      );

      let error;
      try {
        $compiler(element)($scope);
      } catch (e) {
        error = e;
      }

      expect(error).toBeDefined();
    });

    it('doesn\'t throw any error if validator is a function', () => {
      $scope.model = {name: ''};
      $scope.test = function() {};

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.name" name="name" custom-validator="test" />' +
        '</form>'
      );

      let error;
      try {
        $compiler(element)($scope);
      } catch (e) {
        error = e;
      }

      expect(error).toBeUndefined();
    });
  });

  describe('Validate model value', () => {

    it('if same value already exists', () => {
      let uniqueValues = ['name1', 'name2', 'name3'],
        nonUniqueValue = uniqueValues[0];
      $scope.model = {name: ''};
      $scope.testFunc = (value) => {
        return !uniqueValues.includes(value);
      };

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.name" name="name" custom-validator="testFunc" />' +
        '</form>'
      );
      $compiler(element)($scope);

      form = $scope.form;
      form.name.$setViewValue(nonUniqueValue);

      // check form (expect invalid)
      expect(form.name.$invalid).toBe(true);
      expect(form.name.$valid).toBe(false);
    });

    it('if new unique value entered', function() {
      let uniqueValues = ['name1', 'name2', 'name3'],
          newUniqueValue = 'name4';
      $scope.model = {name: ''};
      $scope.testFunc = (value) => {
        return !uniqueValues.includes(value);
      };

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.name" name="name" custom-validator="testFunc" />' +
        '</form>'
      );
      $compiler(element)($scope);

      form = $scope.form;
      form.name.$setViewValue(newUniqueValue);

      // check form (expect invalid)
      expect(form.name.$invalid).toBe(false);
      expect(form.name.$valid).toBe(true);
    });
  });
});
