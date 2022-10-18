describe('pbCurrencyInput', function () {

  let $compile, scope;

  beforeEach(module('bonitasoft.ui.services'));
  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('cur.$mask'));


  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {
      isBound: function() {
        return false;
      },
      labelWidth: 4,
      label: '<i class="fa fa-icon">foobar</i>',
      allowHTML: true,
      min:2,
      max:6,
      maskCurrency:"$",
      orientation:"left",
      indentation:"none",
      decimal: ",",
      decimalSize: 2,
      group:"whitespace"
    };
  }));

  describe('label', function() {

    it('should be on top by default if displayed', function () {
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      let label = element.find('label');
      expect(element.find('.form-horizontal').length).toBe(0);
      expect(label.text().trim()).toBe('foobar');
      expect(label.html()).toBe('<i class="fa fa-icon">foobar</i>');
    });

    it('should be on the left of the input', function () {
      scope.properties = angular.extend( scope.properties, {
        label: 'barbaz',
        labelPosition: 'left'
      });

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      expect(element.find('.form-horizontal').length).toBe(1);
      let label = element.find('label');
      expect(label.text().trim()).toBe('barbaz');
    });

    it('should not be there when label is hidden', function () {
      scope.properties.labelHidden = true;
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });

    it('should allows html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: true
      });

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();
      let label = element.find('label');
      expect(label.text().trim()).toBe('allow html!');
    });

    it('should prevent html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: false
      });

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();
      let label = element.find('label');
      expect(label.text().trim()).toBe('<span>allow html!</span>');
    });

  });

  describe('input', function() {

    it('should adapt its width to label size when on the left', function () {
      scope.properties = angular.extend( scope.properties, {
        labelHidden: false,
        labelPosition: 'left',
        labelWidth: 4
      });

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      let input = element.find('input');

      expect(input.parent().hasClass('col-xs-8')).toBeTruthy();
    });

    it('should be wrapped in full width div', function () {

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      let input = element.find('input');
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should update value property on change', function () {
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      let input = element.find('input');
      input.val('12');
      input.triggerHandler('input');

      expect(scope.properties.value).toBe(0.12);
    });

    it('should be either read only nor required by default', function () {
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      let input = element.find('input');
      expect(input.attr('readonly')).toBeUndefined();
    });

    it('should be read only when requested', function () {
      scope.properties.readOnly = true;

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      expect(element.find('input').attr('readonly')).toBe('readonly');
    });

    it('should be required when requested', function () {
      scope.properties.required = true;

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      expect(element.find('input').attr('required')).toBe('required');
    });

    it('should be not required when requested', function () {
      scope.properties.required = false;

      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();

      expect(element.find('input').attr('required')).toBeUndefined();
    });


    it('should validate min value on min change', function () {
      scope.properties.min = 2;
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      let input = element.find('input');

      input.val('2.00');
      input.triggerHandler('input');
      scope.$apply();
      expect(input.attr('class')).not.toMatch('ng-invalid-min');

      scope.properties.min = 3;
      scope.$apply();
      expect(input.attr('class')).toMatch('ng-invalid-min');
    });

    it('should validate min value on value change', function () {
      scope.properties.min = 1;
      scope.properties.value = 0;
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$digest();

      expect(element.find('input').attr('class')).toMatch('ng-invalid-min');

      scope.properties.value = 3;
      scope.$apply();
      expect(element.find('input').attr('class')).not.toMatch('ng-invalid-min');
    });

    it('should validate max value on min change', function () {
      scope.properties.max = 2;
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      let input = element.find('input');

      input.val('2.00');
      input.triggerHandler('input');
      scope.$apply();
      expect(input.attr('class')).not.toMatch('ng-invalid-max');

      scope.properties.max = 1;
      scope.$apply();
      expect(input.attr('class')).toMatch('ng-invalid-max');
    });

    it('should validate max value on value change', function () {
      scope.properties.max = 1;
      scope.properties.value = 0;
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$digest();

      expect(element.find('input').attr('class')).not.toMatch('ng-invalid-max');

      scope.properties.value = 2;
      scope.$apply();
      expect(element.find('input').attr('class')).toMatch('ng-invalid-max');
    });

    it('should watch properties orientation, indentation, decimal, decimalSize and group', function () {
      scope.properties.value = 1200.00;
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$digest();

      scope.properties.orientation = 'right';
      scope.$apply();
      expect(element.find('input')[0].value).toBe('1 200,00$');

      scope.properties.indentation = 'whitespace';
      scope.$apply();
      expect(element.find('input')[0].value).toBe('1 200,00 $');

      scope.properties.decimal = '.';
      scope.$apply();
      expect(element.find('input')[0].value).toBe('1 200.00 $');

      scope.properties.decimalSize = 1;
      scope.$apply();
      expect(element.find('input')[0].value).toBe('12 000.0 $');

      scope.properties.group = ',';
      scope.$apply();
      expect(element.find('input')[0].value).toBe('12,000.0 $');

    });

    it('should apply default value on currency config', function () {
      scope.properties.value = 1200000.00;
      scope.properties.orientation = '';
      scope.properties.indentation = '';
      scope.properties.decimal = '';
      scope.properties.decimalSize = '';
      scope.properties.group = '';
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();
      expect(element.find('input')[0].value).toBe('1200000$');
    });

    it('should accept custom value for currency config', function () {
      scope.properties.value = 1200000.00;
      scope.properties.orientation = 'Invalid value';
      scope.properties.indentation = '[custom-indentation]';
      scope.properties.decimal = '[custom-decimal]';
      scope.properties.decimalSize = '2';
      scope.properties.group = '[custom-group]';
      let element = $compile('<pb-currency-input></pb-currency-input>')(scope);
      scope.$apply();
      expect(element.find('input')[0].value).toBe('1[custom-group]200[custom-group]000[custom-decimal]00[custom-indentation]$');
    });

  });

});
