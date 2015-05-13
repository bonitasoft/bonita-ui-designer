/**
 * The home page controller, listing the existing pages, widgets
 */
angular.module('pb.home').controller('HomeCtrl', function($scope, $state, $modal, $q, $timeout, $injector, pageRepo, widgetRepo, customWidgetFactory) {
  $scope.pages = [];
  $scope.widgets = [];


  pageRepo.all().then(function(pages){
    $scope.pages = pages;
  });

  widgetRepo.customs().then(function(widgets){
    $scope.widgets = widgets;
  });

  $scope.page = {
    name: '',
    rows: [
      []
    ]
  };

  $scope.widget = {};

  $scope.createPage = function(page) {
    pageRepo.create(page).then(function(data) {
      $state.go('designer.page', {
        id: data.id
      });
    });
  };

  $scope.deletePage = function(page) {
    var modalInstance = $modal.open({
      templateUrl: 'js/home/confirm-deletion-popup.html',
      backdrop: 'static',
      windowClass: 'modal-centered',
      controller: 'DeletionPopUpController',
      resolve: {
        artifact: function() {
          return page;
        },
        type: function() {
          return 'page';
        }
      }
    });

    modalInstance.result
      .then(pageRepo.delete)
      .then($scope.refreshAll);
  };

  $scope.exportPage = function(page) {
    return pageRepo.exportUrl(page);
  };


  /**
   * When something is deleted, we need to refresh every collection,
   * because we can maybe delete a component we couldn't previously
   * example :
   *   custom widget <hello> was used in page <person>, so we could not delete it.
   *   if <hello> is deleted, now we can delete <person>
   * @returns {Promise}
   */
  $scope.refreshAll = function() {
    return $q.all({
      pages: pageRepo.all(),
      widgets: widgetRepo.customs()
    }).then(function(response) {
      $scope.pages = response.pages;
      $scope.widgets = response.widgets;
    });
  };


  $scope.createWidget = function(widgetName) {
    var widget = customWidgetFactory.createCustomWidget(widgetName);
    widgetRepo.create(widget).then(function(data) {
      $state.go('designer.widget', {
        widgetId: data.id
      });
    });
  };

  $scope.deleteCustomWidget = function(customWidget) {
    var template = !angular.isDefined(customWidget.usedBy) ? 'js/home/confirm-deletion-popup.html' : 'js/home/alert-deletion-notpossible-popup.html';

    var modalInstance = $modal.open({
      templateUrl: template,
      backdrop: 'static',
      windowClass: 'modal-centered',
      controller: 'DeletionPopUpController',
      resolve: {
        artifact: function() {
          return customWidget;
        },
        type: function() {
          return 'custom widget';
        }
      }
    });

    modalInstance.result
      .then(widgetRepo.delete)
      .then($scope.refreshAll);
  };

  $scope.exportWidget = function(widget) {
    return widgetRepo.exportUrl(widget);
  };




  $scope.importElement = function(type){
    var modalInstance = $modal.open({
      templateUrl: 'js/home/import-artifact.html',
      backdrop: 'static',
      windowClass: 'modal-centered',
      controller: function($scope, $modalInstance, alerts) {
        $scope.importUrl = 'import/' + type;
        $scope.filename = '';
        $scope.importType = type;

        $scope.onSuccess = function(response) {
          //Even if a problem occurs in the backend a response is sent with a message
          //If the message has a type and a message this an error
          if(response && response.type && response.message){
            alerts.addError(response);
          }
          $modalInstance.close();
        };

        $scope.onError = function(error) {
          alerts.addError(error);
          $modalInstance.dismiss('cancel');
        };


        $scope.cancel = function() {
          $modalInstance.dismiss('cancel');
        };
      }
    });

    modalInstance.result.then($scope.refreshAll);
  };

  /**
   * Toggles the name edition, to allow editing the name
   * or cancel the edition, and just display it
   * @param {Object} item - the item to rename
   */
  $scope.toggleItemEdition = function(item) {
    if (!item.isEditingName) {
      // backup old name to check if update is necessary later
      item.oldName = item.name;
    }    item.isEditingName = !item.isEditingName;
  };

  /**
   * Renames an item with a new name, only if the name has changed.
   * If it doesn't, than no http call is made, we just toggle the edition
   * @param {Object} item - the item to rename
   * @param {String} type Type of item to return (default: page)
   */
  $scope.renameItem = function(item, type) {

    var repository = $injector.get( (type || 'page') + 'Repo' );

    if (item.name !== item.oldName) {
      repository
        .rename(item.id, item.name)
        .then($scope.refreshAll);
    } else {

      /**
       * We need to defer the action of hidding the page because of the click event
       * When you click it will trigger:
       *   1. onBlur -> hide input
       *   2. click -> toggle input -> display input
       * So with a defferd action, the input is hidden on blur even if we click on da edit button
       */
      $timeout(function() {
        item.isEditingName = false;
      }, 100);
    }
  };

});
