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
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.common.repositories')
    .factory('fragmentRepo', fragmentRepository);

  function fragmentRepository(Repository) {

    class FragmentRepository extends Repository {
      constructor() {
        super('fragment', 'rest/fragments');
      }

      /**
       * Gets all the fragments that are not using element given in argument
       * Usefull to filter fragment that can be used by another one
       * @returns {Promise}
       */
      allNotUsingElement(elementId) {
        return this.$http.get(this.baseUrl, {
          params: { notUsedBy: elementId }
        });
      }

      all() {
        return super.all().then(fragments =>
          fragments.map(fragment => {
            fragment.icon = true;
            return fragment;
          })
        );
      }

      /**
       * Gets all the fragments (format light)
       * @returns {Promise}
       */
      allLight() {
        return this.$http.get(`${this.baseUrl}?view=light`)
          .then((response) => response.data);
      }

      /**
       * Renames a fragment and returns a promise
       * @param id - the page's id
       * @param newName - the page's new name
       */
      rename(id, newName) {
        return this.$http.put(`${this.baseUrl}/${id}/name`, newName);
      }
    }
    return new FragmentRepository();
  }
})();
