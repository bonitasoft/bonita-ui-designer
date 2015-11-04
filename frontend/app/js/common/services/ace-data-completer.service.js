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
angular.module('bonitasoft.designer.common.services').service('aceDataCompleter', function() {
  return function(data) {
    return {
      getCompletions: function(editor, session, pos, prefix, callback) {

        function getPrefixedKeys(key) {
          return '$data.' + key;
        }

        function filterKeys(match, key) {
          return key.indexOf(match) === 0;
        }

        var completions = Object.keys(data)
          .map(getPrefixedKeys)
          .filter(filterKeys.bind(null, prefix))
          .map(function(data) {
            return {
              name: data,
              value: data,
              score: 2, // increase score to show suggestion on top of the list
              meta: 'data' // the suggestion's category
            };
          });
        callback(null, completions);
      }
    };
  };
});
