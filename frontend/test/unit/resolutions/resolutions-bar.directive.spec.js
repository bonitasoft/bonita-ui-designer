describe('Resolution bar', function() {
  var resolutions, element, $scope, vm, $stateParams;

  beforeEach(angular.mock.module('bonitasoft.designer.templates'));
  beforeEach(angular.mock.module('bonitasoft.designer.resolution'));
  beforeEach(angular.mock.module('uidesigner'));

  beforeEach(inject(function($injector, _$stateParams_, _$httpBackend_) {
    resolutions = $injector.get('resolutions');
    var rootScope = $injector.get('$rootScope');
    var $compile = $injector.get('$compile');
    $stateParams = _$stateParams_;
    var $httpBackend = _$httpBackend_;

    $scope = rootScope.$new();
    $scope.onChange = jasmine.createSpy('onChange');

    $httpBackend.whenGET('rest/config/isExperimental').respond(200, { data:{ isExperimental:false }});

    element = $compile(`
        <resolutions-bar
            id="resolutions"
            on-change="onChange"
            class="btn-group btn-group-lg">
        </resolutions-bar>`)($scope);
    $scope.$apply();
    vm = element.isolateScope().vm;
  }));

  it('should load the current resolution', function() {
    expect(vm.currentResolution).toBe(resolutions.get('md'));
  });

  it('should load resolutions', function() {
    expect(vm.resolutions).toBe(resolutions.all());
  });

  it('should update the currentResolution on update', function() {
    vm.update({ key: 'xs' });
    expect(vm.currentResolution.key).toBe('xs');
  });

  it('should call on-change when updating current resolution', function () {
    vm.update({ key: 'md' });
    expect($scope.onChange).toHaveBeenCalledWith({key: 'md'});
  });

  it('should set current selected resolution based on state params', function () {
    $stateParams.resolution = 'lg';
    $scope.$apply();
    expect(vm.currentResolution.key).toBe('lg');
  });

  describe('the dom created', function() {

    var length;

    beforeEach(function() {
      length = resolutions.all().length;
    });

    it('should create as many button we have in the resolutions', function() {
      expect(element.find('button').length).toBe(length);
    });

    it('should update the resolution on click', function() {
      spyOn(vm, 'update');

      expect(element.find('[ng-click]').length).toBe(length);
      expect(element.find('[ng-click]').get(0).getAttribute('ng-click').indexOf('update') > -1).toBe(true);
      element.find('[ng-click]').eq(0).click();
      expect(vm.update).toHaveBeenCalled();
    });

  });

});
