angular.module('pb.widgets')
  .directive('{{ id }}', function() {
    return {
      {{#if controller}}
      controllerAs: 'ctrl',
      controller: {{{ controller }}},
      {{/if}}
      template: '{{{ escapedTemplate }}}'
    };
  });
