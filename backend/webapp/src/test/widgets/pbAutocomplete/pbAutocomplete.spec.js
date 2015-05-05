describe('pbAutocomplete', function () {

  var $compile, scope, $rootScope, $document;

  beforeEach(module('org.bonitasoft.pagebuilder.generator.services'));
  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function (_$compile_, _$rootScope_, _$document_) {
    $compile = _$compile_;
    $document = _$document_;
    $rootScope = _$rootScope_;
    scope = $rootScope.$new();
    scope.properties = {labelHidden: true};
  }));

  describe('label', function () {

    it('should be on top by default if displayed', function () {
      scope.properties.labelHidden = false;
      scope.properties.label = 'foobar';

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(label.hasClass('widget-label-horizontal')).toBeFalsy();
      expect(label.text().trim()).toBe('foobar');
    });

    it('should be on the left of the input', function () {
      scope.properties.labelHidden = false;
      scope.properties.label = 'barbaz';
      scope.properties.labelPosition = 'left';

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(label.hasClass('widget-label-horizontal')).toBeTruthy();
      expect(label.text().trim()).toBe('barbaz');
    });

    it('should not be there when displayValue is falsy', function () {

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });
  });

  describe('input', function () {

    it('should adapt its width to label size when on the left', function () {
      scope.properties.labelHidden = false;
      scope.properties.labelPosition = 'left';
      scope.properties.labelWidth = 4;

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      var input = element.find('input');

      expect(input.parent().hasClass('col-xs-8')).toBeTruthy();
    });

    it('should be wrapped in full width div', function () {

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      var input = element.find('input');
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should update value property on change', function () {
      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      var input = element.find('input');
      input.val('barfoo');
      input.triggerHandler('input');

      expect(scope.properties.value).toBe('barfoo');
    });

    it('should be either read only nor required by default', function () {
      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      var input = element.find('input');
      expect(input.attr('readonly')).toBeUndefined();
      expect(input.attr('required')).toBeUndefined();
    });

    it('should be read-only when requested', function () {
      scope.properties.readOnly = true;

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      expect(element.find('input').attr('readonly')).toBe('readonly');
    });

    it('should use the typeahead directive if we have a availableValues for an autocomplete', function() {
      var scope = $rootScope.$new();
      scope.properties = {
        availableValues: [{name: 'jeanne'}, {name: 'paul'}],
        value:  'paul',
        displayedKey:  'name',
        labelHidden: false
      };

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();
      expect(element[0].querySelector('input').value).toBe('paul');
    });
  });
});
