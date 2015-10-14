describe('HomeCtrl', function () {
  var $scope, $q, $modal, pageRepo, widgetRepo, pages, widgets, $state, $timeout;

  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(inject(function ($controller, $rootScope, $injector) {
    $scope = $rootScope.$new();
    $q = $injector.get('$q');
    $modal = $injector.get('$modal');
    pageRepo = $injector.get('pageRepo');
    widgetRepo = $injector.get('widgetRepo');
    $state = $injector.get('$state');
    $timeout = $injector.get('$timeout');

    pages = [
      {
        id: 'page1',
        name: 'Page 1'
      }
    ];
    widgets = [
      {
        id: 'widget1',
        name: 'Widget 1',
        custom: true
      }
    ];

    spyOn(pageRepo, 'all').and.returnValue($q.when(pages));
    spyOn(widgetRepo, 'customs').and.returnValue($q.when(widgets));

    $controller('HomeCtrl', {
      $scope: $scope,
      pageRepo: pageRepo,
      widgetRepo: widgetRepo
    });
    $scope.$digest();
  }));

  it('should expose a new empty page in the scope', function () {
    expect($scope.page.name).toBe('');
    expect($scope.page.rows).toEqual([[]]);
  });

  it('should create a page and navigate to the page editor', function () {
    var createdPage = {
      id: 'foo'
    };
    spyOn(pageRepo, 'create').and.returnValue($q.when(createdPage));
    spyOn($state, 'go');

    $scope.createPage($scope.page);
    $scope.$apply();
    expect($state.go).toHaveBeenCalledWith('designer.page', {id: 'foo'});
  });

  it('should create a widget and navigate to the widget editor', function () {
    var createdWidget = {
      id: 'foo'
    };
    spyOn(widgetRepo, 'create').and.returnValue($q.when(createdWidget));
    spyOn($state, 'go');

    $scope.createWidget('foo');
    $scope.$apply();
    expect($state.go).toHaveBeenCalledWith('designer.widget', {widgetId: 'foo'});
  });

  it('should open a confirmation dialog to confirm page deletion', function () {
    // given a fake modal service
    spyOn(pageRepo, 'delete').and.returnValue($q.when());
    spyOn($scope, 'refreshAll');
    var fakeModal = {
      result: $q.when('page1')
    };
    spyOn($modal, 'open').and.returnValue(fakeModal);
    $scope.deletePage({
      id: 'page1',
      name: 'Page 1'
    });
    expect($modal.open).toHaveBeenCalled();

    // then the result callback should have been called
    $scope.$apply();

    expect(pageRepo.delete).toHaveBeenCalledWith('page1');
    expect($scope.refreshAll).toHaveBeenCalled();

  });

  it('should export a page', function () {
    var page = {id: 'aPage'};
    spyOn(pageRepo, 'exportUrl');

    $scope.exportPageUrl(page);
    $scope.$apply();

    expect(pageRepo.exportUrl).toHaveBeenCalledWith(page);
  });

  it('should export a widget', function () {
    var widget = {id: 'idWidget'};
    spyOn(widgetRepo, 'exportUrl');

    $scope.exportWidgetUrl(widget);
    $scope.$apply();

    expect(widgetRepo.exportUrl).toHaveBeenCalledWith(widget);
  });

  it('should open a confirmation dialog to confirm widget deletion', function () {
    // given a fake modal service
    spyOn(widgetRepo, 'delete').and.returnValue($q.when());
    spyOn($scope, 'refreshAll');
    var fakeModal = {
      result: $q.when('widget1')
    };
    spyOn($modal, 'open').and.returnValue(fakeModal);

    // when deleting widget
    $scope.deleteCustomWidget({
      id: 'widget1',
      name: 'widget 1'
    });
    expect($modal.open).toHaveBeenCalled();

    // then the result callback should have been called
    $scope.$apply();

    expect(widgetRepo.delete).toHaveBeenCalledWith('widget1');
    expect($scope.refreshAll).toHaveBeenCalled();
  });

  it('should refresh everything', function () {

    $scope.refreshAll();
    $scope.$apply();

    expect(pageRepo.all).toHaveBeenCalled();
    expect($scope.pages).toEqual(pages);
    expect(widgetRepo.customs).toHaveBeenCalled();
    expect($scope.widgets).toEqual(widgets);
  });

  it('should toggle to edit a page name', function () {
    var page = {name: 'hello'};

    $scope.toggleItemEdition(page);

    expect(page.oldName).toBe('hello');
    expect(page.isEditingName).toBe(true);

    $scope.toggleItemEdition(page);

    expect(page.isEditingName).toBe(false);
  });

  it('should rename a page if the name has changed', function () {
    // given a page with a new name
    var page = {name: 'hello', oldName: 'oldHello'};
    spyOn(pageRepo, 'rename').and.returnValue($q.when());
    spyOn($scope, 'refreshAll').and.returnValue($q.when());

    // when renaming the page
    $scope.renameItem(page);
    $scope.$apply();

    // then it should have save the name and refresh the page
    expect(page.oldName).toBe('oldHello');
    expect(page.name).toBe('hello');
    expect(pageRepo.rename).toHaveBeenCalled();
    expect($scope.refreshAll).toHaveBeenCalled();
  });

  it('should revert the name if the rename of the page has failed', function () {
    var page = {name: 'hello', oldName: 'oldHello'};
    spyOn(pageRepo, 'rename').and.returnValue($q.reject());
    spyOn($scope, 'refreshAll').and.returnValue($q.when());

    $scope.renameItem(page);
    $scope.$apply();

    expect(page.name).toBe('oldHello');
    expect(pageRepo.rename).toHaveBeenCalled();
    expect($scope.refreshAll).toHaveBeenCalled();
  });

  it('should not rename a page if the name has not changed', function () {
    // given a page with the same name as a new name
    var page = {name: 'hello', oldName: 'hello'};
    spyOn(pageRepo, 'rename').and.returnValue($q.when());
    spyOn($scope, 'toggleItemEdition');

    // when renaming the page
    $scope.renameItem(page);
    $scope.$apply();
    // then it should not save and just toggle edition mode
    expect(page.oldName).toBe('hello');
    expect(page.name).toBe('hello');
    expect(pageRepo.rename).not.toHaveBeenCalled();

    $timeout.flush();
    expect(page.isEditingName).toBe(false);
  });
  describe('manageImportReport', () => {
    it('should open modal for file selection ', () => {
      var result = {}, title = 'Import a page', type = 'page', report = {};
      spyOn($modal, 'open').and.returnValue({result});

      expect($scope.manageImportReport(title, type, report)).toEqual(result);

      var [[args]] = $modal.open.calls.allArgs();
      expect(args.windowClass).toEqual('modal-centered');
      expect(args.resolve.importReport()).toEqual(report);
      expect(args.resolve.type()).toEqual(type);
      expect(args.resolve.title()).toEqual(title);
    });
  });
  describe('importElement', () => {
    it('should open modal and call manage report when report is returned and import is forced', function() {
      var deferredModal = $q.defer(), result = deferredModal.promise, title = 'Import a page', type = 'page', report = {}, deferredManageImport = $q.defer();
      spyOn($modal, 'open').and.returnValue({result});
      spyOn($scope, 'refreshAll');
      spyOn($scope, 'manageImportReport').and.returnValue(deferredManageImport.promise);
      deferredManageImport.resolve();
      deferredModal.resolve(report);
      
      $scope.importElement(type, title);
      $scope.$apply();
      var [[args]] = $modal.open.calls.allArgs();
      
      expect(args.windowClass).toEqual('modal-centered');
      expect(args.resolve.type()).toEqual(type);
      expect(args.resolve.title()).toEqual(title);
      expect($scope.refreshAll).toHaveBeenCalled();
      expect($scope.manageImportReport).toHaveBeenCalled();
    });
    it('should open modal and call manage report when report is returned and forced import is cancelled', function() {
      var deferredModal = $q.defer(), result = deferredModal.promise, title = 'Import a page', type = 'page', report = {}, deferredManageImport = $q.defer();
      spyOn($modal, 'open').and.returnValue({result});
      spyOn($scope, 'refreshAll');
      spyOn($scope, 'manageImportReport').and.returnValue(deferredManageImport.promise);
      deferredModal.resolve(report);
      deferredManageImport.reject();

      $scope.importElement(type, title);
      $scope.$apply();
      
      expect($scope.refreshAll).not.toHaveBeenCalled();
      expect($scope.manageImportReport).toHaveBeenCalled();
    });
    it('should open modal and don\'t call manage report when no report is returned', function() {
      var deferredModal = $q.defer(), result = deferredModal.promise, title = 'Import a page', type = 'page';
      spyOn($modal, 'open').and.returnValue({result});
      spyOn($scope, 'refreshAll');
      spyOn($scope, 'manageImportReport');
      deferredModal.resolve();

      $scope.importElement(type, title);
      $scope.$apply();
      
      expect($scope.refreshAll).toHaveBeenCalled();
      expect($scope.manageImportReport).not.toHaveBeenCalled();
    });
  });

});
