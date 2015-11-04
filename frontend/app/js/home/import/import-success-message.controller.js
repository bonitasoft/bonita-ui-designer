/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

  angular
    .module('bonitasoft.designer.home.import')
    .controller('ImportSuccessMessageController', ImportSuccessMessageController);

  function ImportSuccessMessageController($scope, gettextCatalog) {

    var vm = this;

    vm.joinOnNames = joinOnNames;
    vm.getState = getState;

    function joinOnNames(artifacts) {
      return artifacts.map(function(item) {
        return item.name;
      }).join(', ');
    }

    function getState() {
      if ($scope.overridden) {
        return gettextCatalog.getString('overridden');
      }
      return gettextCatalog.getString('added');
    }
  }

})();
