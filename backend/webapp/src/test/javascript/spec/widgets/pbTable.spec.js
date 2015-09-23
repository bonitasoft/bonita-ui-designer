describe('pbTable', function() {

  var $compile, scope;

  beforeEach(module('bonitasoft.ui.widgets'));

  beforeEach(inject(function(_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = { isBound: function() { return true; }};
  }));

  it('should add a th to the header per headers passing through properties', function() {
    scope.properties.headers = ['foo', 'bar', 'baz'];
    var element = $compile('<pb-table></pb-table>')(scope);
    scope.$apply();

    var headers = [].map.call(element.find('th'), function(th) {
      return th.innerText.trim();
    });

    expect(headers).toEqual(['foo', 'bar', 'baz']);
  });

  it('should add a tr per row', function() {
    scope.properties.content = ['first', 'second', 'third'];
    var element = $compile('<pb-table></pb-table>')(scope);
    scope.$apply();

    var rows = [].map.call(element.find('tbody').find('tr'), function(tr) {
      return tr.innerText.trim();
    });

    expect(rows).toEqual(['first', 'second', 'third']);
  });

  it('should fill rows using columnsKey', function() {
    scope.properties.content = [{id: '42', content: 'hello'}, {id: '43', content: 'salut'}];
    scope.properties.columnsKey = ['id', 'content'];
    var element = $compile('<pb-table></pb-table>')(scope);
    scope.$apply();

    var rows = [].map.call(element.find('tbody').find('tr'), function(tr) {
      return [].map.call(angular.element(tr).find('td'), function(td) {
        return td.innerText.trim();
      });
    });

    expect(rows).toEqual([['42', 'hello'], ['43', 'salut']]);
  });

  it('should handle columnKey with nested properties', function() {
      scope.properties.content = [{id: '42', content: {name: 'hello'}}, {id: '43', content: {name: 'salut'}}];
      scope.properties.columnsKey = ['id', 'content.name'];
      var element = $compile('<pb-table></pb-table>')(scope);
      scope.$apply();

      var rows = [].map.call(element.find('tbody').find('tr'), function(tr) {
        return [].map.call(angular.element(tr).find('td'), function(td) {
          return td.innerText.trim();
        });
      });

      expect(rows).toEqual([['42', 'hello'], ['43', 'salut']]);
    });

  it('should not show tbody if rows isn\'t an array', function() {
    scope.properties.content = '';
    var element = $compile('<pb-table></pb-table>')(scope);
    scope.$apply();

    expect(element.find('tbody').length).toBe(0);
  });

  it('should allow selecting row', function() {
    scope.properties.content = ['first', 'second', 'third', 'last'];
    scope.propertyValues = {
      selectedRow: {type: 'data'}
    };
    var element = $compile('<pb-table></pb-table>')(scope);
    scope.$apply();
    clickOn(element.find('tbody').find('tr')[1]);

    expect(element.find('table').hasClass('table-hover')).toBeTruthy();
    expect(scope.properties.selectedRow).toBe('second');
    expect(element.find('tbody').find('tr')[1].className).toContain('info');
  });

  it('should give feedback when initial value is provided for selected row', function() {
    scope.properties.content = [{id: '1'}, {id: '2'}, {id: '3'}];
    scope.properties.selectedRow =  {id: '2'};

    var element = $compile('<pb-table></pb-table>')(scope);
    scope.$apply();

    expect(element.find('tbody').find('tr')[1].className).toContain('info');
  });

  function clickOn(row) {
    angular.element(row).triggerHandler('click');
  }
});
