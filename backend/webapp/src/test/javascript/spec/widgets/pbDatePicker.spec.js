describe('pbDatePicker', function () {

  var $compile, scope;

  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('bonitasoft.ui.services'));

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
  }));


  describe('label', function() {

    it('should be displayed by default', function () {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      expect(element.find('label').length).toBe(1);
    });

    it('should has a default value equals to [Date]', function () {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      expect(element.find('label').text().trim()).toBe('Date');
    });

    it('should be on the top by default', function () {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      expect(element.find('.form-horizontal').length).toBe(0);
    });

    it('should be on the top of the input if labelPosition change', function () {
      scope.properties.label= 'Date';

      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      expect(element.find('.form-horizontal').length).toBe(0);
      var label = element.find('label');
      expect(label.text().trim()).toBe('Date');
    });

    it('should not be there when displayLabel is falsy', function () {
      scope.properties.labelHidden = true;

      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });
  });

  describe('input-group', function() {
    it('should adapt its width to label size when on the left', function () {
      scope.properties = angular.extend(scope.properties, {
        labelPosition: 'left',
        labelWidth: 4
      });

      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      var input = element.find('input');
      expect(input.parent().parent().hasClass('col-xs-8')).toBeTruthy();
    });

    it('should be wrapped in full width div when no label', function () {
      scope.properties.labelHidden = true;
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      var input = element.find('input').parent();
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should be wrapped in full width div when label is on the top', function () {

      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      var input = element.find('input').parent();
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });
  });

  describe('input used to display date', function() {

    it('should be readonly if datepicker is readonly', function () {
      scope.properties.readOnly = true;
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      var input = element.find('input').eq(0);

      expect(input.attr('readonly')).toBe('readonly');

    });

    it('should be updated when date change', function () {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      var inputs = element.find('input');

      //The date is updated
      var date = inputs.eq(0);
      date.val('barfoo');
      date.triggerHandler('input');

      expect(inputs.val()).toBe('barfoo');
    });

    it('should be required when requested', function () {
      scope.properties.required = true;
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();


      var input = element.find('input');
      expect(input.attr('required')).toBe('required');
    });
  });

  describe('button', function() {

    it('should be displayed by default', function () {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      expect(element.find('button').length).toBe(2);
    });

    it('should be disabled if date picker disabled', function () {
      scope.properties.readOnly = true;
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();


      var button = element.find('button');
      expect(button.attr('disabled')).toBe('disabled');
    });
  });
  describe('floorDate', function() {
    it('should set date as the one seen', function() {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      scope.properties.value = new Date(1438011476419);
      scope.ctrl.floorDate();
      expect(scope.properties.value.getTime()).toEqual(1437955200000);
    });
    it('should set date as the one seen even when UTC is one day behind', function() {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      scope.properties.value = new Date(2013,0,1,0,30); // Tue Jan 01 2013 00:30:00 GMT+0100 (CET)
      scope.ctrl.floorDate();
      expect(scope.properties.value.getTime()).toEqual(1356998400000); //Tue Jan 01 2013 00:00:00 GMT+0100 (CET)
    });
  });
});
