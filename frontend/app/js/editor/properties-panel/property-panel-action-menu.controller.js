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
angular.module('bonitasoft.designer.editor.properties-panel').controller('PropertyPanelActionMenuCtrl', function($scope, $uibModal, $state, componentUtils) {

  var vm = this;

  vm.openSaveFragmentModal = function() {
    var currentComponent = $scope.currentComponent;
    var editor = $scope.editor;
    return $uibModal.open({
      templateUrl: 'js/editor/properties-panel/fragment/fragment-save-popup.html',
      controller: 'SaveFragmentController',
      controllerAs: 'ctrl',
      resolve: {
        currentComponent: () => currentComponent,
        editor: () => editor
      }
    });
  };

  vm.saveAndEditFragment = function(fragmentId) {
    $scope.save()
      .then(function() {
        $state.go('designer.fragment', {
          id: fragmentId
        });
      });
  };

  vm.hasActionButton = function() {
    return vm.isViewable() || vm.isCustomEditable() || vm.isFragmentEditable() || vm.canBeSavedAsFragment() || vm.isSwitchable();
  };

  vm.isViewable = function() {
    return !vm.isFragment() && !vm.isCustom() && !vm.isContainer();
  };

  vm.isCustomEditable = function() {
    return !vm.isFragment() && vm.isCustom();
  };

  vm.isFragmentEditable = function() {
    return vm.isFragment();
  };

  vm.isSwitchable = function() {
    return !vm.isFragment() && !vm.isContainer();
  };

  vm.isCustom = function() {
    var currentComponent = $scope.currentComponent;
    return currentComponent ? currentComponent.$$widget.custom : false;
  };

  vm.isContainer = function() {
    var currentComponent = $scope.currentComponent;
    return currentComponent ? currentComponent.$$widget.type === 'container' : false;
  };

  vm.isFragment = function() {
    var currentComponent = $scope.currentComponent;
    return currentComponent ? currentComponent.type === 'fragment' : false;
  };

  vm.canBeSavedAsFragment = function() {
    var currentComponent = $scope.currentComponent;
    return currentComponent ? currentComponent.$$widget.id === 'pbContainer' && !componentUtils.containsModalInContainer(currentComponent) : false;
  };

});
