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

  angular.module('bonitasoft.ui.assets').service('assetsService', function (gettextCatalog) {

    'use strict';

    var type = {
      js : {key : 'js', value: 'JavaScript', filter:true },
      css : {key : 'css', value: 'CSS', filter:true},
      img : {key : 'img', value: 'Image', filter:true}
    };

    var source = {
      external : {key : 'external', value: gettextCatalog.getString('External')},
      local : {key : 'local', value: gettextCatalog.getString('Local')}
    };

    /**
     * Asset types
     */
    function getType() {
      return type;
    }

    /**
     * Asset sources
     */
    function getSource() {
      return source;
    }

    /**
     * Convert asset object in object for the html form
     */
    function assetToForm(asset) {
      if (!asset) {
        return {
          type: type.js.key,
          source: source.external.key
        };
      }
      //An asset is identified by name and type. If user choose to change them we need to delete
      //the old asset and we need the old name and type
      return {
        id: asset.id,
        name: asset.name,
        type: asset.type,
        source: isExternal(asset) ? source.external.key : source.local.key,
        oldname: asset.name,
        oldtype: asset.type
      };
    }

    /**
     * Convert html form asset in business object which can be sent to the backend
     */
    function formToAsset(formAsset) {
      var asset = {
        id: formAsset.id,
        type: formAsset.type
      };
      if (formAsset.source === source.external.key) {
        asset.name = formAsset.name;
      }
      return asset;
    }

    /**
     * External asset are URL
     */
    function isExternal(asset) {
      return asset.source === source.external.key || (asset.name && (asset.name.indexOf('http:') === 0 || asset.name.indexOf('https:') === 0));
    }

    /**
     * Page asset
     */
    function isPageAsset(asset) {
      return !asset.componentId;
    }

    return {
      isExternal: isExternal,
      isPageAsset: isPageAsset,
      getSource: getSource,
      getType: getType,
      assetToForm: assetToForm,
      formToAsset: formToAsset
    };
  });

})();
