describe('pbLink', function() {

  var compile, scope, dom;

  beforeEach(module('bonitasoft.ui.widgets'));

  beforeEach(inject(function ($injector){
    compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    scope.properties = {
      alignment: 'left',
      buttonStyle: 'none'
    };
  }));

  beforeEach(function() {
    dom = compile('<pb-link></pb-link>')(scope);
    scope.$apply();
  });

  it('should contain a a inside a div', function() {
    // Ivre, il ne connait pas div a ou div > a... WTF
    expect(dom.find('div').find('a')[0]).toBeDefined();
  });

  it('should position the div to the left', function() {
    expect(dom.find('div').hasClass('text-left')).toBe(true);
  });

  it('should not have a btn class if no buttonStyle is defined', function() {
    expect(dom.find('div').find('a').hasClass('btn')).toBe(false);
  });

  it('should set a btn class for the button style chosen', function() {
    scope.properties.buttonStyle = 'danger';
    scope.$apply();
    expect(dom.find('div').find('a').hasClass('btn')).toBe(true);
    expect(dom.find('div').find('a').hasClass('btn-danger')).toBe(true);

    scope.properties.buttonStyle = 'info';
    scope.$apply();
    expect(dom.find('div').find('a').hasClass('btn-info')).toBe(true);
  });

  it('should set an url', function() {
    expect(dom.find('div').find('a').attr('href')).toBe(void 0);
    scope.properties.targetUrl = 'http://google.fr';
    scope.$apply();
    expect(dom.find('div').find('a').attr('href')).toBe('http://google.fr');
  });

  it('should set a target', function() {
    expect(dom.find('div').find('a').attr('target')).toBe('');
    scope.properties.target = '_blank';
    scope.$apply();
    expect(dom.find('div').find('a').attr('target')).toBe('_blank');
  });

  it('should set a text', function() {
    expect(dom.find('div').find('a').text()).toBe('');
    scope.properties.text = 'yolo';
    scope.$apply();
    expect(dom.find('div').find('a').text()).toBe('yolo');
  });

});
