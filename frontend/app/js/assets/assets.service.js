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

  angular.module('pb.assets').service('assetsService', function (gettextCatalog) {

    'use strict';

    var types = [
      { key: 'js', label: 'JavaScript'},
      { key: 'css', label: 'CSS'},
      { key: 'img', label: 'Image'}
    ];

    var sources = [
      { key: 'External', label: gettextCatalog.getString('External')},
      { key: 'Local', label: gettextCatalog.getString('Local')}
    ];

    /**
     * By default all the assets are displayed
     */
    function initFilterMap() {
      return types.map(function (obj) {
        obj.filter = true;
        return obj;
      });
    }

    /**
     * Asset types
     */
    function getTypes() {
      return types;
    }

    /**
     * Asset sources
     */
    function getSources() {
      return sources;
    }

    /**
     * Asset external source
     */
    function getExternalSource() {
      return sources[0].key;
    }

    /**
     * Return the label for a type
     */
    function getTypeLabel(key) {
      var type = types.filter(function (element) {
        return element.key === key;
      })[0];

      return type ? type.label : '';
    }

    /**
     * Convert asset object in object for the html form
     */
    function assetToForm(asset) {
      if(!asset){
        return {
          type : types[0].key,
          source : sources[0].key
        };
      }
      //An asset is identified by name and type. If user choose to change them we need to delete
      //the old asset and we need the old name and type
      return {
        name : asset.name,
        type : asset.type,
        source : sources[isExternal(asset) ? 0 : 1].key,
        oldname : asset.name,
        oldtype : asset.type
      };
    }

    /**
     * Convert html form asset in business object which can be sent to the backend
     */
    function formToAsset(formAsset) {
      var asset = {
        type : formAsset.type
      };
      if(formAsset.source=== 'External'){
        asset.name = formAsset.name;
      }
      return asset;
    }

    /**
     * External asset are URL
     */
    function isExternal(asset) {
      return asset.name.indexOf('http:') === 0 || asset.name.indexOf('https:') === 0;
    }

    return {
      initFilterMap: initFilterMap,
      isExternal: isExternal,
      getSources:getSources,
      getExternalSource:getExternalSource,
      getTypeLabel:getTypeLabel,
      getTypes : getTypes,
      assetToForm : assetToForm,
      formToAsset : formToAsset
    };
  });

})();