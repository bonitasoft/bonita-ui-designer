describe('modal-container', function() {
  var $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<modal-container id="{{ id }}" container="modalContainer" editor="editor"></modal-container>';
    // when compiling with tabs container containing 2 tabs
    var row = [];

    $rootScope.modalContainer = {
      type: 'modalContainer',
      container: {
        rows: [ ]
      }
    };

    row.push($rootScope.container);
    $rootScope.id = 'modalContainer-1';
    $rootScope.editor = {
      isCurrentComponent: function() {
        return false;
      },
      isCurrentRow: function() {
        return false;
      }
    };

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the modalContainer', function() {
    // then we should have
    expect(element.find('container').length).toBe(1);
  });

});
