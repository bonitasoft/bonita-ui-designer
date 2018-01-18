/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

  angular
    .module('bonitasoft.designer.editor.header.help')
    .directive('uidMigrationHelpTab', ['$localStorage', function($localStorage) {
      return {
        restrict: 'E',
        replace: true,
        templateUrl: 'js/editor/header/help/help-migration.html',
        link: function($scope) {
          if (!$localStorage.bonitaUIDesigner) {
            $localStorage.bonitaUIDesigner = {};
          }
          let storedDoNotShowAgain = $localStorage.bonitaUIDesigner.doNotShowMigrationNotesAgain;
          $scope.showMessage = storedDoNotShowAgain ? !storedDoNotShowAgain : true;
        }
      };
    }]);
}());
