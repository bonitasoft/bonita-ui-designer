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
angular.module('bonitasoft.designer.editor.properties-panel').controller('SaveFragmentController', function($scope, $rootScope, currentComponent, editor, $uibModalInstance, fragmentRepo, fragmentService) {

  'use strict';

  var vm = this;

  vm.ok = function() {
    vm.saveAsFragment(this.name);
    $uibModalInstance.close(this.name);
  };

  /**
   * Saves a container as a fragment, and replace the container with the fragment created.
   * @param name the fragment name
   */
  vm.saveAsFragment = function(name) {
    var fragment = {
      name: name,
      rows: currentComponent.rows
    };

    fragmentRepo
      .create(fragment)
      .then(function(createdFragment) {
        //update the fragment palette
        var paletteItem = fragmentService.register([createdFragment])[0];

        //replace the components by the fragment component
        var fragmentComponent = paletteItem.create(currentComponent.$$parentContainerRow);
        replaceCurrentComponent(fragmentComponent);

        // Update fragment palette with the new fragment
        $rootScope.$broadcast('fragmentCreated');
      });
  };

  /**
   * Replaces the currently selected component (widget or container) from its parent row by another component. A component must be selected
   * before calling this function.
   * @todo refactor, it's the same code removeCurrentComponent
   */
  function replaceCurrentComponent(newComponent) {
    var component = currentComponent;
    var row = component.$$parentContainerRow.row;
    var componentIndex = row.indexOf(component);
    newComponent.dimension = component.dimension;
    row[componentIndex] = newComponent;
    editor.selectComponent(newComponent);
  }
});
