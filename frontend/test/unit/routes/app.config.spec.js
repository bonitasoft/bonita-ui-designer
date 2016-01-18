describe('The route config', function() {

  var $rootScope, $location, $state, widgetRepo, pageRepo, editorService, $httpBackend, $q;

  beforeEach(angular.mock.module('uidesigner'));

  beforeEach(inject(function($injector) {
    $rootScope = $injector.get('$rootScope');
    $location = $injector.get('$location');
    $state = $injector.get('$state');
    widgetRepo = $injector.get('widgetRepo');
    pageRepo = $injector.get('pageRepo');
    editorService = $injector.get('editorService');
    $httpBackend = $injector.get('$httpBackend');
    $q = $injector.get('$q');

    spyOn(editorService, 'initialize').and.returnValue({});
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
    $location.path('en/pages/person/preview');
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

    $location.url('fr/home');

    $httpBackend.flush();
  });

  it('should load spanish translations', function() {

    $httpBackend.expectGET('i18n/lang-template-es-ES.json').respond(200, '');

    $location.url('es/home');

    $httpBackend.flush();
  });

});
