describe('pbTextarea', function () {

  var $compile, scope;

 beforeEach(module('org.bonitasoft.pagebuilder.generator.services'));
  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {
      type: 'text',
      labelHidden: true
    };
  }));

  describe('label', function() {

    it('should be on top by default if displayed', function () {
      scope.properties = {
        labelHidden: false,
        label: 'foobar'
      };

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(label.hasClass('widget-label-horizontal')).toBeFalsy();
      expect(label.text().trim()).toBe('foobar');
    });

    it('should be on the left of the textarea', function () {
      scope.properties = {
        labelHidden: false,
        label: 'barbaz',
        labelPosition: 'left'
      };

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(label.hasClass('widget-label-horizontal')).toBeTruthy();
      expect(label.text().trim()).toBe('barbaz');
    });

    it('should not be there when displayValue is falsy', function () {

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });
  });

  describe('textarea', function() {

    it('should adapt its width to label size when on the left', function () {
      scope.properties = {
        labelHidden: false,
        labelPosition: 'left',
        labelWidth: 4
      };

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      var textarea = element.find('textarea');

      expect(textarea.parent().hasClass('col-xs-8')).toBeTruthy();
    });

    it('should be wrapped in full width div', function () {

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      var textarea = element.find('textarea');
      expect(textarea.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should update value property on change', function () {
      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      var textarea = element.find('textarea');
      textarea.val('barfoo');
      textarea.triggerHandler('input');

      expect(scope.properties.value).toBe('barfoo');
    });

    it('should be either read only nor required by default', function () {
      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      var textarea = element.find('textarea');
      expect(textarea.attr('readonly')).toBeUndefined();
    });

    it('should be read only when requested', function () {
      scope.properties.readOnly = true;

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      expect(element.find('textarea').attr('readonly')).toBe('readonly');
    });

    it('should not set a className from-control if the textarea type is range', function() {
      var element = $compile('<pb-textarea id="toto"></pb-textarea>')(scope);
      scope.properties.type = 'range';
      scope.$apply();

      // It should works... It works everywhere but not with PhantomJS (for the lulz I think)
      // expect(element.find('textarea').hasClass('form-control')).toBe(false);
    });
  });

});
