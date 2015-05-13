describe('splitter horizontal directive', function() {
  var $compile, $rootScope, element, doc, scope, controller, $window;

  function triggerEvent (event, opt) {
    var e = angular.element.Event(event);
    angular.extend(e, opt);
    doc.triggerHandler(e);
  }

  var top = angular.element('<div id="top" style="position: absolute; bottom:200px; top: 0;"></div>');
  top.appendTo(document.body);

  var bottom = angular.element('<div id="bottom" style="position: absolute; bottom: 0; height: 200px;"></div>');
  bottom.appendTo(document.body);

  beforeEach(function() {
    jasmine.addMatchers(CustomMatcher.elementMatchers);
  });

  beforeEach(module('pb.directives'));
  beforeEach(inject(function(_$compile_, _$rootScope_, _$document_, _$window_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    doc = _$document_;
    $window = _$window_;

    scope = _$rootScope_.$new();
    var template = '<div splitter-horizontal pane-top="#top" pane-bottom="#bottom" ><div id="content">Content</div></div>';
    element = $compile(template)(scope);
    scope.$digest();

    controller = element.controller('splitterHorizontal');
  }));

  it('should add an extra splitter div before content', function() {
    var divs = element.find('div');

    expect(divs.length).toBe(2);
    expect(divs.get(0)).toHaveClass('splitter splitter-horizontal');
    expect(divs.get(1).id).toBe('content');
  });

  it('should add class names to top and bottom elements', function() {
    expect(top).toHaveClass('splitter-pane splitter-pane-top');
    expect(bottom).toHaveClass('splitter-pane splitter-pane-bottom');
  });

  it('should close bottom element when it is opened', function() {
    spyOn(controller, 'closeBottom');
    controller.displayed = true;

    element.triggerHandler('splitter:toggle:bottom');

    expect(controller.closeBottom).toHaveBeenCalled();
  });

  it('should open bottom element when it is closed', function() {
    spyOn(controller, 'openBottom');
    controller.displayed = false;

    element.triggerHandler('splitter:toggle:bottom');

    expect(controller.openBottom).toHaveBeenCalled();
  });

  it('should resize left and right elements and add classes when splitter is clicked', function() {
    spyOn(controller, 'resize');
    var splitter = angular.element(element.find('.splitter').get(0));

    splitter.triggerHandler('mousedown');
    triggerEvent('mousemove', {target: document, pageY: 100});

    expect(top).toHaveClass('splitter-onmove');
    expect(bottom).toHaveClass('splitter-onmove');

    triggerEvent('mouseup', {target: document});

    expect(controller.resize).toHaveBeenCalledWith(100);
    expect(top).not.toHaveClass('splitter-onmove');
    expect(bottom).not.toHaveClass('splitter-onmove');
  });

  describe('controller', function() {
    var scope;
    beforeEach(function() {
      scope = element.isolateScope();
    });

    it('should close bottom sidebar', function() {

      controller.closeBottom();

      expect(controller.bottomElem).toHaveClass('splitter-pane-closed');
      expect(controller.topElem.attr('style')).toContain('bottom: 0');
    });

    it('should open bottom sidebar', function() {

      controller.openBottom();

      expect(controller.bottomElem).not.toHaveClass('splitter-pane-closed');
      expect(controller.topElem.attr('style')).toContain('bottom: 200'); // bottom height
    });

    it('should not resize bottom pane to a height less than min', function() {
      $window.innerHeight = 1000;
      scope.paneBottomMin = 200;

      controller.resize(900); // > 1000-200

      expect(controller.bottomElem[0].style.height).toBe('200px');
      expect(controller.topElem[0].style.bottom).toBe('200px');
    });

    // should work but don't
    xit('should not resize right pane to a width greater than max', function() {
     $window.innerHeight = 1000;
      scope.paneBottomMax = 200;

      controller.resize(700); // < 1000-200

      expect(controller.bottomElem[0].style.height).toBe('200px');
      expect(controller.topElem[0].style.bottom).toBe('200px');
    });
  });

});

