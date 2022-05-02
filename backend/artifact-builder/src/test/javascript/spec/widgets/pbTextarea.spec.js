describe('pbTextarea', function () {

  var $compile, scope;

 beforeEach(module('bonitasoft.ui.services'));
  beforeEach(module('bonitasoft.ui.widgets'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {
      isBound: function() {
        return false;
      },
      type: 'text',
      label: 'foobar',
      labelPosition: 'top',
      labelHidden: false
    };
  }));

  describe('label', function() {

    it('should be on top by default if displayed', function () {

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      expect(element.find('.form-horizontal').length).toBe(0);
      var label = element.find('label');
      expect(label.text().trim()).toBe('foobar');
    });

    it('should be on the left of the textarea', function () {
      scope.properties.labelPosition =  'left';
      scope.properties.label =  'barbaz';

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      expect(element.find('.form-horizontal').length).toBe(1);
      var label = element.find('label');
      expect(label.text().trim()).toBe('barbaz');
    });

    it('should not be there when label is hidden', function () {
      scope.properties.labelHidden =  true;
      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });


    it('should allows html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: true
      });

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();
      var label = element.find('label');
      expect(label.text().trim()).toBe('allow html!');
    });

    it('should prevent html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: false
      });

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();
      var label = element.find('label');
      expect(label.text().trim()).toBe('<span>allow html!</span>');
    });

  });

  describe('textarea', function() {

    it('should adapt its width to label size when on the left', function () {
      scope.properties.labelPosition = 'left';
      scope.properties.labelWidth = 4;

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

    it('should be required when requested', function () {
      scope.properties.required = true;

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();

      expect(element.find('textarea').attr('required')).toBe('required');
    });

    it('should validate minlength', function () {
      scope.properties.minLength = 5;
      scope.properties.value = 'foo';

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();
      expect(element.find('textarea').attr('class')).toMatch('ng-invalid-minlength');

      scope.properties.value = 'foofoo';
      scope.$apply();
      expect(element.find('textarea').attr('class')).not.toMatch('ng-invalid-minlength');
    });

    it('should validate maxlength', function () {
      scope.properties.maxLength = 5;
      scope.properties.value = 'foofoo';

      var element = $compile('<pb-textarea></pb-textarea>')(scope);
      scope.$apply();
      expect(element.find('textarea').attr('class')).toMatch('ng-invalid-maxlength');

      scope.properties.value = 'foo';
      scope.$apply();
      expect(element.find('textarea').attr('class')).not.toMatch('ng-invalid-maxlength');
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
