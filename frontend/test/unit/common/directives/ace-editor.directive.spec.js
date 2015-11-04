describe('ace-editor directive', function() {
  var $compile, element, template, scope, directiveScope;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    var $rootScope = _$rootScope_;

    scope = $rootScope.$new();
    scope.property = {};

    template = '<ace-editor ng-model=\'toto\' mode=\'javascript\'></ace-editor>';
    element = $compile(template)(scope);
    scope.$apply();

    directiveScope = element.isolateScope();

  }));

  it('should give default options and pass the wanted mode to ui-ace directive ', function() {
    expect(element.attr('ui-ace')).toBe('{ mode: \'javascript\', showGutter: true, onLoad: loaded }');
  });

  it('should hide print margin of ace editor when loaded', function() {
    var editor = {
      setShowPrintMargin: function() {},
      commands: {
        addCommand: function() {}
      }
    };
    spyOn(editor, 'setShowPrintMargin');
    directiveScope.loaded(editor);
    expect(editor.setShowPrintMargin).toHaveBeenCalledWith(false);
  });

  it('should enable auto-complete if auto-completion data object is provided', function() {
    var editor = {
      setOptions: function() {},
      setShowPrintMargin: function() {},
      commands: {
        addCommand: function() {}
      }
    };

    scope.data = { name: 'bob' };
    template = '<ace-editor mode=\'javascript\' ng-model=\'toto\' auto-completion=\'{{data}}\'></ace-editor>';
    element = $compile(template)(scope);
    scope.$apply();

    directiveScope = element.isolateScope();
    spyOn(editor, 'setOptions');
    directiveScope.loaded(editor);
    expect(editor.setOptions).toHaveBeenCalledWith({
      enableBasicAutocompletion: true,
      enableLiveAutocompletion: true
    });

  });
});
