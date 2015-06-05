describe('Service: modelFactory', function () {

  beforeEach(module('pb.generator.services'));

  var dataModel, template, scope, $httpBackend, $compile, $location, data = {
    foo: {
      type: 'variable',
      value: 'bar'
    },
    bar: {
      type: 'url',
      value: 'https://some.url.com/rest/{{ foo }}/foo'
    },
    baz: {
      type: 'json',
      value: '{ "foo": 5 }'
    },
    qux: {
      type: 'expression',
      value: 'return $data.baz.foo * 3;'
    },
    failingExpression: {
      type: 'expression',
      value: 'return $data.unknown.value;'
    },
    urlParamTime: {
      type: 'urlparameter',
      value: 'time'
    },
    urlParamToto: {
      type: 'urlparameter',
      value: 'toto'
    },
    failingUrlParam: {
      type: 'urlparameter',
      value: 'foo'
    },
    arrayExpression: {
      type: 'expression',
      value: 'return ["pierre", "paul", "jack"];'
    }
  };

  beforeEach(inject(function(modelFactory, $rootScope, _$compile_, _$httpBackend_, _$location_) {
    dataModel = modelFactory.create(data);
    scope = $rootScope.$new();
    scope.properties = dataModel;
    $httpBackend = _$httpBackend_;
    $compile = _$compile_;
    $location = _$location_;
    template = $compile('<div>{{ properties.qux }}</div>')(scope);
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  it('should read a simple variable', function () {
    expect(dataModel.foo).toBe('bar');
  });

  it('should read a json variable', function () {
    expect(dataModel.baz.foo).toBe(5);
  });

  it('should evaluate an expression', function () {
    expect(dataModel.qux).toBe(15);
  });

  it('should not throws exception when the expression evaluation fail', function () {
    expect(function() {
      return dataModel.failingExpression;
    }).not.toThrow();
  });

  it('should appear in a directive', function () {
    scope.$apply();
    expect(template.html()).toBe('15');

    dataModel.baz.foo = 13;
    scope.$apply();
    expect(template.html()).toBe('39');
  });

  it('should make a call to the url when accessing data for the first time', function () {
    $httpBackend.expectGET('https://some.url.com/rest/bar/foo').respond(200, ['foo', 'bar', 'baz']);
    expect(dataModel.bar).toBeUndefined();
    $httpBackend.flush();
    expect(dataModel.bar).toEqual(['foo', 'bar', 'baz']);
  });

  it('should make a call to the url when url change', function () {
    $httpBackend.expectGET('https://some.url.com/rest/bar/foo').respond(200, "foobar");
    expect(dataModel.bar).toBeUndefined();
    $httpBackend.flush();

    $httpBackend.expectGET('https://some.url.com/rest/baz/foo').respond(200, "foobaz");
    dataModel.foo = 'baz';
    expect(dataModel.bar).toEqual("foobar");
    $httpBackend.flush();

    expect(dataModel.bar).toEqual("foobaz");
  });

  it('should not make a call to the url when data is undefined', function () {
    dataModel.foo = undefined;

    expect(dataModel.bar).toBeUndefined();
  });

  xit('should not make a call to the url when data is null', function () {
    dataModel.foo = null;

    expect(dataModel.bar).toBeUndefined();
  });

  it('should make a call to the url and update template once fulfilled', function () {
    $httpBackend.expectGET('https://some.url.com/rest/bar/foo').respond(200, ['foo', 'bar', 'baz']);
    var template = $compile('<div>{{ properties.bar }}</div>')(scope);
    scope.$apply();

    expect(template.html()).toBe('');
    $httpBackend.flush();

    expect(template.html()).toBe('["foo","bar","baz"]');
  });

  it('should allow updating a variable', function () {
    var template = $compile('<div>{{ properties.foo }}</div>')(scope);
    scope.$apply();

    expect(template.html()).toBe('bar');
    dataModel.foo = 'foobar';
    scope.$apply();

    expect(template.html()).toBe('foobar');
  });

  it('should evaluate an urlparameter', function () {
    $location.absUrl = function(){
      return "http://domain.tld?toto=bob&time=123"
    }
    expect(dataModel.urlParamTime).toBe('123');
    expect(dataModel.urlParamToto).toBe('bob');
    expect(dataModel.failingUrlParam).toBe('');
  });

  it('should return the same reference when expression result did not change', function () {
    expect(dataModel.arrayExpression === dataModel.arrayExpression).toBeTruthy();
  });
});
