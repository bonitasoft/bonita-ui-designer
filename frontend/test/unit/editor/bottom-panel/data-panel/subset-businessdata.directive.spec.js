describe('SubsetBusinessData directive', () => {

  let controller, element, $scope, $compile;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    $scope = $rootScope.$new();
    $scope.ctrl = {
      businessData: {},
      queriesForObject: {},
      variableInfo: {},
    };
    element = $compile('<subset-business-data business-data="ctrl.businessData" queries-for-object="ctrl.queriesForObject" variable-info="ctrl.variableInfo"></subset-business-data>')($scope);
    $scope.$apply();
    controller = element.controller('subsetBusinessData');
  }));

  it('should init pagination data when entry data is empty', () => {
    expect(controller.variableInfo.pagination.p).toBe(0);
    expect(controller.variableInfo.pagination.c).toBe(10);
  });


  it('should store value of variable when query is selected', () => {
    $scope.ctrl.businessData = {
      id: 'com_bonitasoft_Customer',
      name: 'Customer',
      qualifiedName: 'com.bonitasoft.Customer',
      type: 'model',
    };
    $scope.ctrl.queriesForObject = {
      additionalQuery: [
        {displayName: 'find', query: 'find', filters: []},
        {
          displayName: 'findByNameAndLastName', query: 'findByNameAndLastName',
          filters: [{name: 'name', type: 'String'}, {name: 'lastName', type: 'String'}]
        }],
      defaultQuery: [
        {displayName: 'name', query: 'findByName', filters: [{name: 'name', type: 'String'}]},
        {displayName: 'lastName', query: 'findByLastName', filters: [{name: 'lastName', type: 'String'}]},
        {displayName: 'persistenceId', query: 'findByPersistenceId', filters: [{name: 'persistenceId', type: 'Int'}]}
      ]
    };

    element = $compile('<subset-business-data business-data="ctrl.businessData" queries-for-object="ctrl.queriesForObject" variable-info="ctrl.variableInfo"></subset-business-data>')($scope);
    $scope.$apply();

    controller.displayFilters({displayName: 'findByNameAndLastName', query: 'findByNameAndLastName'}, true);

    expect(controller.variableInfo.filters.length).toBe(2);
    expect(controller.variableInfo.filters[0].name).toBe('name');
    expect(controller.variableInfo.filters[0].value).toBe('');
    expect(controller.variableInfo.filters[1].name).toBe('lastName');
    expect(controller.variableInfo.filters[1].value).toBe('');

    expect(controller.variableInfo.data).toEqual({
      displayValue: 'findByNameAndLastName [com.bonitasoft.Customer]',
      businessObjectName: 'Customer',
      query: {name: 'findByNameAndLastName', displayName: 'findByNameAndLastName'},
      id: 'com_bonitasoft_Customer',
      qualifiedName: 'com.bonitasoft.Customer',
      filters: [{name: 'name', value: '', type: 'String'}, {name: 'lastName', value: '', type: 'String'}],
      pagination: {p: 0, c: 10}
    });
    expect(controller.variableInfo.query.displayName).toBe('findByNameAndLastName');
    expect(controller.variableInfo.query.name).toBe('findByNameAndLastName');
  });

});
