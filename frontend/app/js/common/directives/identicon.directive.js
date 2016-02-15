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
  class IdenticonDirective {
    constructor() {
      this.restrict = 'E';
      this.template = '<img width={{ctrl.size}} height={{ctrl.size}} ng-src="data:image/png;base64,{{ctrl.data}}"/>';
      this.bindToController = { name: '@' , size: '@', backgroundColor: '=', foregroundColor: '=' };
      this.scope = true;
      this.controllerAs = 'ctrl';
    }
    controller($scope, $sha, $window) {
      'ngInject';
      $scope.$watchGroup([() => this.name, () => this.size], () => {
        this.data = new $window.Identicon({
          'hash': $sha.hash('1', 'TEXT', 'HEX', this.name || ''),
          size: Number(this.size) || 40,
          bg: (angular.isArray(this.backgroundColor) && this.backgroundColor.length > 1) ? this.backgroundColor : [64, 72, 83],
          fg: (angular.isArray(this.foregroundColor) && this.foregroundColor.length > 1) ? this.foregroundColor : [255, 255, 255],
        }).toString();
      });
    }
  }
  angular
    .module('bonitasoft.designer.common.directives')
    .directive('identicon', () => new IdenticonDirective());
})();
