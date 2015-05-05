describe('pbSelect', function () {

  var $compile, scope, $rootScope;

  var collection = [{name: 'foo', value: 'baz'}, {name: 'bar', value: 'qux'}];

  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function (_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    scope = _$rootScope_.$new();
    scope.properties = {
      labelHidden: true,
      disabled: false
    };
  }));

  it('should contains only a select by default', function () {

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('label').length).toBe(0);
    expect(element.find('select').length).toBe(1);
  });

  it('should display a label when required', function () {
    scope.properties = {
      labelHidden: false,
      label: 'hello'
    };
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('label').text().trim()).toBe('hello');
  });

  it('should have label and input side by side', function () {
    scope.properties = {
      labelHidden: false,
      labelPosition: 'left',
      labelWidth: 5
    };
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('label').hasClass('col-xs-5')).toBeTruthy();
    expect(element.find('select').parent().hasClass('col-xs-7')).toBeTruthy();
  });

  it('should update model value with selected model', function () {
    scope.properties = {
      availableValues: collection
    };

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    element.find('select').val("1");
    element.find('select').triggerHandler('change');

    expect(scope.properties.value).toBe(collection[1]);
  });

  it('should set option\'s label using complex displayed key', function () {
    scope.properties = {
      availableValues: [{'foo': {'bar': 'baz'}}],
      displayedKey: 'foo.bar'
    };
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    var labels = [].map.call(element.find('option'), function (option) {
      return option.innerText.trim();
    });

    expect(labels).toEqual(['', 'baz']);
  });

  it('should use whole object if no displayed key is specified', function () {
    scope.properties = {
      availableValues: collection
    };
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
    expect(element[0].querySelector('[style="display:none"]')).toBeDefined();
  });

  it('should customize the placeholder', function () {
    scope.properties.placeholder = 'jeanne';
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();
    expect(element[0].querySelector('[style="display:none"]').innerText.trim()).toBe('jeanne');
  });

  it('should generates option form a list', function () {
    scope.properties.availableValues = ['jeanne', 'jean', 'marcel'];
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    var labels = [].map.call(element[0].querySelectorAll('option:not([style])'), function (option) {
      return option.innerText.trim();
    });

    expect(labels).toEqual([
      'jeanne',
      'jean',
      'marcel'
    ]);

  });

  it('should be disabled when requested', function () {
    scope.properties.disabled = true;

    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    expect(element.find('select').attr('disabled')).toBe('disabled');
  });

  it('should return value using returned key', function () {
    scope.properties = {
      availableValues: [{'foo': {'bar': 'baz'}}],
      returnedKey: 'foo.bar'
    };
    var element = $compile('<pb-select></pb-select>')(scope);
    scope.$apply();

    element.find('select').val("0");
    element.find('select').triggerHandler('change');

    expect(scope.properties.value).toBe('baz');
  });

  it('should select the correct option if value is  not null', function(){
    scope.properties.availableValues = ['jeanne', 'serge', 'bob'];
    scope.properties.value =  'serge' ;
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    var selectedIndex = widget.find("select")[0].value;

    expect(scope.properties.availableValues[selectedIndex]).toBe('serge');
  });

  it('should select the correct option if value is not null, depending of returnedKey', function(){
    var scope = $rootScope.$new();
    scope.properties = {
      availableValues: [{"name": 'jeanne'}, {"name": 'serge'}, {"name": 'bob'}],
      value: 'serge',
      displayedKey: 'name',
      returnedKey: 'name'
    }
    var widget = $compile('<pb-select></pb-select>')(scope);
    scope.$digest();

    var selectedIndex = widget.find("select")[0].value;

    expect(scope.properties.availableValues[selectedIndex]).toEqual({"name": 'serge'});
  });
});
