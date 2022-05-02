describe('pbSaveButton', function () {

  var $compile, scope, element, $timeout, $window, rootScope;

  beforeEach(module('bonitasoft.ui.services', 'bonitasoft.ui.widgets'));

  beforeEach(inject(function ($injector, $rootScope) {

    $compile = $injector.get('$compile');
    $timeout = $injector.get('$timeout');
    $window = $injector.get('$window');
    rootScope = $rootScope;
    scope = $rootScope.$new();

    // set the default value for properties (as per json file)
    scope.properties = {
      label: "Save draft",
      alignment: "left",
      buttonStyle: "default",
      allowHTML: true
    };

    element = $compile('<pb-save-button></pb-save-button>')(scope);
    scope.$apply();
  }));

  describe('save button appearance', function () {

    it('should have default label', function () {

      expect(element.find('button').text().trim()).toBe('Save draft');

    });

    it('should have specified label and allows html markup to be interpreted', function () {
      scope.properties.label = '<i class="fa fa-bonita">foobar</i>';
      scope.$apply();

      expect(element.find('button').text().trim()).toBe('foobar');
      expect(element.find('button').html()).toContain('<i class="fa fa-bonita">foobar</i>');
    });

    it('should have specified label and prevent html markup to be interpreted', function () {
      scope.properties.label = '<i class="fa fa-bonita">foobar</i>';
      scope.properties.allowHTML= false;
      scope.$apply();

      expect(element.find('button').text().trim()).toContain('<i class="fa fa-bonita">foobar</i>');
      expect(element.find('button').html()).toContain('&lt;i class="fa fa-bonita"&gt;foobar&lt;/i&gt;');
    });

    it('should support changing text alignment', function () {
      // Default value is left
      expect(element.find('div').hasClass('text-left')).toBeTruthy();

      scope.properties.alignment = 'right';
      scope.$apply();

      expect(element.find('div').hasClass('text-right')).toBeTruthy();
    });

    it('should be disabled', function () {
      scope.properties.disabled = true;
      scope.$apply();

      expect(element.find('button').attr('disabled')).toBe('disabled');
    });
  });

  describe('controller actions', function () {

    beforeEach(function () {
      $window.localStorage.clear();
    });

    it('should save some data to LocalStorage', function () {
      var data = {'formInput': {"stringAttr": "value", "numericAttr": 7, "objAttr": {"name": "value"}}};
      var formId = $window.location.href; // default generated id is the URL of the page

      scope.properties.formInput = data;

      scope.$apply();

      scope.ctrl.saveInLocalStorage();

      expect(JSON.parse($window.localStorage.getItem("bonita-form-" + formId))).toEqual(data);
    });

    it('should save some data to LocalStorage with generated id', function () {
      var data = "simple text to save";
      var formId = $window.location.href; // default generated id is the URL of the page

      scope.properties.formInput = data;

      scope.$apply();

      scope.ctrl.saveInLocalStorage();

      expect(JSON.parse($window.localStorage.getItem("bonita-form-" + formId))).toEqual(data);
    });

    it('should toggle css style when saving', function () {

      var data = {'formInput': {"stringAttr": "value", "numericAttr": 7, "objAttr": {"name": "value"}}};

      scope.properties.formInput = data;

      scope.$apply();

      // initial css style
      expect(element.find('span').hasClass('glyphicon-floppy-disk')).toBeTruthy();

      scope.ctrl.saveInLocalStorage();
      // propagate scope changes to re-render directive
      scope.$apply();

      expect(element.find('span').hasClass('glyphicon-floppy-saved')).toBeTruthy();

      // after timeout the css style should be restored to initial state
      $timeout.flush();
      expect(element.find('span').hasClass('glyphicon-floppy-disk')).toBeTruthy();
    });

  });
});
