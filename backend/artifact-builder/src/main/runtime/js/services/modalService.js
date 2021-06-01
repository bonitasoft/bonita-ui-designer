(function() {
  'use strict';

  function modalService() {
    var modals = {};
    var openModals = [];
    return {
      open:openModal,
      close:closeModal,
      register:registerModal
    };

    function openModal(name) {
      if (name && modals[name]) {
        openModals.push(name);
        modals[name].open();
      }
    }

    function closeModal() {
      var name = openModals.pop();
      if (name && modals[name]) {
        modals[name].close();
      }
    }

    function registerModal(name, modal) {
      modals[name] = modal;
    }
  }
  angular.module('bonitasoft.ui.services')
    .service('modalService', modalService);
})();
