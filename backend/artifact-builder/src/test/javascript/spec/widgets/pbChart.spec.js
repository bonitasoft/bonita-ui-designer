describe('pbChart', function () {

  var $compile, scope, element;

  beforeEach(module('bonitasoft.ui.widgets', 'chart.js'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {};
  }));

  beforeEach(function () {
    scope.properties = {
      colors: [],
      type: 'Pie',
      data: [65, 59, 80, 81, 56, 55, 40],
      labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July']
    };
    element = $compile('<pb-chart></pb-chart>')(scope);
    scope.$apply();
  });

  describe('directive', function () {

    it('should draw a graph', function () {
      scope.$apply();

      expect(element.find('div.chart-container').length).toBe(1);
    });

    it('should draw a svg in ui designer editor', function () {
      scope.environment = {
        editor: true
      };
      scope.$apply();

      expect(element.find('svg').length).toBe(1);
    });
  });

  describe('controller', function() {

    it('should wrap data for multi series chart when data is a simple array', function () {
      scope.properties.data = [65, 59, 80, 81, 56, 55, 40];
      scope.properties.type = 'Line';
      scope.$apply();

      expect(scope.data).toEqual([scope.properties.data]);
    });

    it('should not wrap data for multi series chart when data is an array of array', function () {
      scope.properties.data = [[65, 59, 80, 81, 56, 55, 40]];
      scope.properties.type = 'Line';
      scope.$apply();

      expect(scope.data).toEqual(scope.properties.data);
    });

    it('should not wrap data for single series chart', function () {
      scope.properties.data = [65, 59, 80, 81, 56, 55, 40];
      scope.properties.type = 'Pie';
      scope.$apply();

      expect(scope.data).toEqual(scope.properties.data);
    });

    it('should watch properties colours and set colours to null when undefined', function () {
      scope.properties.colors = undefined;
      scope.$apply();

      expect(scope.colors).toEqual(null);
    });

    it('should watch properties colours and set colours to null when empty', function () {
      scope.properties.colors = [];
      scope.$apply();

      expect(scope.colors).toEqual(null);
    });

    it('should watch properties colours and set colours to array when not empty', function () {
      scope.properties.colors = ['#efefef'];
      scope.$apply();

      expect(scope.colors).toEqual(scope.properties.colors);
    });
  });

});
