/*******************************************************************************
 * Copyright (C) 2009, 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
(() => {

  angular
    .module('bonitasoft.designer.i18n')
    .directive('navbarToggle', () => ({
      scope: {
        onClick: '&'
      },
      restrict: 'E',
      templateUrl: 'js/navbar-toggle/navbar-toggle.html'
    }));

})();
