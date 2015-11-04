describe('errorInterceptor', function() {

  var alerts, $httpBackend, $http;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services'));

  beforeEach(inject(function(_$httpBackend_, _$http_, _alerts_) {
    $httpBackend = _$httpBackend_;
    $http = _$http_;
    alerts = _alerts_;
    spyOn(alerts, 'addError');
  }));

  it('should add an error if JSON error response with message', function() {
    var data = {
      type: 'SomeException',
      message: 'functional error'
    };
    $httpBackend.expectPUT('/foo').respond(400, data, { 'Content-Type': 'application/json' });
    $http.put('/foo');
    $httpBackend.flush();

    expect(alerts.addError).toHaveBeenCalledWith(data.message);
  });

  it('should add a generic error if non-JSON error response', function() {
    var data = 'Houston, we have a problem';
    $httpBackend.expectPUT('/foo').respond(400, data, { 'Content-Type': 'text/plain' });
    $http.put('/foo');
    $httpBackend.flush();

    expect(alerts.addError).toHaveBeenCalledWith('Unexpected server error');
  });

  it('should add a generic error if JSON error without message', function() {
    var data = {
      error: 'Houston, we have a problem'
    };
    $httpBackend.expectPUT('/foo').respond(400, data, { 'Content-Type': 'application/json' });
    $http.put('/foo');
    $httpBackend.flush();

    expect(alerts.addError).toHaveBeenCalledWith('Unexpected server error');
  });
});
