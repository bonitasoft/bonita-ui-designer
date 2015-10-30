describe('directive autofocus', function() {

  var $document, rootScope, compile, scope, timeout, dom;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function($injector) {

    rootScope = $injector.get('$rootScope');
    timeout = $injector.get('$timeout');
    compile = $injector.get('$compile');
    $document = $injector.get('$document');
    scope = rootScope.$new();
  }));

  afterEach(function() {
    angular.element('#toto').remove();
  });

  it('should set focus on the input', function() {
    dom = compile('<input id="toto" data-autofocus>')(scope);
    $document.find('body').append(dom);
    scope.$digest();
    timeout.flush();
    // is(':focus') does not work
    expect(angular.element('#toto').get(0) === document.activeElement).toBe(true);
  });

  it('should set the focus on an input after a watch', function() {
    scope.toto = false;
    dom = compile('<input id="toto" data-autofocus="toto">')(scope);
    $document.find('body').append(dom);
    scope.$digest();
    dom.scope().toto = true;
    scope.$digest();
    timeout.flush();
    expect(angular.element('#toto').get(0) === document.activeElement).toBe(true);
  });

  it('should set the focus on an ace-editor', function() {
    dom = compile('<ace-editor id="editor" ng-model="toto" mode="javascript" autofocus ></ace-editor>')(scope);
    var controller = dom.controller('aceEditor');

    spyOn(controller.editor, 'focus');

    scope.$digest();
    timeout.flush();
    $document.find('body').append(dom);
    expect(controller.editor.focus).toHaveBeenCalled();
  });

});
