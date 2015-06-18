describe('directive pb-datepicker-utc', function(){
  var $scope, $compile;

  beforeEach(module('pb.generator.directives'));

  beforeEach(inject(function($injector){
    $compile = $injector.get('$compile');
    $scope = $injector.get('$rootScope').$new()
    $scope.value = 1434672000000;
  }));


  it('require a ngModel directive', function(){
    function test(html, scope){
      var element = $compile(html)(scope);
      scope.$apply();
    }
    expect(test.bind(null, '<input pb-datepicker-utc />', $scope)).toThrow();
    expect(test.bind(null, '<input pb-datepicker-utc ng-model="value" />', $scope)).not.toThrow();
  });

  it('should convert ngModel number value to a Date', function(){
    var element = $compile('<input pb-datepicker-utc ng-model="value" />')($scope);
    $scope.$apply();
    expect($scope.value instanceof Date).toBe(true);
  });

  it('should reset hours, minutes, seconds and milliseconds to 0', function(){
    $scope.value = Date.now();
    var date = new Date($scope.value);
    var element = $compile('<input pb-datepicker-utc ng-model="value" />')($scope);
    $scope.$apply();
    expect($scope.value.getUTCDate()).toBe(date.getUTCDate());
    expect($scope.value.getUTCMonth()).toBe(date.getUTCMonth());
    expect($scope.value.getUTCFullYear()).toBe(date.getUTCFullYear());
    expect($scope.value.getUTCHours()).toBe(0);
    expect($scope.value.getUTCMinutes()).toBe(0);
    expect($scope.value.getUTCSeconds()).toBe(0);
    expect($scope.value.getUTCMilliseconds()).toBe(0);
  });

});
