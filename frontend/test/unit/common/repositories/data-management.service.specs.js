describe('DataManagementService', () => {

  var dataManagementRepo, $httpBackend, alerts, gettextCatalog;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories','bonitasoft.designer.common.services'));

  beforeEach(inject(function (_dataManagementRepo_, _$httpBackend_, _alerts_, _gettextCatalog_) {
    dataManagementRepo = _dataManagementRepo_;
    $httpBackend = _$httpBackend_;
    gettextCatalog = _gettextCatalog_;
    alerts = _alerts_;
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
    let Success = {data: {}};

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
                  'name': 'findByNameAndPhoneNumber',
                  'filters': [{'name': 'name', 'type': 'STRING'}, {'name': 'phoneNumber', 'type': 'STRING'}]
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
    let Error = {error: true, objects: []};
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


  it('should return an empty array and error status when try to load businessObject with business data repository is on error', () => {
    $httpBackend.whenGET('./bdm/json').respond(500);

    dataManagementRepo.getDataObjects().then((data) => {
      expect(data.error).toEqual(true);
      expect(data.objects).toEqual([]);
    });
    $httpBackend.flush();
  });

  describe('dataManagementRepo retrieve attributes for relation businessObject', () => {
    beforeEach(() => {
      let Success = {
        data: {
          businessObjects: [
            {
              qualifiedName: 'com.invoice.model.A',
              name: 'A',
              description: '',
              attributes: [
                {
                  name: 'aa',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'b',
                  type: 'AGGREGATION',
                  nullable: 'true',
                  collection: 'false',
                  description: '',
                  reference: 'com.invoice.model.B',
                  fetchType: 'LAZY'
                }
              ],
              attributeQueries: [
                {
                  name: 'findByAa',
                  filters: [
                    {
                      name: 'aa',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            },
            {
              qualifiedName: 'com.invoice.model.Address',
              name: 'Address',
              description: '',
              attributes: [
                {
                  name: 'street',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'city',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'code',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'description',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'billingAddress',
                  type: 'BOOLEAN',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                }
              ],
              attributeQueries: [
                {
                  name: 'findByStreet',
                  filters: [
                    {
                      name: 'street',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'findByCity',
                  filters: [
                    {
                      name: 'city',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'findByCode',
                  filters: [
                    {
                      name: 'code',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'findByDescription',
                  filters: [
                    {
                      name: 'description',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'findByBillingAddress',
                  filters: [
                    {
                      name: 'billingAddress',
                      type: 'BOOLEAN'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            },
            {
              qualifiedName: 'com.invoice.model.B',
              name: 'B',
              description: '',
              attributes: [
                {
                  name: 'bb',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'a',
                  type: 'AGGREGATION',
                  nullable: 'true',
                  collection: 'false',
                  description: '',
                  reference: 'com.invoice.model.A',
                  fetchType: 'LAZY'
                }
              ],
              attributeQueries: [
                {
                  name: 'findByBb',
                  filters: [
                    {
                      name: 'bb',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            },
            {
              qualifiedName: 'com.invoice.model.Company',
              name: 'Company',
              description: '',
              attributes: [
                {
                  name: 'name',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'company',
                  type: 'AGGREGATION',
                  nullable: 'true',
                  collection: 'false',
                  description: '',
                  reference: 'com.invoice.model.Company',
                  fetchType: 'LAZY'
                }
              ],
              attributeQueries: [
                {
                  name: 'findByName',
                  filters: [
                    {
                      name: 'name',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            },
            {
              qualifiedName: 'com.invoice.model.Customer',
              name: 'Customer',
              description: 'Customer Object, It is referenced by an Invoice',
              attributes: [
                {
                  name: 'nif',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'name',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'labels',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'true',
                  description: ''
                },
                {
                  name: 'addresses',
                  type: 'COMPOSITION',
                  nullable: 'true',
                  collection: 'true',
                  description: '',
                  reference: 'com.invoice.model.Address',
                  fetchType: 'LAZY'
                },
                {
                  name: 'customer',
                  type: 'AGGREGATION',
                  nullable: 'true',
                  collection: 'false',
                  description: '',
                  reference: 'com.invoice.model.Customer',
                  fetchType: 'LAZY'
                }
              ],
              attributeQueries: [
                {
                  name: 'findByNif',
                  filters: [
                    {
                      name: 'nif',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'findByName',
                  filters: [
                    {
                      name: 'name',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            },
            {
              qualifiedName: 'com.invoice.model.Invoice',
              name: 'Invoice',
              description: 'This is the main object invoice which has a relationship(Agregation -lazy) with a Customer and a list of InvoiceLines in Composition (Eager)',
              attributes: [
                {
                  name: 'invoiceDate',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'customer',
                  type: 'AGGREGATION',
                  nullable: 'true',
                  collection: 'false',
                  description: '',
                  reference: 'com.invoice.model.Customer',
                  fetchType: 'LAZY'
                },
                {
                  name: 'invoiceLines',
                  type: 'COMPOSITION',
                  nullable: 'true',
                  collection: 'true',
                  description: '',
                  reference: 'com.invoice.model.InvoiceLine',
                  fetchType: 'EAGER'
                }
              ],
              attributeQueries: [
                {
                  name: 'findByInvoiceDate',
                  filters: [
                    {
                      name: 'invoiceDate',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            },
            {
              qualifiedName: 'com.invoice.model.InvoiceLine',
              name: 'InvoiceLine',
              description: 'It is the element that describe what is going to be invoiced. Has a reference to a Product  (Agregation - Lazy)',
              attributes: [
                {
                  name: 'quantity',
                  type: 'INTEGER',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'price',
                  type: 'INTEGER',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'product',
                  type: 'AGGREGATION',
                  nullable: 'true',
                  collection: 'false',
                  description: '',
                  reference: 'com.invoice.model.Product',
                  fetchType: 'EAGER'
                }
              ],
              attributeQueries: [
                {
                  name: 'findByQuantity',
                  filters: [
                    {
                      name: 'quantity',
                      type: 'INTEGER'
                    }
                  ]
                },
                {
                  name: 'findByPrice',
                  filters: [
                    {
                      name: 'price',
                      type: 'INTEGER'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            },
            {
              qualifiedName: 'com.invoice.model.Product',
              name: 'Product',
              description: '',
              attributes: [
                {
                  name: 'name',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'false',
                  description: ''
                },
                {
                  name: 'labels',
                  type: 'STRING',
                  nullable: 'true',
                  collection: 'true',
                  description: ''
                }
              ],
              attributeQueries: [
                {
                  name: 'findByName',
                  filters: [
                    {
                      name: 'name',
                      type: 'STRING'
                    }
                  ]
                },
                {
                  name: 'find',
                  filters: []
                },
                {
                  name: 'findByPersistenceId',
                  filters: [
                    {
                      name: 'persistenceId',
                      type: 'INTEGER'
                    }
                  ]
                }
              ],
              constraintQueries: [],
              customQueries: []
            }
          ]
        }
      };

      $httpBackend.whenGET('./bdm/json').respond(Success.data);
      dataManagementRepo.getDataObjects();
      $httpBackend.flush();
    });

    it('should load all attributes when BO get 2 level', () => {
      let dataObject = dataManagementRepo.getDataObject('com.invoice.model.Invoice');

      expect(dataObject.businessObject.name).toEqual('com.invoice.model.Invoice');

      let attributes = dataObject.businessObject.attributes;
      expect(attributes.length).toEqual(3);

      let invoiceLine = attributes[2];
      expect(invoiceLine.reference).toEqual('com.invoice.model.InvoiceLine');
      expect(invoiceLine.attributes.length).toEqual(3);
    });

    it('should cut businessObject attributes when BO get more than 5 level', () => {
      let dataObject = dataManagementRepo.getDataObject('com.invoice.model.A');

      expect(dataObject.businessObject.name).toEqual('com.invoice.model.A');

      let attributes = dataObject.businessObject.attributes;
      expect(attributes.length).toEqual(2);

      let objectBLevel1 = attributes[1];
      expect(objectBLevel1.reference).toEqual('com.invoice.model.B');
      expect(objectBLevel1.attributes.length).toEqual(2);

      let objectALevel2 = objectBLevel1.attributes[1];
      expect(objectALevel2.reference).toEqual('com.invoice.model.A');
      expect(objectALevel2.attributes.length).toEqual(2);

      let objectBLevel3 = objectALevel2.attributes[1];
      expect(objectBLevel3.reference).toEqual('com.invoice.model.B');
      expect(objectBLevel3.attributes.length).toEqual(2);

      let objectALevel4 = objectBLevel3.attributes[1];
      expect(objectALevel4.reference).toEqual('com.invoice.model.A');
      expect(objectALevel4.attributes.length).toEqual(2);

      let lastObjectLevel = objectALevel4.attributes[1];
      expect(lastObjectLevel.reference).toEqual('com.invoice.model.B');
      expect(lastObjectLevel.attributes.length).toEqual(0);
    });
  });


});
