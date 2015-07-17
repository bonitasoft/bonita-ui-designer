describe('pendingStatus service', function() {
  var service, $timeout;
  beforeEach(module('bonitasoft.ui.services'));
  beforeEach(inject(function($injector) {
    service = $injector.get('pendingStatus');
    $timeout = $injector.get('$timeout');
  }));

  it('should track a pending request', function(){
    expect(service.isPending()).toBe(false);
    service.addPendingRequest()
    expect(service.isPending()).toBe(true);
    service.removePendingRequest()
    expect(service.isPending()).toBe(false);
  });

  it('should trigger listener just after listen', function(){
    var handler1 = jasmine.createSpy('handler1');
    service.listen(handler1)
    $timeout.flush();
    expect(handler1).toHaveBeenCalled();
  });

  it('should trigger listener when pending request equal 0', function(){
    var handler1 = jasmine.createSpy('handler1');
    var handler2 = jasmine.createSpy('handler2');
    service.addPendingRequest();
    service.listen(handler1)
    $timeout.flush();
    service.removePendingRequest();

    expect(handler1).toHaveBeenCalled();
    expect(handler1.calls.count()).toBe(1);
  });


  describe('httpActivityInterceptor', function(){
    var interceptor;
    beforeEach(inject(function($injector) {
      interceptor = $injector.get('httpActivityInterceptor');
      spyOn(service, 'addPendingRequest');
      spyOn(service, 'removePendingRequest');
    }));

    it('should track get request', function(){
      interceptor.request({method: 'GET'});
      expect(service.addPendingRequest).toHaveBeenCalled();
    });

    it('should not track non GET request', function(){
      interceptor.request({method: 'PUT'});
      interceptor.request({method: 'POST'});
      interceptor.request({method: 'DELETE'});
      expect(service.addPendingRequest).not.toHaveBeenCalled();
    });

    it('should track response', function(){
      interceptor.request({method: 'GET'});
      interceptor.response({ config: {method: 'GET'}});
      expect(service.removePendingRequest).toHaveBeenCalled();
    });

    it('should not track non GET response', function(){
      interceptor.response({ config: {method: 'PUT'}});
      interceptor.response({ config: {method: 'POST'}});
      interceptor.response({ config: {method: 'DELETE'}});
      expect(service.removePendingRequest).not.toHaveBeenCalled();
    });

    it('should track response error', function(){
      interceptor.request({method: 'GET'});
      interceptor.responseError({ config: {method: 'GET'}});
      expect(service.removePendingRequest).toHaveBeenCalled();
    });

    it('should not track non GET response error', function(){
      interceptor.responseError({ config: {method: 'PUT'}});
      interceptor.responseError({ config: {method: 'POST'}});
      interceptor.responseError({ config: {method: 'DELETE'}});
      expect(service.removePendingRequest).not.toHaveBeenCalled();
    });
  })
})
