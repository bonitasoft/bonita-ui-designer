describe('pbLink', function() {

  var compile, scope, dom, location;

  beforeEach(module('bonitasoft.ui.services','bonitasoft.ui.widgets'));

  beforeEach(inject(function ($injector){
    compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    location = $injector.get('$location');
    spyOn(location, 'absUrl').and.callFake(function() {
      return 'http://localhost:8080/bonita/portal/resource/app/myApp/tasks/content/?app=myApp';
    });
    scope.properties = {
      type: 'URL',
      alignment: 'left',
      buttonStyle: 'none',
      allowHTML: true
    };
  }));

  beforeEach(function() {
    dom = compile('<pb-link></pb-link>')(scope);
    scope.$apply();
  });

  it('should contain a a inside a div', function() {
    // Ivre, il ne connait pas div a ou div > a... WTF
    expect(dom.find('a')[0]).toBeDefined();
  });

  it('should position the div to the left', function() {
    expect(dom.find('div').hasClass('text-left')).toBe(true);
  });

  it('should not have a btn class if no buttonStyle is defined', function() {
    expect(dom.find('a').hasClass('btn')).toBe(false);
  });

  it('should set a btn class for the button style chosen', function() {
    scope.properties.buttonStyle = 'danger';
    scope.$apply();
    expect(dom.find('a').hasClass('btn')).toBe(true);
    expect(dom.find('a').hasClass('btn-danger')).toBe(true);

    scope.properties.buttonStyle = 'info';
    scope.$apply();
    expect(dom.find('a').hasClass('btn-info')).toBe(true);
  });

  it('should set an url when type is custom URL', function() {
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.targetUrl = 'http://google.fr';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('http://google.fr');
  });

  it('should set a target', function() {
    expect(dom.find('a').attr('target')).toBe('');
    scope.properties.target = '_blank';
    scope.$apply();
    expect(dom.find('a').attr('target')).toBe('_blank');
  });

  it('should set a text and allows html markup to be interpreted', function() {
    expect(dom.find('a').text()).toBe('');
    scope.properties.text = '<i class="fa fa-bonita">yolo</i>';
    scope.$apply();
    expect(dom.find('a').text()).toBe('yolo');
    expect(dom.find('a').html()).toBe('<i class="fa fa-bonita">yolo</i>');
  });

  it('should set a text and prevent html markup to be interpreted', function() {
    scope.properties.allowHTML= false;
    expect(dom.find('a').text()).toBe('');
    scope.properties.text = '<i class="fa fa-bonita">yolo</i>';
    scope.$apply();
    expect(dom.find('a').text()).toBe('<i class="fa fa-bonita">yolo</i>');
    expect(dom.find('a').html()).toBe('&lt;i class="fa fa-bonita"&gt;yolo&lt;/i&gt;');
  });

  it('should set target to top when type is page', function() {
    expect(dom.find('a').attr('target')).toBe('');
    scope.properties.type = 'page';
    scope.$apply();
    expect(dom.find('a').attr('target')).toBe('_top');
  });

  it('should set URL when type is page', function() {
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'page';
    scope.properties.pageToken = 'mySecondPage';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('http://localhost:8080/bonita/apps/myApp/mySecondPage');
  });

  it('should set URL in layout when webapp name is custom and type is page', function() {
    location.absUrl = function() {
      return 'http://localhost:8080/my-webapp/apps/myApp/home';
    };
    scope.$apply();
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'page';
    scope.properties.pageToken = 'mySecondPage';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('http://localhost:8080/my-webapp/apps/myApp/mySecondPage');
  });

  it('should set URL when type is start process', function() {
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'process';
    scope.properties.processName = 'Pool';
    scope.properties.processVersion = '1.0';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('http://localhost:8080/bonita/portal/form/process/Pool/1.0?app=myApp');
  });

  it('should set URL when type is perform task', function() {
    expect(dom.find('div').find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'task';
    scope.properties.taskId = '42';
    scope.$apply();
    expect(dom.find('div').find('a').attr('href')).toBe('http://localhost:8080/bonita/portal/form/taskInstance/42?app=myApp');
  });

  it('should set URL when type is case overview', function() {
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'overview';
    scope.properties.caseId = '12';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('http://localhost:8080/bonita/portal/form/processInstance/12?app=myApp');
  });

  it('should set URL when we are in preview and type is start process', function() {
    location.absUrl = function() {
      return 'http://localhost:8080/designer/preview/page/41f71aae-9980-45cb-84ff-cd9d9fddf0da/?time=1488459114511&app=myApp';
    };
    scope.$apply();
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'process';
    scope.properties.processName = 'Pool';
    scope.properties.processVersion = '1.0';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('/bonita/portal/form/process/Pool/1.0?app=myApp');
  });

  it('should set URL when we are in preview and type is page', function() {
    location.absUrl = function() {
      return 'http://localhost:8080/designer/preview/page/41f71aae-9980-45cb-84ff-cd9d9fddf0da/?time=1488459114511&app=myApp';
    };
    scope.$apply();
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'page';
    scope.properties.pageToken = 'mySecondPage';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('/bonita/apps/myApp/mySecondPage');
  });

  it('should set URL when we are in preview and type is page and app token is not passed', function() {
    location.absUrl = function() {
      return 'http://localhost:8080/designer/preview/page/41f71aae-9980-45cb-84ff-cd9d9fddf0da/?time=1488459114511';
    };
    scope.$apply();
    expect(dom.find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'page';
    scope.properties.pageToken = 'mySecondPage';
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('/bonita/apps/APP_TOKEN_PLACEHOLDER/mySecondPage');
  });

  it('should set App token in URL when the property is set', function() {
    expect(dom.find('div').find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'task';
    scope.properties.taskId = '42';
    scope.properties.appToken = 'livingApp';
    scope.$apply();
    expect(dom.find('div').find('a').attr('href')).toBe('http://localhost:8080/bonita/portal/form/taskInstance/42?app=livingApp');
  });

  it('should set urlParams in URL when the property is set and type is perform task', function() {
    expect(dom.find('div').find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'task';
    scope.properties.taskId = '42';
    scope.properties.appToken = 'livingApp';
    scope.properties.urlParams = {
      locale: 'fr',
      tenant: 2
    };
    scope.$apply();
    expect(dom.find('div').find('a').attr('href')).toBe('http://localhost:8080/bonita/portal/form/taskInstance/42?app=livingApp&locale=fr&tenant=2');
  });

  it('should set urlParams in URL when the property is set and type is page', function() {
    expect(dom.find('div').find('a').attr('href')).toBeUndefined();
    scope.properties.type = 'page';
    scope.properties.pageToken = 'mySecondPage';
    scope.properties.urlParams = {
      locale: 'fr',
      tenant: 2
    };
    scope.$apply();
    expect(dom.find('a').attr('href')).toBe('http://localhost:8080/bonita/apps/myApp/mySecondPage?locale=fr&tenant=2');
  });
});
