describe('pbRichTextarea', function () {

  var $compile, scope ,translations;

  beforeEach(module('textAngular'));
  beforeEach(module('textAngular-i18n'));
  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('bonitasoft.ui.filters'));
  beforeEach(module(function($provide) {
    $provide.decorator('$window', function($delegate){
      $delegate.navigator.language = 'es';
      return $delegate;
    });
    $provide.decorator('$cookies', function($delegate){
      $delegate.BOS_Locale = 'es';
      return $delegate;
    });
  }));
  angular.module('bonitasoft.ui.i18n').factory('localizationFactory', function() {
    return {
      get: function() {
        return {
          'es-ES': {
            'Bold': 'Gras',
            'Redo': 'Rétablir',
            'Undo': 'Annuler'
          }
        };
      }
    };
  });

  beforeEach(inject(function (_$compile_, $rootScope, taTranslations) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    translations = taTranslations;
    scope.properties = {
      isBound: function() {
        return false;
      },
      readOnly: false,
      required: false,
      minLength: '0',
      maxLength: '0',
      labelHidden: false,
      label: 'Default label',
      labelPosition: 'top',
      labelWidth: 4,
      value: 'content'
    };
  }));

  it('should wrap a full textAngular', function () {
    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();
    expect(translations.bold.tooltip).toEqual('Gras');
    expect(translations.redo.tooltip).toEqual('Rétablir');
    expect(translations.undo.tooltip).toEqual('Annuler');

    expect(element.find('div').find('div').find('div').find('div').find('div').attr('text-angular')).toBeDefined();
    expect(element.find('div').find('div').find('div').find('div').find('div').find('div').hasClass('ta-toolbar')).toBeTruthy();
    expect(element.find('div').find('div').find('div').find('div').find('div').find('div').next().hasClass('ta-text')).toBeTruthy();
  });

  it('should be on the top of the textAngular with default label if displayed', function () {
    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('div').hasClass('form-horizontal')).toBeFalsy();
    expect(element.find('label').text().trim()).toBe('Default label');
  });

  it('should be on the left of the textAngular if displayed', function () {
    scope.properties.labelPosition =  'left';
    scope.properties.label =  'Other Label';

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('div').hasClass('form-horizontal')).toBeTruthy();
    expect(element.find('label').text().trim()).toBe('Other Label');
  });

  it('should hidde the label when property is hidden', function () {
    scope.properties.labelHidden =  true;

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('label').length).toBe(0);
  });

  it('should adapt its width to label size when on the left', function () {
    scope.properties.labelPosition = 'left';
    scope.properties.labelWidth = '4';

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('label').attr('class')).toContain('col-xs-4');
    expect(element.find('div').find('div').find('div').attr('class')).toContain('col-xs-8');
  });

  it('should allows html markup to be interpreted', function() {
    scope.properties = angular.extend(scope.properties, {
      label: '<span>allow html!</span>',
      allowHTML: true
    });

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();
    var label = element.find('label');
    expect(label.text().trim()).toBe('allow html!');
  });

  it('should prevent html markup to be interpreted', function() {
    scope.properties = angular.extend(scope.properties, {
      label: '<span>allow html!</span>',
      allowHTML: false
    });

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();
    var label = element.find('label');
    expect(label.text().trim()).toBe('<span>allow html!</span>');
  });

  it('should be wrapped in full width div', function () {
    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('div').find('div').find('div').attr('class')).toContain('col-xs-12');
  });

  it('should be either read only nor required by default', function () {
    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('div').find('div').find('div').find('div').find('div').attr('ta-disabled')).toBe('properties.readOnly');
  });

  it('should be read only when requested', function () {
    scope.properties.readOnly = true;

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    // Note: the test only check the variable name is set
    expect(element.find('div').find('div').find('div').find('div').find('div').attr('ta-disabled')).toBe('properties.readOnly');
  });

  it('should only contain the specified toolbar elements', function () {
    scope.properties.toolbarsGrp1 = ['h1', 'h2', 'h3', 'h4'];
    scope.properties.toolbarsGrp3 = [];
    scope.properties.toolbarsGrp4 = ['h6', 'p', 'pre', 'quote'];

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('div').find('div').find('div').find('div').find('div').attr('ta-toolbar')).toEqual('[["h1","h2","h3","h4"],["h6","p","pre","quote"]]');
  });

  it('should be required when requested', function () {
    scope.properties.required = true;

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();

    expect(element.find('div').find('div').find('div').find('div').find('div').attr('ng-required')).toBeTruthy();
  });

  it('should validate minlength', function () {
    scope.properties.minLength = 5;
    scope.properties.value = '123';

    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();
    expect(element.find('div').find('div').find('div').find('div').find('div').attr('class')).toMatch('ng-invalid-ta-min-text');

    scope.properties.value = '12345';
    scope.$apply();
    expect(element.find('div').find('div').find('div').find('div').find('div').attr('class')).not.toMatch('ng-invalid-ta-min-text');
  });

  it('should validate maxlength', function () {
    scope.properties.maxLength = 5;
    scope.properties.value = '123456';

    var element = $compile('<form><pb-rich-textarea></pb-rich-textarea></form>')(scope);
    scope.$apply();
    expect(element.find('div').find('div').find('div').find('div').find('div').attr('class')).toMatch('ng-invalid-ta-max-text');

    scope.properties.value = '123';
    scope.$apply();
    expect(element.find('div').find('div').find('div').find('div').find('div').attr('class')).not.toMatch('ng-invalid-ta-max-text');
  });

  it('should set minlength to 0 when not specified to avoid errors in developer console', function () {
    scope.properties.minLength = '';
    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();
    let controller = element.controller('pbRichTextarea');

    expect(controller.minText).toBe(0);
  });

  it('should set maxlength to Number max value when not specified to avoid errors in developer console', function () {
    scope.properties.maxLength = '';
    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();
    let controller = element.controller('pbRichTextarea');

    expect(controller.maxText).toEqual(9007199254740991);
  });

  it('shouldn\'t show maxlength error when no value will be written', function () {
    var element = $compile('<pb-rich-textarea></pb-rich-textarea>')(scope);
    scope.$apply();
    element.controller('pbRichTextarea');

    scope.$apply();
    expect(element.find('div').find('div').find('div').find('div').find('div').attr('class')).not.toMatch('ng-invalid-ta-min-text ng-valid-ta-min-text');

  });
});
