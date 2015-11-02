describe('pbDatePicker', function () {

  var $compile, scope, element;

  beforeEach(module('bonitasoft.ui.widgets', 'mgcrea.ngStrap.datepicker'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    //We specified the default values
    scope.properties = {
      isBound: function() {
        return false;
      },
      placeholder : 'dd/MM/yyyy',
      dateFormat : 'dd/MM/yyyy',
      label : 'Date'
    };
    element = $compile('<pb-date-picker></pb-date-picker>')(scope);
    scope.$apply();
  }));


   describe('calendar', function() {

    it('should not be displayed by default', function() {
      expect(element.find('div.dropdown-menu.datepicker').length).toBe(0);
    });

    it('should be displayed while clicking on button', function() {
      var button = element.find('.input-group .input-group-btn button');

      button.triggerHandler('click');

      expect(element.find('div.dropdown-menu.datepicker').length).toBe(1);
    });

    it('should be displayed while clicking on input', function() {
      var input = element.find('.input-group input');

      input.triggerHandler('click');

      expect(element.find('div.dropdown-menu.datepicker').length).toBe(1);
    });
  });

  describe('label', function() {

    it('should be displayed by default', function () {
      expect(element.find('label').length).toBe(1);
    });

    it('should has a default value equals to [Date]', function () {
      expect(element.find('label').text().trim()).toBe('Date');
    });

    it('should be on the top by default', function () {
      expect(element.find('.form-horizontal').length).toBe(0);
    });

    it('should be on the left of the input if labelPosition change', function () {
      scope.properties.labelPosition= 'left';
      scope.$apply();

      expect(element.find('.form-horizontal').length).toBe(1);
    });

    it('should not be there when displayLabel is falsy', function () {
      scope.properties.labelHidden = true;
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });

    it('should be configurable with label property', function() {
      scope.properties.label = 'New label';
      scope.$apply();

      expect(element.find('label').text().trim()).toBe('New label');
    });
  });

  describe('input-group', function() {

    it('should adapt its width to label size when on the left', function () {
      scope.properties = angular.extend(scope.properties, {
        labelPosition: 'left',
        labelWidth: 4
      });
      scope.$apply();

      var input = element.find('input');
      expect(input.parent().parent().hasClass('col-xs-8')).toBeTruthy();
    });

    it('should be wrapped in full width div when no label', function () {
      scope.properties.labelHidden = true;
      scope.$apply();

      var input = element.find('input').parent();
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should be wrapped in full width div when label is on the top', function () {
      var input = element.find('input').parent();
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });
  });

  describe('input used to display date', function() {

    it('should be readonly if datepicker is readonly', function () {
      scope.properties.readOnly = true;
      scope.$apply();

      var input = element.find('input').eq(0);

      expect(input.attr('readonly')).toBe('readonly');

    });

    it('should be required when requested', function () {
      scope.properties.required = true;
      scope.$apply();

      var input = element.find('input');
      expect(input.attr('required')).toBe('required');
    });

    it('should display date passed as time millis', function() {
      scope.properties.value = 1438011476419;
      scope.$apply();

      expect(element.find('input').val()).toBe('27/07/2015');
    });

    // Not supported anymore with new date picker
    xit('should display date passed as string time millis', function() {
      scope.properties.value = '1356998400000';
      scope.$apply();

      expect(element.find('input').val()).toBe('01/01/2013');
    });

    it('should display date passed with iso format', function() {
      scope.properties.value = '2015-09-30T00:00:00.000Z';
      scope.$apply();

      expect(element.find('input').val()).toBe('30/09/2015');
    });

    it('should display date using expected date format', function() {
      scope.properties.value = '2015-09-30T00:00:00.000Z';
      scope.properties.dateFormat = 'yyyy/MM/dd';
      scope.$apply();

      expect(element.find('input').val()).toBe('2015/09/30');
    });

    it('should parse correctly date', function() {

      element.find('input').val('01/09/2015').triggerHandler('input');

      expect(scope.properties.value).toEqual(new Date('2015-09-01T00:00:00.000Z'));
    });

    it('should parse correctly date according to date format', function() {
      scope.properties.dateFormat = 'yyyy/MM/dd';
      scope.$apply();

      element.find('input').val('2015/09/01').triggerHandler('input');

      expect(scope.properties.value).toEqual(new Date('2015-09-01T00:00:00.000Z'));
    });

  });

  describe('button', function() {

    it('should be displayed by default', function () {
      expect(element.find('.input-group .input-group-btn button').length).toBe(1);
    });

    it('should be disabled if date picker disabled', function () {
      scope.properties.readOnly = true;
      scope.$apply();

      var button = element.find('.input-group .input-group-btn button');
      expect(button.attr('disabled')).toBe('disabled');
    });
  });
});
