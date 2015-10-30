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
angular.module('bonitasoft.designer.editor.whiteboard').controller('TabsContainerDirectiveCtrl', function($scope, arrays, whiteboardComponentWrapper, pageElementFactory) {

  'use strict';

  $scope.openTab = function(tab, event) {
    $scope.tabsContainer.$$openedTab = tab;
    $scope.editor.selectComponent(tab, event);
  };

  $scope.isOpened = function(tab) {
    return $scope.tabsContainer.$$openedTab === tab;
  };

  $scope.tabsContainer.$$openedTab = $scope.tabsContainer.tabs[0];

  $scope.moveTabLeftVisible = function(tab) {
    return $scope.editor.isCurrentComponent(tab) && arrays.moveLeftPossible(tab, $scope.tabsContainer.tabs);
  };

  $scope.moveTabRightVisible = function(tab) {
    return $scope.editor.isCurrentComponent(tab) && arrays.moveRightPossible(tab, $scope.tabsContainer.tabs);
  };

  $scope.moveTabLeft = function(tab) {
    arrays.moveLeft(tab, $scope.tabsContainer.tabs);
  };

  $scope.moveTabRight = function(tab) {
    arrays.moveRight(tab, $scope.tabsContainer.tabs);
  };

  $scope.addTab = function(event) {
    var tabs = $scope.tabsContainer.tabs;
    var newTab = pageElementFactory.createTabElement('Tab ' + (tabs.length + 1));
    whiteboardComponentWrapper.wrapTab(newTab, $scope.tabsContainer);
    tabs.push(newTab);
    $scope.openTab(newTab, event);
  };

  $scope.isRemoveTabVisible = function(tab) {
    return $scope.editor.isCurrentComponent(tab) && $scope.tabsContainer.tabs.length > 1;
  };

  $scope.removeTab = function(tab, event) {
    var tabs = $scope.tabsContainer.tabs;
    var index = tabs.indexOf(tab);
    index = index >= tabs.length ? tabs.length - 1 : index;
    var previousTabIndex = index === 0 ? 0 : index - 1;
    tabs.splice(index, 1);
    $scope.openTab(tabs[previousTabIndex], event);
    tab.triggerRemoved();
  };
});
