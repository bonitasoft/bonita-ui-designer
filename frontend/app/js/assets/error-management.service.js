(() => {

  class AsserErrorManagement {

    /* @ngInject */
    constructor(alerts, $q) {
      this.alerts = alerts;
      this.$q = $q;
    }

    displayError(response) {
      if (response.type === 'MalformedJsonException') {
        this.alerts.addError({
          contentUrl: 'js/assets/malformed-json-error-message.html',
          context: response
        }, 12000);
      } else {
        this.alerts.addError(response.message);
      }
    }

    hasError(response) {
      return response && response.type && response.message;
    }

    manageErrorsFromResponse(response) {
      if (this.hasError(response)) {
        this.displayError(response);
        return this.$q.reject(response);
      } else {
        return this.$q.when(response);
      }
    }
  }

  angular
    .module('bonitasoft.designer.assets')
    .service('assetErrorManagement', AsserErrorManagement);
})();
