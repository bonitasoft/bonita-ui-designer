describe('pbDateTimePicker', function () {


  var $compile, scope, element, body, filter, $timeout;

  beforeEach(module('bonitasoft.ui.widgets', 'mgcrea.ngStrap.datepicker', 'mgcrea.ngStrap.timepicker'));

  beforeEach(inject(function (_$compile_, $rootScope, $filter, _$timeout_) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    $timeout = _$timeout_;
    filter = $filter;
    //We specified the default values
    scope.properties = {
      isBound: function() {
        return false;
      },
      placeholder : 'dd/MM/yyyy',
      dateFormat : 'dd/MM/yyyy',
      timeFormat : 'h:mm:ss a',
      label : 'Date'
    };

    body = $('body');
    body.html('');
  }));


   describe('calendar', function() {
    beforeEach(function () {
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
    });

    it('should not be displayed by default', function() {
      expect(body.find('div.dropdown-menu.dateTimepicker').length).toBe(0);
    });

    it('should be displayed and attached to body while clicking on button', function() {
      var button = element.find('.input-group .input-group-btn button.calendar');

      button.triggerHandler('click');

      expect(body.find('div.dropdown-menu.datepicker').length).toBe(1);
    });

    it('should be displayed and attached to body while clicking on input', function() {
      var input = element.find('.input-group input');

      input[0].focus();

      expect(body.find('div.dropdown-menu.datepicker').length).toBe(1);
    });
  });

  describe('time picker', function() {
    it('should be time picker with timeZone when Handle time zone is true', function() {
      scope.properties.withTimeZone = true;
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
      var input = element.find('input');

      expect(input.attr('data-timezone')).toBe('');
    });

    it('should be time picker UTC when Handle time zone is false', function() {
      scope.properties.withTimeZone = false;
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
      var input = element.find('input');
      expect(input.attr('data-timezone')).toBe('UTC');
    });

    it('should be displayed by default', function() {
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
      expect(body.find('div.dropdown-menu.timepicker').length).toBe(0);
    });

    it('should be displayed and attached to body while clicking on button', function() {
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
      var button = element.find('.input-group .input-group-btn button.timepicker');

      button.triggerHandler('click');

      expect(body.find('div.dropdown-menu.timepicker').length).toBe(1);
    });

    it('should be displayed and attached to body while clicking on input', function() {
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
      var input = element.find('.input-group input');

      input[1].focus();

      expect(body.find('div.dropdown-menu.timepicker').length).toBe(1);
    });
  });

  describe('label', function() {
    beforeEach(function(){
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
    });
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
      scope.properties.labelPosition = 'left';
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
    beforeEach(function(){
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
    });
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
    beforeEach(function(){
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
    });
    it('should be readonly if dateTimepicker is readonly', function () {
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
      $timeout.flush();

      expect(scope.properties.value).toEqual(new Date('2015-09-01T00:00:00.000Z'));
    });

    it('should parse correctly date according to date format', function() {
      scope.properties.dateFormat = 'yyyy/MM/dd';
      scope.$apply();

      element.find('input').val('2015/09/01').triggerHandler('input');
      $timeout.flush();

      expect(scope.properties.value).toEqual(new Date('2015-09-01T00:00:00.000Z'));
    });
    it('should don\'t be change time when date input changed', function() {
      var input = element.find('input');
      input.val('01/09/2015').triggerHandler('input');
      input.eq(1).val('2:02:55 PM').triggerHandler('input');
      input.val('05/09/2015').triggerHandler('input');
      input.val('06/09/2015').triggerHandler('input');

      expect(scope.properties.value).toEqual(new Date('2015-09-06T14:02:55.000Z'));

    });
  });

  describe('input used to display time', function() {

    beforeEach(function () {
      scope.properties.withTime = true;
      scope.$apply();
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
    });

    it('should be readonly if dateTimePicker is readonly', function () {
      scope.properties.readOnly = true;
      scope.$apply();

      var input = element.find('input').eq(1);

      expect(input.attr('readonly')).toBe('readonly');
    });

    it('should be required when requested', function () {
      scope.properties.required = true;
      scope.$apply();

      var input = element.find('input').eq(1);
      expect(input.attr('required')).toBe('required');
    });

    it('should display date passed as time millis', function() {
      scope.properties.value = 1438011476419;
      scope.$apply();

      expect(element.find('input').eq(1).val()).toBe('3:37:56 PM');
    });

    it('should display date and time passed with iso format', function() {
      scope.properties.value = '2015-09-30T14:02:55.000Z';
      scope.$apply();

      expect(element.find('input').val()).toBe('30/09/2015');
      expect(element.find('input').eq(1).val()).toBe('2:02:55 PM');
    });

    it('should display time using expected time format', function() {
      scope.properties.value = '2015-09-30T14:02:55.000Z';
      scope.properties.timeFormat = 'HH:mm:ss';
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();

      expect(element.find('input').eq(1).val()).toBe('14:02:55');
    });

    it('should parse correctly date and time', function() {
      element.find('input').val('01/09/2015').triggerHandler('input');
      element.find('input').eq(1).val('2:02:55 PM').triggerHandler('input');

      expect(scope.properties.value).toEqual(new Date('2015-09-01T14:02:55.000Z'));
    });

    it('should parse correctly date and time according to time format', function() {
      scope.properties.timeFormat = 'HH:mm:ss';
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();

      element.find('input').val('01/09/2015').triggerHandler('input');
      element.find('input').eq(1).val('14:02:55').triggerHandler('input');

      expect(scope.properties.value).toEqual(new Date('2015-09-01T14:02:55.000Z'));
    });

    it('should be displayed always time option', function () {
      expect(element.find('input').length).toBe(2);
    });
  });

  describe('now button', function() {
    it('should set the date to current day and time',function() {
      scope.properties.showNow = true;
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();

      element.find('.input-group .input-group-btn button.now').click();

      expect(element.find('input').val()).toEqual(filter('date')(new Date(), 'dd/MM/yyyy'));
    });
    it('should be hidden when property showNow is false', function() {
      scope.properties.showNow = false;
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);

      scope.$apply();
      expect(element.find('.input-group .input-group-btn button.now').length).toBe(0);
    });
    it('should set current absolute date and time into UTC date and time',function() {
      scope.properties.withTimeZone = false;
      scope.properties.showNow = true;
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();

      element.find('.input-group .input-group-btn button.now').click();

      expect(element.find('input').eq(1).val()).toEqual(filter('date')(new Date(), scope.properties.timeFormat));
    });

    it('should set current GMT timeZone date and time to current GMT timeZone date and time',function() {
      scope.properties.withTimeZone = true;
      scope.properties.showNow = true;
      var element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      scope.$apply();

      element.find('.input-group .input-group-btn button.now').click();

      expect(scope.properties.value.toLocaleString()).toEqual(new Date().toLocaleString());
    });

  });

  describe('calendar button', function() {
    beforeEach(function(){
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
    });

    it('should be displayed by default', function () {
      expect(element.find('.input-group .input-group-btn button.calendar').length).toBe(1);
    });

    it('should be disabled if date picker disabled', function () {
      scope.properties.readOnly = true;
      scope.$apply();

      var button = element.find('.input-group .input-group-btn button.calendar');
      expect(button.attr('disabled')).toBe('disabled');
    });
  });

  describe('time button', function() {
    beforeEach(function(){
      element = $compile('<pb-date-time-picker></pb-date-time-picker>')(scope);
      element.appendTo(body);
      scope.$apply();
    });

    it('should be displayed by default', function () {
      expect(element.find('.input-group .input-group-btn button.timepicker').length).toBe(1);
    });

    it('should be disabled if time picker disabled', function () {
      scope.properties.readOnly = true;
      scope.$apply();

      var button = element.find('.input-group .input-group-btn button.timepicker');
      expect(button.attr('disabled')).toBe('disabled');
    });
  });

});
