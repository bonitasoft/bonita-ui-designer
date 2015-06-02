describe('The route config', function() {

  var $rootScope, $location, $state, widgetRepo, pageRepo, whiteboard, $httpBackend, $q;

  beforeEach(module('uidesigner', 'ui.router', 'pb.templates'));

  beforeEach(inject(function($injector) {
    $rootScope = $injector.get('$rootScope');
    $location = $injector.get('$location');
    $state = $injector.get('$state');
    widgetRepo = $injector.get('widgetRepo');
    pageRepo = $injector.get('pageRepo');
    whiteboard = $injector.get('whiteboard');
    $httpBackend = $injector.get('$httpBackend');
    $q = $injector.get('$q');

    spyOn(whiteboard, 'initialize').and.returnValue({});
  }));

  it('should resolve the necessary for the page editor', function() {
    // when we go to the editor
    $location.url('en/pages/person?resolution=xs');
    $rootScope.$apply();

    // then we should call the resolve
    // a page as there is a name in URL
    expect(whiteboard.initialize).toHaveBeenCalledWith(pageRepo, 'person');

    // a controller
    expect($state.current.views['@designer'].controller).toBe('EditorCtrl');
    expect($state.current.views['data@designer.page'].controller).toBe('DataCtrl');
    expect($state.current.views['palette@designer.page'].controller).toBe('PaletteCtrl');
  });

  it('should resolve the necessary for the page preview', function() {
    // when we go to the editor
    $location.path('en/pages/person/preview');
    $rootScope.$apply();

    // a controller
    expect($state.current.views['@designer'].controller).toBe('PreviewCtrl');
  });

  it('should redirect to custom widget editor', function() {
    spyOn(widgetRepo, 'getById').and.returnValue($q.when({id: 'customLabel', label: 'Label'}));

    // when we go to the custom widget editor
    $location.path('en/widget/customLabel');
    $rootScope.$apply();

    // then we should have
    // resolve widgets
    expect(widgetRepo.getById).toHaveBeenCalledWith('customLabel');
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

    $httpBackend.expectGET('i18n/lang-template-fr.json').respond(200, '');

    $location.url('fr/home');

    $httpBackend.flush();
  });

  it('should load deutsch translations', function() {

    $httpBackend.expectGET('i18n/lang-template-de.json').respond(200, '');

    $location.url('de/home');

    $httpBackend.flush();
  });

  it('should load spanish translations', function() {

    $httpBackend.expectGET('i18n/lang-template-es-ES.json').respond(200, '');

    $location.url('es/home');

    $httpBackend.flush();
  });

  it('should load italian translations', function() {

    $httpBackend.expectGET('i18n/lang-template-it.json').respond(200, '');

    $location.url('it/home');

    $httpBackend.flush();
  });

  it('should load portuguese translations', function() {

    $httpBackend.expectGET('i18n/lang-template-pt-BR.json').respond(200, '');

    $location.url('pt/home');

    $httpBackend.flush();
  });
});
