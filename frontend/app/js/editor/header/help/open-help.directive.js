/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
  'use strict';

  angular.module('bonitasoft.designer.editor.header.help').directive('openHelp', ['$uibModal', function($uibModal) {
    return {
      restrict: 'A',
      scope: {
        helpSection: '@openHelp',
        editorMode: '@'
      },
      link: function($scope, elem) {
        function onClick() {
          $uibModal.open({
            templateUrl: 'js/editor/header/help/help-popup.html',
            size: 'lg',
            resolve: {
              pageEdition: function() {
                return $scope.editorMode === 'page';
              },
              helpSection: function() {
                return $scope.helpSection;
              }
            },
            controller: function($scope, $uibModalInstance, pageEdition, helpSection) {
              'ngInject';
              $scope.pageEdition = pageEdition;
              if (helpSection) {
                $scope.tabContainer = {
                  activeTab: helpSection
                };
              }
              $scope.cancel = function() {
                $uibModalInstance.dismiss('cancel');
              };
            }
          });
        }
        elem.on('click', onClick);
      }
    };
  }]);
})();
