(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .value('assets', {
      'myStyle.css': {
        content: '.somecssrule {\n  color: blue\n}'
      }
    });

})();
