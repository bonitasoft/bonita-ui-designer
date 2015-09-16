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

  angular.module('bonitasoft.designer.assets').controller('AssetPreviewPopupCtrl', function ($scope, $modalInstance, asset, component, mode) {

    'use strict';

    $scope.url = getUrl();
    $scope.asset = asset;

    $scope.cancel = function () {
      $modalInstance.dismiss();
    };

    function getUrl() {
      //Url depends on the nature of component
      //In custom widget editor, component is a widget
      if (mode === 'widget') {
        return 'preview/widget/' + component.id + '/assets/' + asset.type + '/' + asset.name + '?format=text';
      }
      //In page editor widget id is stored in asset.componentId if the asset scope is WIDGET
      else if (asset.scope === 'WIDGET') {
        return 'preview/widget/' + asset.componentId + '/assets/' + asset.type + '/' + asset.name + '?format=text';
      }
      //The last case is to see a page asset
      return 'preview/page/' + component.id + '/assets/' + asset.type + '/' + asset.name + '?format=text';
    }
  });

})();
