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
(function () {
  'use strict';

  angular
    .module('bonitasoft.designer.custom-widget')
    .controller('CustomWidgetEditorCtrl', CustomWidgetEditorCtrl);

  function CustomWidgetEditorCtrl($scope, artifact, artifactRepo, alerts, $uibModal, $window, keyBindingService, gettextCatalog, $stateParams, $state, BONDS, browserHistoryService, $timeout) {

    $scope.widget = artifact;
    $scope.bonds = BONDS;

    $scope.sections = [{
      id:'code',
      name: gettextCatalog.getString('Code'),
      icon: 'fa-file-code-o',
      classes:'selected',
      click: (event, id) => $scope.openSection(event, id)
    },{
      id:'general',
      name: gettextCatalog.getString('General'),
      icon: 'fa-tag',
      click: (event, id) => $scope.openSection(event, id)
    }];

    $scope.isPropertyPanelClosed = false;

    $scope.openSection = function (evt, section) {
      let sections = document.getElementsByClassName('widget-section');

      Object.values(sections).forEach(s => {
        s.style.display = 'none';
      });

      let navSections = document.getElementsByClassName('nav-section');
      Object.values(navSections).forEach(tab => {
        tab.className = tab.className.replace(' selected', '');
      })
      document.getElementById(section).style.display = 'flex';
      evt.currentTarget.className += ' selected';
    }

    if (!$scope.widget.custom) {
      if ($scope.widget.type === 'container') {
        alerts.addInfo(gettextCatalog.getString('Containers cannot be edited.'), 5000);
      } else {
        alerts.addInfo(gettextCatalog.getString('To edit this standard widget, save it first as a new custom widget'), 5000);
      }
    }

    var widgetRepo = artifactRepo;

    keyBindingService.bindGlobal(['ctrl+s', 'command+s'], function () {
      $scope.$apply(function () {
        $scope.save();
      });
      // prevent default browser action
      return false;
    });

    $scope.isTypeSelectable = function (propertyBond) {
      return propertyBond !== 'variable' && propertyBond !== 'interpolation';
    };

    $scope.back = () => browserHistoryService.back(() => $state.go('designer.home'));

    /**
     * Updates the property
     * @param paramName the name of the property to update
     * @param param - the property to update
     */
    $scope.updateParam = function (paramName, param) {
      widgetRepo.updateProperty($scope.widget.id, paramName, param).then(function (response) {
        $scope.widget.properties = response.data;
      });
    };

    /**
     * Adds a new property
     * @param param - the param to add
     */
    $scope.addParam = function (param) {
      widgetRepo.addProperty($scope.widget.id, param).then(function (response) {
        $scope.widget.properties = response.data;
      });
    };

    /**
     * Deletes an existing property, by dropping it from the collection
     * @param paramIndex - the index of the property to drop
     */
    $scope.deleteParam = function (param) {
      widgetRepo.deleteProperty($scope.widget.id, param.name).then(function (response) {
        $scope.widget.properties = response.data;
      });
    };

    /**
     * Saves a widget and gives it an id based on its name (Awesome Widget -> awesome-widget)
     */
    $scope.save = function () {
      widgetRepo.save($scope.widget)
        .then(() => $scope.dirty = false)
        .then(() => $scope.$broadcast('saved'));

    };

    $scope.saveAndExport = function () {
      widgetRepo.save($scope.widget).then(function () {
        $window.location = widgetRepo.exportUrl($scope.widget);
      });
    };

    $scope.saveAs = function (widget) {
      var modalInstance = $uibModal.open({
        templateUrl: 'js/custom-widget/save-as-popup.html',
        controller: 'saveWidgetAsPopUpCtrl',
        controllerAs: 'ctrl',
        resolve: {
          widget: () => widget
        }
      });

      modalInstance.result
        .then(saveAs)
        .then(reload);

      function reload(widget) {
        $stateParams.id = widget.id;
        $state.go($state.current, $stateParams, {
          reload: true
        });
      }

      function saveAs(data) {
        return widgetRepo.create(data, widget.id);
      }
    };

    $scope.pristine = true;
    $scope.dirty = false;
    $scope.isPageDirty = function (artifact) {
      let needSave = widgetRepo.needSave(artifact);
      $scope.pristine = $scope.pristine && !needSave;
      $scope.dirty = $scope.dirty || needSave;
      return needSave;
    };

    $scope.createOrUpdate = function (param) {

      var modalInstance = $uibModal.open({
        templateUrl: 'js/custom-widget/create-property.html',
        controller: 'PropertyEditorPopupCtrl',
        resolve: {
          param: () => param
        }
      });

      modalInstance.result
        .then(function (result) {
          if (result.paramToUpdate) {
            $scope.updateParam(result.paramToUpdate.name, result.param);
          } else {
            $scope.addParam(result.param);
          }
        });
    };

    $scope.openHelp = (section) => {
      $uibModal.open({
        templateUrl: 'js/custom-widget/help-popup.html',
        size: 'lg',
        resolve: {
          helpSection: function () {
            return section;
          }
        },
        controller: function ($scope, $uibModalInstance, helpSection) {
          if (helpSection) {
            $scope.tabContainer = {
              activeTab: helpSection
            };
          }

          $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
          };
        }
      });
    };

    $scope.$on('$destroy', function () {
      keyBindingService.unbind(['ctrl+s', 'command+s']);
    });

    $scope.togglePropertyPanel = function () {
      $scope.isPropertyPanelClosed = !$scope.isPropertyPanelClosed;
      // Allow to resize the code editor after toogle animation
      $timeout(() => $window.dispatchEvent(new Event('resize')), 300);

    }
  }

})();
