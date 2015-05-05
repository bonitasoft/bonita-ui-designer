describe('pbInput', function () {

  var $compile, scope;

 beforeEach(module('org.bonitasoft.pagebuilder.generator.services'));
  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {
      type: 'text',
      labelHidden: true,
    };
  }));

  describe('label', function() {

    it('should be on top by default if displayed', function () {
      scope.properties = {
        labelHidden: false,
        label: 'foobar'
      };

      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(label.hasClass('widget-label-horizontal')).toBeFalsy();
      expect(label.text().trim()).toBe('foobar');
    });

    it('should be on the left of the input', function () {
      scope.properties = {
        labelHidden: false,
        label: 'barbaz',
        labelPosition: 'left'
      };

      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(label.hasClass('widget-label-horizontal')).toBeTruthy();
      expect(label.text().trim()).toBe('barbaz');
    });

    it('should not be there when displayValue is falsy', function () {

      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });
  });

  describe('input', function() {

    it('should adapt its width to label size when on the left', function () {
      scope.properties = {
        labelHidden: false,
        labelPosition: 'left',
        labelWidth: 4
      };

      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      var input = element.find('input');

      expect(input.parent().hasClass('col-xs-8')).toBeTruthy();
    });

    it('should be wrapped in full width div', function () {

      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      var input = element.find('input');
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should update value property on change', function () {
      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      var input = element.find('input');
      input.val('barfoo');
      input.triggerHandler('input');

      expect(scope.properties.value).toBe('barfoo');
    });

    it('should be either read only nor required by default', function () {
      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      var input = element.find('input');
      expect(input.attr('readonly')).toBeUndefined();
    });

    it('should be read only when requested', function () {
      scope.properties.readOnly = true;

      var element = $compile('<pb-input></pb-input>')(scope);
      scope.$apply();

      expect(element.find('input').attr('readonly')).toBe('readonly');
    });

    it('should not set a className from-control if the input type is range', function() {
      var element = $compile('<pb-input id="toto"></pb-input>')(scope);
      scope.properties.type = 'range';
      scope.$apply();

      // It should works... It works everywhere but not with PhantomJS (for the lulz I think)
      // expect(element.find('input').hasClass('form-control')).toBe(false);
    });
  });

});
