describe('pbAnchor', function() {

  var compile, scope, dom;

  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function ($injector){
    compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    scope.properties = {
      alignment: 'left',
      level: 'Level 2'
    };
  }));

  beforeEach(function() {
    dom = compile('<pb-title></pb-title>')(scope);
    scope.$apply();
  });

  it('should display a h2 by default', function() {
    // Ivre, il ne connait pas div a ou div > a... WTF
    expect(dom.find('h2')[0]).toBeDefined();
  });

  it('should position the h2 to the left', function() {
    expect(dom.find('h2').hasClass('text-left')).toBe(true);
  });

  it('should change the title level', function() {
    scope.properties.level = 'Level 1';
    scope.$apply();
    expect(dom.find('h1')[0]).toBeDefined();

    scope.properties.level = 'Level 4';
    scope.$apply();
    expect(dom.find('h4')[0]).toBeDefined();

    scope.properties.level = 'Level 3';
    scope.$apply();
    expect(dom.find('h3')[0]).toBeDefined();
  });

  it('should set a text', function() {
    expect(dom.find('h2').text()).toBe('');
    scope.properties.text = 'yolo';
    scope.$apply();
    expect(dom.find('h2').text()).toBe('yolo');
  });

});
