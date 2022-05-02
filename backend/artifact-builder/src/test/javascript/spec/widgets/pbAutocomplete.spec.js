describe('pbAutocomplete', function () {

  var $compile, scope, $rootScope, $document;

  beforeEach(module('bonitasoft.ui.services'));
  beforeEach(module('bonitasoft.ui.widgets'));

  beforeEach(inject(function (_$compile_, _$rootScope_, _$document_) {
    $compile = _$compile_;
    $document = _$document_;
    $rootScope = _$rootScope_;
    scope = $rootScope.$new();
    scope.properties = {
      isBound: function() {
        return false;
      },
      label: 'foobar',
      labelHidden: false
    };
  }));

  describe('label', function () {

    it('should be on top by default if displayed', function () {
      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(element.find('.form-horizontal').length).toBe(0);
      expect(label.text().trim()).toBe('foobar');
    });

    it('should be on the left of the input', function () {

      scope.properties.label = 'barbaz';
      scope.properties.labelPosition = 'left';

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      expect(element.find('.form-horizontal').length).toBe(1);

      var label = element.find('label');
      expect(label.text().trim()).toBe('barbaz');
    });

    it('should not be there when displayValue is falsy', function () {
      scope.properties.labelHidden = true;
      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });

    it('should allows html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: true
      });

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();
      var label = element.find('label');
      expect(label.text().trim()).toBe('allow html!');
    });

    it('should prevent html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: false
      });

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();
      var label = element.find('label');
      expect(label.text().trim()).toBe('<span>allow html!</span>');
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

    it('should be required when requested', function () {
      scope.properties.required = true;

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();

      expect(element.find('input').attr('required')).toBe('required');
    });

    it('should initialize the input if we have a availableValues and value for an autocomplete', function() {

      scope.properties = angular.extend(scope.properties, {
        availableValues: [{name: 'jeanne'}, {name: 'paul'}],
        value:  'paul',
        displayedKey: 'name',
        returnedKey: 'name'
      });

      var element = $compile('<pb-autocomplete></pb-autocomplete>')(scope);
      scope.$apply();
      expect(element[0].querySelector('input').value).toBe('paul');
    });
  });
});
