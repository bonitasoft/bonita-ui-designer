describe('Service: urlResolver', function () {

  beforeEach(module('bonitasoft.ui.services'));

  let model, $rootScope, resolver, $http, resolverService;

  beforeEach(inject((ResolverService, $location, _$rootScope_, $httpBackend) => {
    $rootScope = _$rootScope_;
    $http = $httpBackend;
    resolverService = ResolverService;

  }));

  it('should return a response with status and headers', () => {
    model = {headers: "", statusCode: ""};
    // returns the current list of phones
    var users = [{name: 'Andre'}, {name: 'Michel'}];
    let mockHeaders = {'content-range': '0-50/1'};
    $http.whenGET('../API/users?c=10&p=0').respond(200, users, mockHeaders);

    resolver = resolverService.createResolver(model, 'users', {
      type: 'url',
      displayValue: '../API/users?c=10&p=0',
      advancedOptions: {headers: "headers", statusCode: "statusCode"}
    });

    $http.whenGET('../API/users?c=10&p=0').respond(200, users, mockHeaders);

    resolver.resolve();
    resolver.watchDependencies();
    $http.flush();

    expect(model.users).toContain({name: 'Andre'}, {name: 'Michel'});
    expect(model.statusCode).toBe(200);
    expect(model.headers).toEqual({'content-range': '0-50/1'});
  });

  it('should return catch and return information when error is throws', () => {
    model = {headers: "", statusCode: ""};
    // returns the current list of phones
    let users = [{}];
    let mockHeaders = {'content-type': 'json'};
    $http.whenGET('../API/users?c=10&p=0').respond(404, users, mockHeaders);

    resolver = resolverService.createResolver(model, 'users', {
      type: 'url',
      displayValue: '../API/users?c=10&p=0',
      advancedOptions: {headers: "headers", statusCode: "statusCode"}
    });

    resolver.resolve();
    resolver.watchDependencies();
    $http.flush();

    expect(model.users).toBe(undefined);
    expect(model.statusCode).toBe(404);
    expect(model.headers).toEqual({'content-type': 'json'});
  });

  it('should store response result in complex json object', () => {
    model = {};
    // returns the current list of phones
    var users = [{name: 'Andre'}, {name: 'Michel'}];
    let mockHeaders = {'content-range': '0-50/1'};
    $http.whenGET('../API/users?c=10&p=0').respond(200, users, mockHeaders);

    resolver = resolverService.createResolver(model, 'users', {
      type: 'url',
      displayValue: '../API/users?c=10&p=0',
      advancedOptions: {headers: "userList.complex.headers", statusCode: "userList.complex.statusCode"}
    });

    $http.whenGET('../API/users?c=10&p=0').respond(200, users, mockHeaders);

    resolver.resolve();
    resolver.watchDependencies();
    $http.flush();

    expect(model.users).toContain({name: 'Andre'}, {name: 'Michel'});
    expect(model.userList.complex.statusCode).toBe(200);
    expect(model.userList.complex.headers).toEqual({'content-range': '0-50/1'});
  });

  it('should store response result in complex json object', () => {
    model = {};
    // returns the current list of phones
    var users = [{name: 'Andre'}, {name: 'Michel'}];
    let mockHeaders = {'content-range': '0-50/1'};
    $http.whenGET('../API/userss?c=10&p=0').respond(404, "API not defined", mockHeaders);

    resolver = resolverService.createResolver(model, 'users', {
      type: 'url',
      displayValue: '../API/userss?c=10&p=0',
      advancedOptions: {headers: "userList.complex.headers",
        statusCode: "userList.complex.statusCode",
        failedResponseValue: "userList.complex.failedValue"}
    });

    $http.whenGET('../API/userss?c=10&p=0').respond(404, users, mockHeaders);

    resolver.resolve();
    resolver.watchDependencies();
    $http.flush();

    expect(model.users).toEqual(undefined);
    expect(model.userList.complex.failedValue).toContain('API not defined');
    expect(model.userList.complex.statusCode).toBe(404);
    expect(model.userList.complex.headers).toEqual({'content-range': '0-50/1'});
  });
});
