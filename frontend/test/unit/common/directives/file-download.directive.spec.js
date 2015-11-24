describe('fileDownload directive', function() {

  var $document, scope, element, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.home'));

  beforeEach(inject(function(_$httpBackend_,$injector) {
    var rootScope = $injector.get('$rootScope');
    var compile = $injector.get('$compile');
    $httpBackend = _$httpBackend_;
    $document = $injector.get('$document');

    scope = rootScope.$new();

    element = compile('<button file-download href="rest/download/url">')(scope);
    $document.find('body').append(element);
    scope.$digest();
  }));

  afterEach(function() {
    angular.element($document).find('iframe').remove();
  });

  it('should download expected url', function() {
    $httpBackend.expectGET('rest/download/url').respond(200);

    element.trigger('click');
    $httpBackend.flush();

    expect( $document.find('iframe')[0].getAttribute('src')).toEqual('rest/download/url');
  });

  it('should intercept error if bad url', function() {
    $httpBackend.expectGET('rest/download/url').respond(404);

    element.trigger('click');
    $httpBackend.flush();

    expect( $document.find('iframe')[0].getAttribute('src')).toEqual('');
  });

  it('should unregister event on element when scope is destroyed', function() {
    scope.$destroy();
    element.trigger('click');

    expect($httpBackend.verifyNoOutstandingExpectation).not.toThrow();
    expect( $document.find('iframe')[0].getAttribute('src')).toEqual('');
  });

});
