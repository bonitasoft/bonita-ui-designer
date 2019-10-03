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
/**
 * Controller of the tabsContainer directive
 */
angular.module('bonitasoft.designer.editor.whiteboard').controller('TabsContainerDirectiveCtrl', function($scope, arrays, whiteboardComponentWrapper, pageElementFactory, components) {

  'use strict';

  $scope.openTab = function(tab, event) {
    $scope.tabsContainer.$$openedTab = tab;
    $scope.editor.selectComponent(tab, event);
  };

  $scope.isOpened = function(tab) {
    return $scope.tabsContainer.$$openedTab === tab;
  };

  $scope.tabsContainer.$$openedTab = $scope.tabsContainer.tabList[0];

  $scope.moveTabLeftVisible = function(tab) {
    return $scope.editor.isCurrentComponent(tab) && arrays.moveLeftPossible(tab, $scope.tabsContainer.tabList);
  };

  $scope.moveTabRightVisible = function(tab) {
    return $scope.editor.isCurrentComponent(tab) && arrays.moveRightPossible(tab, $scope.tabsContainer.tabList);
  };

  $scope.moveTabLeft = function(tab) {
    arrays.moveLeft(tab, $scope.tabsContainer.tabList);
  };

  $scope.moveTabRight = function(tab) {
    arrays.moveRight(tab, $scope.tabsContainer.tabList);
  };

  $scope.addTabContainer = function(event) {
    let tabs = $scope.tabsContainer.tabList;
    let tabContainer = components.getById('pbTabContainer').component;
    let maxTabNumber = 1;
    tabs.forEach((tab) => {
      let tabNumber =  parseInt(tab.propertyValues.title.value.replace('Tab ', ''));
      if (Number.isInteger(tabNumber) && (tabNumber > maxTabNumber)) {
        maxTabNumber = tabNumber;
      }
    });
    let newTabElement = pageElementFactory.createTabContainerElement(tabContainer, 'Tab ' + (maxTabNumber + 1));
    whiteboardComponentWrapper.wrapTabContainer(tabContainer, newTabElement);
    tabs.push(newTabElement);
    $scope.openTab(newTabElement, event);
  };

  $scope.isRemoveTabVisible = function(tab) {
    return $scope.editor.isCurrentComponent(tab) && $scope.tabsContainer.tabList.length > 1;
  };

  $scope.removeTab = function(tab, event) {
    var tabs = $scope.tabsContainer.tabList;
    var index = tabs.indexOf(tab);
    index = index >= tabs.length ? tabs.length - 1 : index;
    var previousTabIndex = index === 0 ? 0 : index - 1;
    tabs.splice(index, 1);
    $scope.openTab(tabs[previousTabIndex], event);
    tab.triggerRemoved();
  };

  $scope.isPropertyExist = function(property) {
    if (!$scope.tabsContainer.hasOwnProperty('propertyValues') || !$scope.tabsContainer.propertyValues.hasOwnProperty(property)) {
      return false;
    }
    return $scope.tabsContainer.propertyValues[property].hasOwnProperty('value');
  };
});
