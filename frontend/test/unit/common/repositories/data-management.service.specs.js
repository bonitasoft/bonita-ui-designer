describe('DataManagementService', () => {

  var dataManagementRepo, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));

  beforeEach(inject(function (_dataManagementRepo_, _$httpBackend_) {
    dataManagementRepo = _dataManagementRepo_;
    $httpBackend = _$httpBackend_;
  }));

  it('should load only real Business Object', () => {
    let Success = {
      data: {
        businessObjects: [
          {
            qualifiedName: 'com.bonita.model.Currency',
            name: 'Currency',
            description: null
          },
          {
            qualifiedName: 'com.bonita.model.Order',
            name: 'Order',
            description: null
          },
          {
            qualifiedName: 'com.bonita.model.OrderInfo',
            name: 'OrderInfo',
            description: null
          },
          {
            qualifiedName: 'com.bonita.model.Address',
            name: 'Address',
            description: null
          }
        ]
      }
    };

    $httpBackend.whenGET('./bdm/json').respond(Success.data);

    dataManagementRepo.getDataObjects().then((data) => {
      expect(data.error).toEqual(false);
      expect(data.objects.length).toEqual(4);
      expect(data.objects[0].id).toEqual('com.bonita.model.Currency');
      expect(data.objects[0].name).toEqual('Currency');
    });
    $httpBackend.flush();
  });

  it('should load an empty array when request is on error', () => {
    let Success = { data: {} };

    $httpBackend.whenGET('./bdm/json').respond(Success.data);

    dataManagementRepo.getDataObjects().then((data) => {
      expect(data.error).toEqual(false);
      expect(data.objects).toEqual([]);
    });
    $httpBackend.flush();
  });

  it('should load queries when a businessData is given as parameter', () => {
    let Success =
      {
          data: {
            'businessObjects': [
              {
                'qualifiedName': 'com.bonita.model.Customer',
                'name': 'Customer',
                'attributeQueries': [
                  {
                    'name': 'findByName', 'filters': [{'name': 'name', 'type': 'STRING'}]
                  },
                  {
                    'name': 'find', 'filters': []
                  },
                  {
                    'name': 'findByPersistenceId', 'filters': [{'name': 'persistenceId', 'type': 'INTEGER'}]
                  }
                ],
                'constraintQueries': [
                  {
                    'name': 'findByNameAndPhoneNumber', 'filters': [{'name': 'name', 'type': 'STRING'}, {'name': 'phoneNumber', 'type': 'STRING'}]
                  }
                ],
                'customQueries': [{
                    'name': 'query1', 'filters': [{'name': 'name', 'type': 'STRING'}]
                  }
                ]
              }
            ]
          }
      };

    // To initialize the json response
    dataManagementRepo._setJsonResponse(Success);
    let queries = dataManagementRepo.getQueries('com.bonita.model.Customer');
    expect(queries.defaultQuery.length).toEqual(2);
    expect(queries.defaultQuery[0].filters[0].name).toEqual('name');
    expect(queries.defaultQuery[0].filters[0].type).toEqual('STRING');
    expect(queries.additionalQuery.length).toEqual(3);
  });

  it('should return an empty array when try to load queries on business object with business data repository is on error', () => {
    let Error = { error: true, objects: [] };
    dataManagementRepo._setJsonResponse(Error);
    let queries = dataManagementRepo.getQueries('com_bonita_model_Customer');
    expect(queries.defaultQuery.length).toEqual(0);
    expect(queries.additionalQuery.length).toEqual(0);
  });

  it('should return an empty array and error status when try to load businessObject with business data repository is on error', () => {
    $httpBackend.whenGET('./bdm/json').respond(500);

    dataManagementRepo.getDataObjects().then((data) => {
      expect(data.error).toEqual(true);
      expect(data.objects).toEqual([]);
    });
    $httpBackend.flush();
  });
});
