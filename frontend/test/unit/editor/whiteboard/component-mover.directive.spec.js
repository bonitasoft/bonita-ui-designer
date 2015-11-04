describe('componentMover', function() {
  var $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<component-mover on-delete="editor.deleteComponent()" component="component2"></component>';
    // when compiling with an input
    var row = [];
    var parentContainerRow = {
      row: row
    };
    $rootScope.component1 = {
      $$parentContainerRow: parentContainerRow
    };
    $rootScope.component2 = {
      $$parentContainerRow: parentContainerRow
    };
    $rootScope.component3 = {
      $$parentContainerRow: parentContainerRow
    };
    row.push($rootScope.component1);
    row.push($rootScope.component2);
    row.push($rootScope.component3);

    $rootScope.editor = {
      deleteComponent: function() {
        return 'toBeSpyed';
      }

    };

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the component mover and move the component', function() {
    // then we should have
    expect(element.find('.btn-caption.fa-arrow-circle-left').length).toBe(1);
    expect(element.find('.btn-caption.fa-arrow-circle-right').length).toBe(1);

    element.find('.btn-caption.fa-arrow-circle-left').click();
    expect($rootScope.component2.$$parentContainerRow.row[0]).toBe($rootScope.component2);
    expect(element.find('.btn-caption.fa-arrow-circle-left').length).toBe(0);
    expect(element.find('.btn-caption.fa-arrow-circle-right').length).toBe(1);

    element.find('.btn-caption.fa-arrow-circle-right').click();
    element.find('.btn-caption.fa-arrow-circle-right').click();

    expect($rootScope.component2.$$parentContainerRow.row[2]).toBe($rootScope.component2);
    expect(element.find('.btn-caption.fa-arrow-circle-left').length).toBe(1);
    expect(element.find('.btn-caption.fa-arrow-circle-right').length).toBe(0);
  });

  it('should delete the component', function() {
    spyOn($rootScope.editor, 'deleteComponent');

    element.find('.btn-caption.fa-times-circle').click();

    expect($rootScope.editor.deleteComponent).toHaveBeenCalled();
  });
});
