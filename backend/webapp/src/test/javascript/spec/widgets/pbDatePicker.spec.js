describe('pbDatePicker', function () {

  var $compile, scope;

  beforeEach(module('pb.widgets'));
  beforeEach(module('pb.generator.services'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    //We specified the default values
    scope.properties = {
      placeholder : 'dd/MM/yyyy',
      dateFormat : 'dd/MM/yyyy',
      label : 'Date',
      labelHidden : false,
      labelWidth : 4,
      labelPosition : 'left'
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

    it('should be on the left by default', function () {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      expect(element.find('.form-horizontal').length).toBe(1);
    });

    it('should be on the top of the input if labelPosition change', function () {
      scope.properties = {
        label : 'Date',
        labelPosition: 'top'
      };

      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();
      expect(element.find('.form-horizontal').length).toBe(0);
      var label = element.find('label');
      expect(label.text().trim()).toBe('Date');
    });

    it('should not be there when displayLabel is falsy', function () {
      scope.properties = {
        labelHidden: true
      };
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });
  });

  describe('input-group', function() {
    it('should adapt its width to label size when on the left', function () {
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      var input = element.find('input').parent();
      expect(input.parent().hasClass('col-xs-8')).toBeTruthy();

    });

    it('should be wrapped in full width div when no label', function () {
      scope.properties = {
        labelHidden: true
      };
      var element = $compile('<pb-date-picker></pb-date-picker>')(scope);
      scope.$apply();

      var input = element.find('input').parent();
      expect(input.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should be wrapped in full width div when label is on the top', function () {
      scope.properties = {
        labelPosition: 'top'
      };
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
});
