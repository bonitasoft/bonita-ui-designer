/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
angular.module('bonitasoft.designer.editor.data-panel').factory('apiExamples', function(gettextCatalog) {

  var examples = {
    'System API': [
      {
        description: gettextCatalog.getString('Get current logged user'),
        url: '../API/system/session/1',
        more: gettextCatalog.getString('User id could then be obtained by using {{youVar.user_id}}')
      }
    ],
    'BPM API': [
      {
        description: gettextCatalog.getString('Get a human task by its identifier'),
        url: '../API/bpm/humanTask/<strong>{{taskId}}</strong>',
        more: gettextCatalog.getString('Case identifier could then be retrieved by {{myVar.rootCaseId}}')
      },
      {
        description: gettextCatalog.getString('List of human task in ready state for current user (pending + assigned)'),
        url: '../API/bpm/humanTask?c=10&p=0&f=state=ready&f=user_id=<strong>{{userId}}</strong>&f=caseId=<strong>{{caseId}}</strong>'
      },
      {
        description: gettextCatalog.getString('List of cases open for a process definition id'),
        url: '../API/bpm/case?p=0&c=10&f=processDefinitionId=<strong>{{myProcessDefinitionId}}</strong>'
      },
      {
        description: gettextCatalog.getString('All process data of a case: Search for a list of case variables'),
        url: '../API/bpm/caseVariable?p=0&c=10&f=case_id=<strong>{{caseId}}</strong>'
      },
      {
        description: gettextCatalog.getString('Search a list of cases based on search indexes'),
        url: '../API/bpm/case?p=0&c=10&s=<strong>{{searchIndexValueSearched}}</strong>'
      },
      {
        description: gettextCatalog.getString('Get a case by its identifier'),
        url: '../API/bpm/case/<strong>{{caseId}}</strong>'
      }
    ],
    'Identity API': [
      {
        description: gettextCatalog.getString('Get professional details of a user'),
        url: '../API/identity/user/<strong>{{userId}}</strong>?d=professional_data'
      }
    ],
    'Customuserinfo API': [
      {
        description: gettextCatalog.getString('Get custom information of a user'),
        url: '../API/customuserinfo/user?p=0&c=10&f=userId=<strong>{{userId}}</strong>'
      }
    ],
    'BDM API': [
      {
        description: gettextCatalog.getString('Get all business variables defined in a case'),
        url: '../API/bdm/businessDataReference?f=caseId=<strong>{{caseId}}</strong>&p=0&c=10',
        more: gettextCatalog.getString('In a process form, if you have defined business variables in your process, use the context variable to retrieve them (e.g. context.businessVariableName_ref).')
      },
      {
        description: gettextCatalog.getString('Get a named business variable reference defined in a case'),
        url: '../API/bdm/businessDataReference/<strong>{{caseId}}</strong>/<strong>{{businessVariableName}}</strong>',
        alternative: {
          before: gettextCatalog.getString('In a process form, if you have defined business variables in your process, use the link to the variable to retrieve using the context variable:'),
          url: '../<strong>{{context.businessVariableName_ref.link}}</strong>',
          more: gettextCatalog.getString('Where businessVariableName is the name of the business variable defined at pool level.')
        }
      },
      {
        description: gettextCatalog.getString('Call a business data (custom) query'),
        url: '../API/bdm/businessData/<strong>{{businessDataType}}</strong>?q=<strong>{{queryName}}</strong>&p=0&c=10&f=<strong>{{filter}}</strong>',
        more: gettextCatalog.getString('Where businessDataType = com.company.model.MyData, queryName = name of the BDM query, filter = "myParam=myValue"')
      }
    ]
  };

  function flatten(arrayOfArray) {
    return [].concat(...arrayOfArray);
  }

  var transformExamplesToFlatList = function() {
    return flatten(Object.keys(examples).map(function(category) {
      return examples[category].map(function(api) {
        api.category = category;
        return api;
      });
    }));
  };

  return {
    get: transformExamplesToFlatList
  };
});
