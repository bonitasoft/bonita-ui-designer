describe('The route config', function() {

  var $rootScope, $location, $state, widgetRepo, pageRepo, fragmentRepo, editorService, $httpBackend, $q, migration;

  beforeEach(angular.mock.module('uidesigner'));

  beforeEach(inject(function($injector) {
    $rootScope = $injector.get('$rootScope');
    $location = $injector.get('$location');
    $state = $injector.get('$state');
    widgetRepo = $injector.get('widgetRepo');
    pageRepo = $injector.get('pageRepo');
    fragmentRepo = $injector.get('fragmentRepo');
    editorService = $injector.get('editorService');
    $httpBackend = $injector.get('$httpBackend');
    $q = $injector.get('$q');
    migration = $injector.get('migration');

    spyOn(editorService, 'initialize').and.returnValue({});
    spyOn(widgetRepo, 'migrate').and.returnValue($q.when({}));
    spyOn(widgetRepo, 'migrationStatus').and.returnValue($q.when({data: {migration: false, compatible: true}}));
    spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when({}));
    spyOn(migration, 'handleMigrationNotif');
  }));

  it('should resolve the necessary for the page editor', function() {
    // when we go to the editor
    $location.url('en/pages/person?resolution=xs');
    $rootScope.$apply();

    // then we should call the resolve
    // a page as there is a name in URL
    expect(editorService.initialize).toHaveBeenCalledWith(pageRepo, 'person');

    // a controller
    expect($state.current.views['@designer'].controller).toBe('EditorCtrl');
    expect($state.current.views['data@designer.page'].controller).toBe('DataCtrl');
  });

  it('should resolve the necessary for the page preview', function() {
    // when we go to the editor
    $location.path('en/preview/page/person');
    $rootScope.$apply();

    // a controller
    expect($state.current.views['@designer'].controller).toBe('PreviewCtrl');
  });

  it('should redirect to custom widget editor', function() {
    spyOn(widgetRepo, 'load').and.returnValue($q.when({ id: 'customLabel', label: 'Label' }));

    // when we go to the custom widget editor
    $location.path('en/widget/customLabel');
    $rootScope.$apply();

    // then we should have
    // resolve widgets
    expect(widgetRepo.load).toHaveBeenCalledWith('customLabel');
    expect(widgetRepo.migrationStatus).toHaveBeenCalledWith('customLabel');
    expect(widgetRepo.load).toHaveBeenCalledWith('customLabel');
    expect($state.current.views['@designer'].controller).toBe('CustomWidgetEditorCtrl');
  });

  it('should redirect to home otherwise', function() {

    // when we go to the editor
    $location.path('/unknown');
    $rootScope.$apply();

    // then we should go to the editor
    expect($state.current.name).toBe('designer.home');
  });

  it('should load french translations', function() {

    $httpBackend.expectGET('i18n/lang-template-fr-FR.json').respond(200, '');
    $httpBackend.expectGET('i18n/widgets-lang-template-fr-FR.json').respond(200, '');

    $location.url('fr/home');

    $httpBackend.flush();
  });

  it('should load spanish translations', function() {

    $httpBackend.expectGET('i18n/lang-template-es-ES.json').respond(200, '');
    $httpBackend.expectGET('i18n/widgets-lang-template-es-ES.json').respond(200, '');

    $location.url('es/home');

    $httpBackend.flush();
  });

  it('should resolve the necessary for the fragment editor', function() {
    // when we go to the editor
    $location.url('en/fragments/person');
    $rootScope.$apply();

    // then we should call the resolve
    // a page as there is a name in URL
    expect(editorService.initialize).toHaveBeenCalledWith(fragmentRepo, 'person');

    // a controller
    expect($state.current.views['@designer'].controller).toBe('EditorCtrl');
    expect($state.current.views['data@designer.fragment'].controller).toBe('DataCtrl');
  });

  it('should resolve the necessary for the fragment preview', function() {
    // when we go to the editor
    $location.path('en/preview/fragment/person');
    $rootScope.$apply();

    // a controller
    expect($state.current.views['@designer'].controller).toBe('PreviewCtrl');
  });
});
