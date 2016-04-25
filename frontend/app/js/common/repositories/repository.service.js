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
    .factory('Repository', repositoryService);

  function repositoryService($http) {

    class Repository {
      constructor(type, baseUrl) {
        this.type = type;
        this.baseUrl = baseUrl;
        this.lastSavedState = {};
        this.$http = $http;
      }

      save(artifact) {
        return this.$http.put(`${this.baseUrl}/${artifact.id}`, artifact)
          .success(() => {
            this.lastSavedState = artifact;
          });
      }

      /**
       * Initialise lastSavedState to track update from editor
       * @param  {Object} artifact  the current artifact being edited
       */
      initLastSavedState(artifact) {
        this.lastSavedState = angular.copy(artifact);
      }

      /**
       * Utility function to track if a artifact being updated, need to be saved
       * @param  {Object} artifact the artifact being updated
       * @return {Boolean}
       */
      needSave(artifact) {
        return !angular.equals(artifact, this.lastSavedState);
      }

      delete(id) {
        return this.$http.delete(`${this.baseUrl}/${id}`);
      }

      create(artifact, sourceArtifactId) {
        return this.$http.post(this.baseUrl + (sourceArtifactId ? '?duplicata=' + sourceArtifactId : ''), artifact)
          .then((response) => response.data);
      }

      load(id) {
        return this.$http.get(`${this.baseUrl}/${id}`);
      }

      all() {
        return this.$http.get(this.baseUrl)
          .then((response) => response.data);
      }

      /**
       * Return export url of a artifact
       * @param artifact - the artifact to be exported
       */
      exportUrl(artifact) {
        return `export/${this.type}/${artifact.id}`;
      }

      markAsFavorite(artifactId) {
        return this.$http.put(`${this.baseUrl}/${artifactId}/favorite`, true);
      }

      unmarkAsFavorite(artifactId) {
        return this.$http.put(`${this.baseUrl}/${artifactId}/favorite`, false);
      }
    }

    return Repository;
  }

})();
