describe('Service: urlResolver', function () {

  beforeEach(module('bonitasoft.ui.services'));

  let model, $rootScope, resolver, $http, resolverService;

  beforeEach(inject((ResolverService, $location, _$rootScope_, $httpBackend) => {
    $rootScope = _$rootScope_;
    $http= $httpBackend;
    resolverService= ResolverService;

  }));

  it('should return a response with status and headers', () => {
    model = {};
    // returns the current list of phones
    var users = [{name: 'Andre'}, {name: 'Michel'}];
    let mockHeaders = {'content-range': '0-50/1'};
    $http.whenGET('../API/users?c=10&p=0').respond(200,users,mockHeaders);

    resolver = resolverService.createResolver(model, 'users', {
      type: 'url',
      displayValue: '../API/users?c=10&p=0'
    });

    $http.whenGET('../API/users?c=10&p=0').respond(200,users,mockHeaders);

    resolver.resolve();
    resolver.watchDependencies();
    $http.flush();

    expect(model.users).toContain({name: 'Andre'},{name: 'Michel'});
    expect(model.users.__status).toBe(200);
    expect(model.users.__headers).toEqual({'content-range':'0-50/1'});
  });

  it('should return catch and return information when error is throws', () => {
    model = {};
    // returns the current list of phones
    let users = [{}];
    let mockHeaders = {'content-type': 'json'};
    $http.whenGET('../API/users?c=10&p=0').respond(404,users,mockHeaders);

    resolver = resolverService.createResolver(model, 'users', {
      type: 'url',
      displayValue: '../API/users?c=10&p=0'
    });

    resolver.resolve();
    resolver.watchDependencies();
    $http.flush();

    expect(model.users).toContain({});
    expect(model.users.__status).toBe(404);
    expect(model.users.__headers).toEqual({'content-type': 'json'});
  });
});
