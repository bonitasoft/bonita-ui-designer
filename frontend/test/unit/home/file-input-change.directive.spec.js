describe('directive fileInputChange', function() {

  var $document, scope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.home'));

  beforeEach(inject(function($injector) {
    var rootScope = $injector.get('$rootScope');
    var compile = $injector.get('$compile');
    $document = $injector.get('$document');

    scope = rootScope.$new();
    scope.filename = '';

    element = compile('<input type=\'file\' id=\'myinput\' file-input-change ng-model=\'filename\'>')(scope);
    $document.find('body').append(element);
    scope.$digest();
  }));

  afterEach(function() {
    angular.element('#myinput').remove();
  });

  it('should update ', function() {
    var event = {
      type: 'change',
      target: {
        files: [{
          name: 'filename.jpg'
        }]
      }
    };
    element.triggerHandler(event);
    expect(scope.filename).toBe('filename.jpg');
  });

});
