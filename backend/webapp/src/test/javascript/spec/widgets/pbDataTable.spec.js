describe('pbDataTable', function() {

  var $compile, scope, $httpBackend, humanTasks;
  var urlMatcher;

  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('org.bonitasoft.bonitable'));
  beforeEach(module('org.bonitasoft.templates'));
  beforeEach(module('org.bonitasoft.bonitable.sortable'));
  beforeEach(module('ui.bootstrap.pagination'));
  beforeEach(module('template/pagination/pagination.html'));
  beforeEach(module('pbDataTable.mock'));

  beforeEach(inject(function($injector, $rootScope) {
    $compile = $injector.get('$compile');
    $httpBackend = $injector.get('$httpBackend');
    humanTasks = $injector.get('humanTasks');

    scope = $rootScope.$new();
    scope.properties = {
      isBound: function() {
        return true;
      },
      pageSize: 2,
      headers: ['id', 'state', 'name'],
      columnsKey: ['id', 'state', 'name'],
      sortColumns: ['id', 'name'],
      apiUrl: '../API/bpm/humanTask',
      requestParams: {
        d:['rootContainerId']
      }
    };
    urlMatcher = new RegExp(scope.properties.apiUrl);
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  it('should add a sortable th to the header per headers passing through properties', function() {
    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    scope.$apply();
    $httpBackend.flush();
    var headers = [].map.call(element.find('th'), function(th) {
      return th.innerText.trim();
    });

    expect(headers).toEqual(['id', 'state', 'name']);
    expect(element.find('th button').length).toEqual(2);
  });

  it('should add a tr per row', function() {
    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    scope.$digest();
    $httpBackend.flush();

    expect(element.find('tbody').find('tr').length).toEqual(2);
  });

  it('should fill rows using columnsKey', function() {

    scope.properties.columnsKey = ['id', 'name'];
    scope.properties.headers = ['id', 'name'];

    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    scope.$digest();
    $httpBackend.flush();

    var rows = [].map.call(element.find('tbody').find('tr'), function(tr) {
      return [].map.call(angular.element(tr).find('td'), function(td) {
        return td.innerText.trim();
      });
    });

    expect(rows).toEqual([['2', 'A Étape1'], ['5', 'A Étape2']]);
  });

  it('should handle columnKey with nested properties', function() {
      scope.properties.headers = ['id', 'name'];
      scope.properties.columnsKey = ['id', 'rootContainerId.name'];

      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      $httpBackend.flush();
      scope.$apply();

      var rows = [].map.call(element.find('tbody').find('tr'), function(tr) {
        return [].map.call(angular.element(tr).find('td'), function(td) {
          return td.innerText.trim();
        });
      });

      expect(rows).toEqual([['2', 'Pool'], ['5', 'Pool']]);
  });


  it('should allow selecting row', function() {
    scope.properties.headers = ['id', 'name'];
    scope.properties.columnsKey = ['id', 'name'];
    scope.propertyValues = {
      selectedRow: {type: 'data'}
    };

    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    $httpBackend.flush();
    scope.$apply();

    element.find('tbody').find('tr').eq(1).triggerHandler('click');
    scope.$apply();

    expect(element.find('table').hasClass('table-hover')).toBeTruthy();
    expect(scope.properties.selectedRow).toEqual(jasmine.objectContaining(humanTasks[1]));
    expect(element.find('tbody').find('tr')[1].className).toContain('info');
  });

  it('should display a pagination with direction links and max 5 pages for desktop resolutions', function() {
    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    scope.$apply();
    $httpBackend.flush();

    var pagination =  [].map.call(element.find('.hidden-xs .pagination li'), function(li) {
      return li.innerText.trim();
    });
    expect(pagination).toEqual(['«', '‹', '1', '2', '3', '4', '5', '...', '›', '»']);
  });

  it('should display a pagination with direction links and max 3 pages for mobile resolutions', function() {
    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    scope.$apply();
    $httpBackend.flush();

    var pagination =  [].map.call(element.find('.visible-xs .pagination li'), function(li) {
      return li.innerText.trim();
    });
    expect(pagination).toEqual(['«', '‹', '1', '2', '3', '›', '»']);
  });

  describe('sort', function() {
    var william = {
      id: '1',
      name: 'william.jobs',
      jobTitle: 'CEO',
      birthDateInMillis: "612223200000"
    }, walter = {
      id: '2',
      name: 'walter.bates',
      jobTitle: 'manager',
      birthDateInMillis: "927756000000"
    }, jan = {
      id: '3',
      name: 'jan.fisher',
      jobTitle: 'janitor',
      birthDateInMillis: "549064800000"
    };
    beforeEach(function() {
      scope.properties.type = 'Bonita API';
      scope.properties.apiUrl = '../API/portal/page';
      scope.properties.columnsKey = ['id', 'name', 'jobTitle'];
      scope.properties.headers = ['Id', 'Name', 'Job title'];

    });
    it('should order with params when sortColumns is empty', function() {
      scope.properties.sortColumns = [];
      scope.properties.params = { o: "lastname ASC" };
      var element = $compile('<pb-data-table></pb-data-table>')(scope);

      $httpBackend.expectGET('../API/portal/page?c=2&o=lastname+ASC&p=0').respond(200, [william, walter, jan]);

      $httpBackend.flush();

      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['1', '2', '3']);
    });

    it('should not order when sortColumns is empty', function() {
      scope.properties.sortColumns = [];
      var element = $compile('<pb-data-table></pb-data-table>')(scope);

      $httpBackend.expectGET('../API/portal/page?c=2&p=0').respond(200, [william, walter, jan]);

      $httpBackend.flush();

      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['1', '2', '3']);
    });

    it('should order a list', function() {
      scope.properties.headers = ['Id', 'Name', 'Job title'];
      var element = $compile('<pb-data-table></pb-data-table>')(scope);

      $httpBackend.expectGET('../API/portal/page?c=2&o=id+ASC&p=0').respond(200, [william, walter, jan]);

      $httpBackend.flush();
      var sortButtons = element.find('.bo-SortButton');
      expect(sortButtons.length).toBe(2);
      expect($(sortButtons[0]).text().trim()).toEqual('Id');
      expect($(sortButtons[1]).text().trim()).toEqual('Name');

      $httpBackend.expectGET('../API/portal/page?c=2&o=id+DESC&p=0').respond(200, [jan, walter, william]);
      element.find('th:first-child .bo-SortButton').click();

      $httpBackend.flush();

      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['3', '2', '1']);
    });

    it('should order a list with filter in column keys', function() {
      scope.properties.columnsKey = ['id', 'name', 'jobTitle', 'birthDate | uiDate'];
      scope.properties.sortColumns = ['id', 'birthDate'];
      scope.properties.headers = ['Id', 'Name', 'Job title', 'Birth date'];
      var element = $compile('<pb-data-table></pb-data-table>')(scope);

      $httpBackend.expectGET('../API/portal/page?c=2&o=id+ASC&p=0').respond(200, [william, walter, jan]);

      $httpBackend.flush();
      var sortButtons = element.find('.bo-SortButton');
      expect(sortButtons.length).toBe(2);
      expect($(sortButtons[0]).text().trim()).toEqual('Id');
      expect($(sortButtons[1]).text().trim()).toEqual('Birth date');

      $httpBackend.expectGET('../API/portal/page?c=2&o=birthDate+ASC&p=0').respond(200, [jan, walter, william]);
      element.find('.bo-SortButton')[1].click();

      $httpBackend.flush();

      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['3', '2', '1']);
    });
  });

  it('should reload the call to the resource when params are updated', function() {
    scope.properties.apiUrl = '../API/portal/page?c=123';
    scope.properties.params = {};
    $compile('<pb-data-table></pb-data-table>')(scope);

    $httpBackend.expectGET('../API/portal/page?&c=2&o=id+ASC&p=0').respond(200, '');

    $httpBackend.flush();
    scope.properties.params.userId = '10';

    $httpBackend.expectGET('../API/portal/page?&c=2&o=id+ASC&p=0&userId=10').respond(200, '');
    $httpBackend.flush();
  });

  it('should not break table if url is empty ', function() {
    scope.properties.apiUrl = undefined;
    $compile('<pb-data-table></pb-data-table>')(scope);
    scope.$apply();
    scope.properties.apiUrl = '../API/portal/page?c=123';
    $httpBackend.expectGET('../API/portal/page?&c=2&o=id+ASC&p=0').respond(200, '');

    $httpBackend.flush();
  });

  it('should filter the call to the resource', function() {
    scope.properties.apiUrl = '../API/portal/page?c=123';
    scope.properties.filter = 'Vinc';
    $compile('<pb-data-table></pb-data-table>')(scope);

    $httpBackend.expectGET('../API/portal/page?&c=2&o=id+ASC&p=0&s=Vinc').respond(200, '');

    $httpBackend.flush();
  });

  it('should remove extra p and c parameter from URL', function() {
    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    $httpBackend.flush();
    var removeHandledParams = element.scope().ctrl.removeHandledParams;

    expect(removeHandledParams('../API/portal/page?c=41')).toEqual('../API/portal/page?');
    expect(removeHandledParams('../API/portal/page?c=41&test=test')).toEqual('../API/portal/page?test=test');
    expect(removeHandledParams('../API/portal/page?c=41#')).toEqual('../API/portal/page?#');
    expect(removeHandledParams('../API/portal/page?zerp=12&c=41')).toEqual('../API/portal/page?zerp=12');
    expect(removeHandledParams('../API/portal/page?page=12&c=41#')).toEqual('../API/portal/page?page=12#');
    expect(removeHandledParams('../API/portal/page?paze=12&c=41&pouet')).toEqual('../API/portal/page?paze=12&pouet');
  });

  it('should reinitialize pagination and update search when page size changes', function() {
    scope.properties.apiUrl = '../API/portal/page?p=12&c=41';
    var element = $compile('<pb-data-table></pb-data-table>')(scope);
    spyOn(element.scope().ctrl, 'updateResults').and.callThrough();
    $httpBackend.expectGET('../API/portal/page?&c=2&o=id+ASC&p=0').respond(200, '');
    $httpBackend.flush();

    scope.properties.pageSize = 5;
    $httpBackend.expectGET('../API/portal/page?&c=5&o=id+ASC&p=0').respond(200, '');

    $httpBackend.flush();

    var pages = element.find('.pagination li');
    expect(pages.length).toBe(0);
  });

  describe('json data', function() {

    beforeEach(function(){
      scope.properties = {
        isBound: function() {
          return true;
        },
        pageSize: 3,
        type: 'Variable',
        columnsKey: ['id', 'name'],
        sortColumns: ['id', 'name'],
        headers: ['id', 'name'],
        content: [
          {
            id: 1,
            name:'Jeanne',
            date_of_birth:"2020-12-24T11:22:28Z"
          }, {
            id: 2,
            name:'Serge',
            date_of_birth:"2021-03-26T00:01:00Z"
          },{
            id: 3,
            name:'Colin',
            date_of_birth:"2020-10-10T18:32:43Z"
          },{
            id: 4,
            name:'Phil',
            date_of_birth:"2021-02-17T21:13:35Z"
          },{
            id: 5,
            name:'Vincent',
            date_of_birth:"2020-09-06T22:23:33Z",
          },{
            id: 6,
            name:'Nathalie',
            date_of_birth:"2020-12-05T10:19:11Z"
          }
        ]
      };
    });

    it('should order a list with an angularJS filter as column key', function() {
      scope.properties.columnsKey = ['id', 'name', 'date_of_birth | uiDate'];
      scope.properties.sortColumns = ['date_of_birth | uiDate'];
      scope.properties.headers = ['Id', 'Name', 'Birth date'];
      let element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.$digest();
      let sortButtons = element.find('.bo-SortButton');
      expect(sortButtons.length).toBe(3);
      expect($(sortButtons[2]).text().trim()).toEqual('Birth date');

      element.find('.bo-SortButton')[2].click();

      let ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['5', '3', '6']);
    });

    it('should display a pagination', function() {
      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.$digest();

      var pagination =  [].map.call(element.find('.hidden-xs .pagination li'), function(li) {
        return li.innerText.trim();
      });
      expect(pagination).toEqual(['«', '‹', '1', '2', '›', '»']);
    });

    it('should navigate to a pagination', function() {
      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.$digest();
      element.find('.pagination li a').eq(3).click();

      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['4', '5', '6']);
    });

    it('should order a list', function() {
      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.$digest();
      element.find('th:first-child .bo-SortButton').click();

      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['6', '5', '4']);
    });

    it('should filter a list', function() {
      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.properties.filter = {};
      scope.$digest();

      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['1','2','3']);
      scope.properties.filter.name = 'Vinc';
      scope.$digest();

      ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['5']);

    });

    it('should add an item and refresh DOM', () => {
      scope.properties.pageSize = 10;
      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.$apply();
      expect(element.find('tbody').find('tr').length).toEqual(6);
      scope.properties.content.push({
            id: 7,
            name:'Julien'
          });
      scope.$apply();
      expect(element.find('tbody').find('tr').length).toEqual(7);
    });

    it('should reinitialize pagination when value is filtered', function() {
      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.$digest();
      element.find('.pagination li a').eq(1).click();

      scope.properties.filter = 'Vinc';
      scope.$digest();

      var pages = element.find('.pagination li');
      expect(pages.length).toBe(0);
    });

    it('should show elements when filter is null', function() {
      var element = $compile('<pb-data-table></pb-data-table>')(scope);
      scope.properties.filter = null;
      scope.$digest();
      var ids =  [].map.call(element.find('tr td:first-child'), function(td) {
        return td.innerText.trim();
      });

      expect(ids).toEqual(['1', '2', '3']);
    });
  });

});
