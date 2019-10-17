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
        data: {
          __schema: {
            types: [
              {name: 'Query', kind: 'OBJECT', description: null},
              {name: 'com_bonita_model_Currency', kind: 'OBJECT', description: null},
              {
                name: 'String',
                kind: 'SCALAR',
                description: 'The `String` scalar type represents textual data, represented as UTF-8 character sequences. The String type is most often used by GraphQL to represent free-form human-readable text.'
              },
              {name: 'com_bonita_model_Order', kind: 'OBJECT', description: null},
              {
                name: 'Int',
                kind: 'SCALAR',
                description: 'The `Int` scalar type represents non-fractional signed whole numeric values. Int can represent values between -(2^31) and 2^31 - 1.'
              }, {name: 'com_bonita_model_OrderInfo', kind: 'OBJECT', description: null},
              {name: 'com_bonita_model_Adress', kind: 'OBJECT', description: null}
            ]
          }
        }
      }
    };

    $httpBackend.whenPOST('./bdr').respond(Success.data);

    dataManagementRepo.getDataObjects().then((data) => {
      expect(data.error).toEqual(false);
      expect(data.objects.length).toEqual(4);
      expect(data.objects[0].id).toEqual('com_bonita_model_Currency');
      expect(data.objects[0].qualifiedName).toEqual('com.bonita.model.Currency');
      expect(data.objects[0].name).toEqual('Currency');
    });
    $httpBackend.flush();
  });

  it('should load an empty arrays when request is on error', () => {
    let Success = { data: {} };

    $httpBackend.whenPOST('./bdr').respond(Success.data);

    dataManagementRepo.getDataObjects().then((data) => {
      expect(data.error).toEqual(false);
      expect(data.objects).toEqual([]);
    });
    $httpBackend.flush();
  });

  it('should load queries when a businessData is give on parameter', () => {
    let Success =
      {
        data: {
          data: {
            queriesAttributeQuery: {
              fields: [
                {
                  name: 'findByName',
                  args: [{name: 'name', type: {ofType: {name: 'String'}}}],
                  type: {name: 'com_bonita_model_Customer'}
                },
                {
                  name: 'findByLastName', args: [{name: 'lastName', type: {ofType: {name: 'String'}}}],
                  type: {name: 'com_bonita_model_Customer'}
                },
                {
                  name: 'find', args: [], type: {name: 'com_bonita_model_Customer'}
                },
                {
                  name: 'findByPersistenceId', args: [{name: 'persistenceId', type: {ofType: {name: 'int'}}}],
                  type: {name: 'com_bonita_model_Customer'}
                }
              ]
            },
            queriesContraintQuery: null,
            queriesCustomQuery: null
          }
        }
      };

    $httpBackend.whenPOST('./bdr').respond(Success.data);

    dataManagementRepo.getQueries('com_bonita_model_Customer').then((data) => {
      expect(data.additionalQuery.length).toEqual(1);
      expect(data.defaultQuery.length).toEqual(3);
      expect(data.defaultQuery[0].filters[0].name).toEqual('name');
      expect(data.defaultQuery[0].filters[0].type).toEqual('String');
    });
    $httpBackend.flush();
  });

  it('should return an empty array when try to load queries on business object with business data repository is on error', () => {
    $httpBackend.whenPOST('./bdr').respond(500);

    dataManagementRepo.getQueries('com_bonita_model_Customer').then((data) => {
      expect(data).toEqual([]);
    });
    $httpBackend.flush();
  });

  it('should return an empty array and error status when try to load businessObject with business data repository is on error', () => {
    $httpBackend.whenPOST('./bdr').respond(500);

    dataManagementRepo.getDataObjects().then((data) => {
      expect(data.error).toEqual(true);
      expect(data.objects).toEqual([]);
    });
    $httpBackend.flush();
  });
});
