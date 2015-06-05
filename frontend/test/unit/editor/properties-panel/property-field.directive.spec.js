describe('widget property field', function () {
  var $compile, element, template, scope, directiveScope;

  beforeEach(module('pb.templates', 'pb.directives', 'gettext'));
  beforeEach(inject(function (_$compile_, _$rootScope_) {
    $compile = _$compile_;
    var $rootScope = _$rootScope_;

    scope = $rootScope.$new();
    scope.property = {};
    scope.propertyValue = {};

    template = '<property-field property="property" property-value="propertyValue"></property-field>';
    element = $compile(template)(scope);
    scope.$apply();

    directiveScope = element.isolateScope();
  }));

  it('should display property label', function () {
    scope.property = {label: 'aLabel'};
    scope.$apply();

    expect(element.find('label').text().trim()).toBe('aLabel');
  });

  it('should display a textarea when property type is html', function () {
    scope.property = {name: "aProperty", type: 'html'};
    scope.$apply();

    expect(element.find('textarea').length).toBe(1);
  });

  it('should display a number input when property type is integer', function () {
    scope.property = {type: 'integer'};
    scope.$apply();

    expect(element.find('input[type="number"]').length).toBe(1);
  });

  it('should display a number input when property type is float', function () {
    scope.property = {type: 'float'};
    scope.$apply();

    expect(element.find('input[type="number"]').length).toBe(1);
  });

  it('should display a ngList input when property type is collection', function () {
    scope.property = {type: 'collection'};
    scope.$apply();

    expect(element.find('input[ng-list]').length).toBe(1);
  });

  it('should display a select and fill it with property choiceValues when property type is choice', function () {
    scope.property = {name: 'aProperty', type: 'choice', choiceValues: [ 'left', 'right' ] };
    scope.$apply();

    expect(element.find('select').length).toBe(1);
    expect(element.find('option').length).toBe(3); // 2 choiceValues + 1 empty
  });

  it('should display radio buttons when property type is boolean', function () {
    scope.property = {type: 'boolean'};
    scope.$apply();
    expect(element.find('.unlinked input[type="radio"]').length).toBe(2);
  });

  it('should display a text input when property type is not specified', function () {
    scope.property = {};
    scope.$apply();
    expect(element.find('.unlinked input[type="text"]').length).toBe(1);
  });

  it('should display an unlink button if not linked', function() {
    expect(element.find('i.fa-unlink').length).toBe(1);
  });

  it('should display a link button if already linked to a data', function() {
    spyOn(directiveScope, 'shouldBeLinked').and.returnValue(true);
    directiveScope.$apply();

    expect(element.find('i.fa-link').length).toBe(1);
  });

  it('should display a simple input if property is linked', function () {
    scope.property = {type: 'boolean'};
    spyOn(directiveScope, 'shouldBeLinked').and.returnValue(true);
    directiveScope.$apply();

    expect(element.find('.linked input[type="text"]').length).toBe(1);
  });
});
