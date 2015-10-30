import CustomMatcher from '../../utils/matchers';
describe('splitter vertical directive', function() {
  var $compile, $rootScope, element, doc, scope, controller, $window;

  function triggerEvent(event, opt) {
    var e = angular.element.Event(event);
    angular.extend(e, opt);
    doc.triggerHandler(e);
  }

  var left = angular.element('<div id="left" style="position: absolute; left:0; right: 100px"></div>');
  left.appendTo(document.body);

  var right = angular.element('<div id="right" style="position: absolute; right: 0; left: 100px"></div>');
  right.appendTo(document.body);

  beforeEach(function() {
    jasmine.addMatchers(CustomMatcher.elementMatchers);
  });

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));
  beforeEach(inject(function(_$compile_, _$rootScope_, _$document_, _$window_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    doc = _$document_;
    $window = _$window_;

    scope = _$rootScope_.$new();
    scope.minWidth = 0;
    scope.maxWidth = $window.innerWidth;

    var template = '<div splitter-vertical pane-left="#left" pane-right="#right" pane-right-min="{{minWidth}}" pane-right-max="{{maxWidth}}"><div id="content">Content</div></div>';
    element = $compile(template)(scope);
    scope.$digest();

    controller = element.controller('splitterVertical');
  }));

  it('should add an extra splitter div before content', function() {
    var divs = element.find('div');

    expect(divs.length).toBe(2);
    expect(divs.get(0)).toHaveClass('splitter splitter-vertical');
    expect(divs.get(1).id).toBe('content');
  });

  it('should add class names to left and right elements', function() {
    expect(right).toHaveClass('splitter-pane splitter-pane-right');
    expect(left).toHaveClass('splitter-pane splitter-pane-left');
  });

  it('should close left element when it is opened', function() {
    spyOn(controller, 'closeLeft');
    controller.displayed = true;

    element.triggerHandler('splitter:toggle:left');

    expect(controller.displayed).toBeFalsy();
    expect(controller.closeLeft).toHaveBeenCalled();
  });

  it('should open left element when it is closed', function() {
    spyOn(controller, 'openLeft');
    controller.displayed = false;

    element.triggerHandler('splitter:toggle:left');

    expect(controller.displayed).toBeTruthy();
    expect(controller.openLeft).toHaveBeenCalled();
  });

  it('should close right element when it is opened', function() {
    spyOn(controller, 'closeRight');
    controller.displayed = true;

    element.triggerHandler('splitter:toggle:right');

    expect(controller.displayed).toBeFalsy();
    expect(controller.closeRight).toHaveBeenCalled();
  });

  it('should open right element when it is closed', function() {
    spyOn(controller, 'openRight');
    controller.displayed = false;

    element.triggerHandler('splitter:toggle:right');

    expect(controller.displayed).toBeTruthy();
    expect(controller.openRight).toHaveBeenCalled();
  });

  it('should resize left and right elements and add classes when splitter is clicked', function() {
    spyOn(controller, 'resize');
    var splitter = angular.element(element.find('.splitter').get(0));

    splitter.triggerHandler('mousedown');
    triggerEvent('mousemove', { target: document, pageX: 100 });

    expect(left).toHaveClass('splitter-onmove');
    expect(right).toHaveClass('splitter-onmove');

    triggerEvent('mouseup', { target: document });

    expect(controller.resize).toHaveBeenCalledWith(100);
    expect(left).not.toHaveClass('splitter-onmove');
    expect(right).not.toHaveClass('splitter-onmove');
  });

  describe('controller', function() {
    var scope;
    beforeEach(function() {
      scope = element.isolateScope();
    });

    it('should close right sidebar and save x position', function() {

      controller.closeRight();

      expect(controller.rightElem).toHaveClass('splitter-pane-closed');
      expect(controller.leftElem).toHaveClass('splitter-closed-right');
      expect(controller.leftElem.attr('style')).toContain('right: 0');
      expect(controller.xPosition).toBeDefined();
    });

    it('should open right sidebar with previous x position', function() {
      controller.xPosition = 100;

      controller.openRight();

      expect(controller.rightElem).not.toHaveClass('splitter-pane-closed');
      expect(controller.leftElem).not.toHaveClass('splitter-closed-right');
      expect(controller.leftElem.attr('style')).toContain('right: 100');
    });

    it('should close left sidebar and save x position', function() {

      controller.closeLeft();

      expect(controller.leftElem).toHaveClass('splitter-pane-closed');
      expect(controller.rightElem).toHaveClass('splitter-closed-left');
      expect(controller.rightElem.attr('style')).toContain('left: 0');
      expect(controller.xPosition).toBeDefined();
    });

    it('should open left sidebar with previous x position', function() {
      controller.xPosition = 100;

      controller.openLeft();

      expect(controller.leftElem).not.toHaveClass('splitter-pane-closed');
      expect(controller.rightElem).not.toHaveClass('splitter-closed-left');
      expect(controller.rightElem.attr('style')).toContain('left: 100');
    });

    // unstable test. Do not pass on phantomjs but pass anywhere else.
    xit('should resize panes', function() {
      $window.innerWidth = 1000;

      controller.resize(100);

      expect(controller.leftElem.attr('style')).toContain('right: 900px');
      expect(controller.rightElem.attr('style')).toContain('left: 100px');
    });

    it('should not resize right pane to a width less than min', function() {
      $window.innerWidth = 1000;
      scope.paneRightMin = 200;

      controller.resize(900); // > 1000-200

      expect(controller.leftElem.attr('style')).toContain('right: 200px');
      expect(controller.rightElem.attr('style')).toContain('left: 800px');
    });

    it('should not resize right pane to a width greater than max', function() {
      $window.innerWidth = 1000;
      scope.paneRightMax = 200;

      controller.resize(700); // < 1000-200

      expect(controller.leftElem.attr('style')).toContain('right: 200px');
      expect(controller.rightElem.attr('style')).toContain('left: 800px');
    });
  });

});

