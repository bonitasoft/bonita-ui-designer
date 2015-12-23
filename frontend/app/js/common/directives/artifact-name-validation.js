(function() {

  'use strict';

  class ArtifactNameValidationController {

    constructor(gettextCatalog) {
      this.maxlength = 240;
      this.validators = {
        pattern: gettextCatalog.getString('Name must contains only alphanumeric characters with no space'),
        maxlength: gettextCatalog.getString('Name must be less than {{maxlength}} characters long', this)
      };
    }

    errorMessage() {
      return this.form && Object.keys(this.form.$error).reduce((acc, val) => acc || this.validators[val] || acc, '');
    }
  }

  /**
   * Perform basic validation on UI Designer artifact's name field
   * Display a tooltip in case of erred/too long names
   *
   * @example <input artifact-name-validation="bottom-right" ng-model="artifact.name">
   *
   * @param artifact-name-validation {string} tooltip placement (top-left by default)
   */
  function artifactNameValidationDirective() {
    return {
      scope: {},
      require: ['^form', 'artifactNameValidation'],
      controller: ArtifactNameValidationController,
      controllerAs: 'name',
      templateUrl: 'js/common/directives/artifact-name-validation.html',
      replace: true,
      link: function(scope, element, attrs, ctrls) {
        let ctrl = ctrls[1];
        ctrl.form = ctrls[0];
        ctrl.placement = attrs.artifactNameValidation || 'top-left';
      }
    };
  }

  /**
   * Add extra validation message to an artifact-name-validation directive
   *
   * @example <input ng-minlength="20" artifact-name-validation artifact-name-validation-messages="{ minlength: 'Field should have at least 20 characters'}" ng-model="artifact.name">
   *
   * @param artifact-name-validation-messages {object} denotes a validation error key while a value should be a validation message
   */
  function artifactNameValidationMessagesDirective() {
    return {
      require: 'artifactNameValidation',
      link: function(scope, element, attrs, ctrl) {
        attrs.$observe('artifactNameValidationMessages', (value) => angular.extend(ctrl.validators, scope.$eval(value)));
      }
    };
  }

  angular
    .module('bonitasoft.designer.common.directives')
    .directive('artifactNameValidation', artifactNameValidationDirective)
    .directive('artifactNameValidationMessages', artifactNameValidationMessagesDirective);

})();
