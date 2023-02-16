describe('pbSelect', function () {

  var $compile, scope, $rootScope, gettextCatalog;

  var collection = [{name: 'foo', value: 'baz'}, {name: 'bar', value: 'qux'}];

  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('bonitasoft.ui.services'));

  beforeEach(inject(function (_$compile_, _$rootScope_, _gettextCatalog_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    scope = _$rootScope_.$new();
    gettextCatalog = _gettextCatalog_;
    gettextCatalog.setStrings('fr-FR', {
      'red': 'rouge',
      'green': 'vert'
    });
    gettextCatalog.setCurrentLanguage('fr-FR');
    scope.properties = {
      isBound: function() {
        return false;
      },
      labelHidden: false,
      label: 'hello',
      disabled: false
    };
  }));

  it('should contains only a select by default', function () {

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('label').length).toBe(1);
    expect(element.find('select').length).toBe(1);
  });

  it('should display a label when required', function () {
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('label').text().trim()).toBe('hello');
  });

  it('should have label and input side by side', function () {
    scope.properties = angular.extend(scope.properties, {
      labelHidden: false,
      labelPosition: 'left',
      labelWidth: 5
    });
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('label').hasClass('col-xs-5')).toBeTruthy();
    expect(element.find('select').parent().hasClass('col-xs-7')).toBeTruthy();
  });

  it('should allows html markup to be interpreted', function() {
    scope.properties = angular.extend(scope.properties, {
      label: '<span>allow html!</span>',
      allowHTML: true
    });

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();
    var label = element.find('label');
    expect(label.text().trim()).toBe('allow html!');
  });

  it('should prevent html markup to be interpreted', function() {
    scope.properties = angular.extend(scope.properties, {
      label: '<span>allow html!</span>',
      allowHTML: false
    });

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();
    var label = element.find('label');
    expect(label.text().trim()).toBe('<span>allow html!</span>');
  });

  it('should update model value with selected model', function () {
    scope.properties.availableValues = collection;

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    element.find('select').val('1');
    element.find('select').triggerHandler('change');

    expect(scope.properties.value).toBe(collection[1]);
  });

  it('should set option\'s label using complex displayed key', function () {
    scope.properties = angular.extend(scope.properties, {
      availableValues: [{'foo': {'bar': 'baz'}}],
      displayedKey: 'foo.bar'
    });
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    var labels = [].map.call(element.find('option'), function (option) {
      return option.innerText.trim();
    });

    expect(labels).toEqual(['', 'baz']);
  });

  it('should use whole object if no displayed key is specified', function () {
    scope.properties.availableValues = collection;
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    var labels = [].map.call(element.find('option'), function (option) {
      return option.innerText.trim();
    });

    expect(labels).toEqual([
      '',
      '{"name":"foo","value":"baz"}',
      '{"name":"bar","value":"qux"}']);
  });

  it('should set a placeholder', function () {
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();
    expect(element[0].querySelector('[style=\'display:none\']')).toBeDefined();
  });

  it('should customize the placeholder', function () {
    scope.properties.placeholder = 'jeanne';
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();
    expect(element[0].querySelector('[style=\'display:none\']').innerText.trim()).toBe('jeanne');
  });

  it('should generates option form a list', function () {
    scope.properties.availableValues = ['jeanne', 'jean', 'marcel', 'red', 'green'];
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    var labels = [].map.call(element[0].querySelectorAll('option:not([style])'), function (option) {
      return option.innerText.trim();
    });

    expect(labels).toEqual([
      'jeanne',
      'jean',
      'marcel',
      'rouge',
      'vert'
    ]);

  });

  it('should be disabled when requested', function () {
    scope.properties.disabled = true;

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('select').attr('disabled')).toBe('disabled');
  });
  it('should be required when requested', function () {
    scope.properties.required = true;

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('select').attr('required')).toBe('required');
  });

  it('should return value using returned key', function () {
    scope.properties = angular.extend(scope.properties, {
      availableValues: [{'foo': {'bar': 'baz'}}],
      returnedKey: 'foo.bar'
    });
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    element.find('select').val('0');
    element.find('select').triggerHandler('change');

    expect(scope.properties.value).toBe('baz');
  });

  it('should select the correct option if value is  not null', function(){
    scope.properties.availableValues = ['jeanne', 'serge', 'bob'];
    scope.properties.value =  'serge' ;
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();
    var selectedIndex = widget.find('select')[0].value;

    expect(scope.properties.availableValues[selectedIndex]).toBe('serge');
  });

  it('should select the correct option if value is not null, depending of returnedKey', function(){
    scope.properties = angular.extend(scope.properties, {
      availableValues: [{'name': 'jeanne'}, {'name': 'serge'}, {'name': 'bob'}],
      value: 'serge',
      displayedKey: 'name',
      returnedKey: 'name'
    });
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    var selectedIndex = widget.find('select')[0].value;
    expect(scope.properties.availableValues[selectedIndex]).toEqual({'name': 'serge'});
  });

  it('should leave the value as it is if no available values but reset value if available value do not contain value', function(){
    scope.properties = angular.extend(scope.properties, {
      value: 'jean',
      displayedKey: 'name',
      returnedKey: 'name'
    });
    $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    expect(scope.properties.value).toEqual('jean');
    scope.properties.availableValues = [{'name': 'jeanne'}, {'name': 'serge'}, {'name': 'bob'}];

    scope.$apply();

    expect(scope.properties.value).toBeNull();
  });

  it('should not change model value if the available values are not available yet', function () {
    scope.properties = angular.extend(scope.properties, {
      value: 'jean',
      displayedKey: 'name',
      returnedKey: 'name'
    });

    scope.$watch('properties.value', function (value) {
      expect(value).toBeDefined();
    });

    $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(scope.properties.value).toEqual('jean');

    scope.properties.availableValues = [{'name': 'jean'}, {'name': 'serge'}, {'name': 'bob'}];
    scope.$apply();

    expect(scope.properties.value).toEqual('jean');
  });

  it('should allow setting the value of the select after the available values', function(){
    scope.properties = angular.extend(scope.properties, {
      displayedKey: 'name',
      returnedKey: 'name'
    });
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    scope.properties.availableValues = [{'name': 'jeanne'}, {'name': 'serge'}, {'name': 'bob'}];

    scope.$apply();

    expect(scope.properties.value).toBeNull();

    scope.properties.value = 'serge';

    scope.$apply();

    expect(scope.properties.value).toEqual('serge');
    var selectedIndex = widget.find('select')[0].value;
    expect(scope.properties.availableValues[selectedIndex]).toEqual({'name': 'serge'});
  });

  it('should allow setting value to null if available values contain null value', function(){
    scope.properties = angular.extend(scope.properties, {
        value: null,
        displayedKey: 'firstname',
        returnedKey: 'username'
    });
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    expect(scope.properties.value).toBeNull();
    scope.properties.availableValues = [{'firstname': 'jeanne', 'username': 'jeanne1'}, {'firstname': 'serge', 'username': 'serge1'}, {'firstname': '', 'username': null}];

    scope.$apply();

    expect(scope.properties.value).toBeNull();
    var selectedIndex = widget.find('select')[0].value;
    expect(scope.properties.availableValues[selectedIndex]).toEqual({'firstname': '', 'username': null});
  });

  it('should allow setting value to 0 if available values contain 0 value', function(){
    scope.properties = angular.extend(scope.properties, {
      value: 0,
      displayedKey: 'firstname',
      returnedKey: 'username'
    });
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    expect(scope.properties.value).toEqual(0);
    scope.properties.availableValues = [{'firstname': 'jeanne', 'username': 'jeanne1'}, {'firstname': 'serge', 'username': 'serge1'}, {'firstname': '', 'username': 0}];

    scope.$apply();

    expect(scope.properties.value).toEqual(0);
    var selectedIndex = widget.find('select')[0].value;
    expect(scope.properties.availableValues[selectedIndex]).toEqual({'firstname': '', 'username': 0});
  });

  it('should allow setting value to false if available values contain false value', function(){
    scope.properties = angular.extend(scope.properties, {
      value: false,
      displayedKey: 'firstname',
      returnedKey: 'username'
    });
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    expect(scope.properties.value).toEqual(false);
    scope.properties.availableValues = [{'firstname': 'jeanne', 'username': 'jeanne1'}, {'firstname': 'serge', 'username': 'serge1'}, {'firstname': '', 'username': false}];

    scope.$apply();

    expect(scope.properties.value).toEqual(false);
    var selectedIndex = widget.find('select')[0].value;
    expect(scope.properties.availableValues[selectedIndex]).toEqual({'firstname': '', 'username': false});
  });
});
