(function () {
  try {
    return angular.module('bonitasoft.ui.widgets');
  } catch(e) {
    return angular.module('bonitasoft.ui.widgets', []);
  }
})().directive('{{ id }}', function() {
    return {
      {{#if controller}}
      controllerAs: 'ctrl',
      controller: {{{ controller }}},
      {{/if}}
      template: '{{{ escapedTemplate }}}'
    };
  });
