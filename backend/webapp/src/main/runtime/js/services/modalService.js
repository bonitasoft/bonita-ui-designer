(function() {
  'use strict';

  //FIXME: Move this service in subscription package
  // Use decorator to override button in sp
  function modalService() {
    var modals = {};
    var openModals = [];
    return {
      open:openModal,
      close:closeModal,
      register:registerModal
    };

    function openModal(name) {
      openModals.push(name);
      modals[name].open();
    }

    function closeModal() {
      var name = openModals.pop();
      modals[name].close();
    }

    function registerModal(name, modal) {
      modals[name] = modal;
    }
  }
  angular.module('bonitasoft.ui.services')
    .service('modalService', modalService);
})();
