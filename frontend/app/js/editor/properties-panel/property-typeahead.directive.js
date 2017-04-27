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
(() => {

  class PropertyTypeaheadCtrl {

    constructor($filter) {
      this.prefix = '';
      this.$filter = $filter;
      // need to initialize model with empty string to trigger typeahead on focus using typeahead-min-length="0"
      this.model = this.model || '';
    }

    getValues(search) {
      let effectiveSearch = search;
      if (search.startsWith('!')) {
        this.prefix = search.charAt(0);
        effectiveSearch = search.substr(1);
      } else {
        this.prefix = '';
      }
      return this.$filter('filter')(this.values, effectiveSearch);
    }

    onSelectedCallback(item, model, label) {
      this.model = this.prefix + label;
    }
  }

  angular
    .module('bonitasoft.designer.editor.properties-panel')
    .directive('uidPropertyTypeahead', ($parse) => ({
        scope: {},
        replace: true,
        templateUrl: 'js/editor/properties-panel/property-typeahead.html',
        controller: PropertyTypeaheadCtrl,
        controllerAs: 'vm',
        bindToController: {
          model: '=uidPropertyTypeaheadModel'
        },
        link: (scope, elem, attrs, ctrl) => {
          let typeAheadValuesExpr = $parse(attrs.uidPropertyTypeaheadValues);
          let deregister = scope.$parent.$watchCollection(typeAheadValuesExpr, (newValues) => {
            ctrl.values = newValues;
          });
          scope.$on('$destroy', deregister);
        }
      })
    );
})();
