(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.webResources')
    .factory('bonitaResources', bonitaResources);

  function bonitaResources() {
    const resources={
      get:[
        // Identity resources
        'identity/user',
        'identity/personalcontactdata',
        'identity/professionalcontactdata',
        'identity/group',
        'identity/membership',
        'identity/role',
        'customuserinfo/user',
        'customuserinfo/definition',
        'customuserinfo/value',
        //BPM resources
        'bpm/process',
        'bpm/processInfo',
        'bpm/process/*/contract',
        'bpm/processConnector',
        'bpm/processConnectorDependency',
        'bpm/processParameter',
        'bpm/processSupervisor',
        'bpm/actor',
        'bpm/actorMember',
        'bpm/category',
        'bpm/processResolutionProblem',
        'bpm/case',
        'bpm/case/*/context',
        'bpm/caseInfo',
        'bpm/comment',
        'bpm/archivedCase',
        'bpm/archivedCase/*/context',
        'bpm/archivedComment',
        'bpm/caseVariable',
        'bpm/archivedCaseVariable',
        'bpm/caseDocument',
        'bpm/document',
        'bpm/flowNode',
        'bpm/activity',
        'bpm/task',
        'bpm/humanTask',
        'bpm/userTask',
        'bpm/userTask/*/contract',
        'bpm/userTask/*/context',
        'bpm/manualTask',
        'bpm/activityVariable',
        'bpm/archivedActivityVariable',
        'bpm/connectorInstance',
        'bpm/archivedFlowNode',
        'bpm/archivedActivity',
        'bpm/archivedTask',
        'bpm/archivedHumanTask',
        'bpm/archivedManualTask',
        'bpm/archivedUserTask',
        'bpm/archivedTask',
        'bpm/archivedUserTask/*/context',
        'bpm/archivedConnectorInstance',
        'bpm/document',
        'bpm/archiveddocument',
        'bpm/caseDocument',
        'bpm/command',
        'bpm/connectorFailure',
        'bpm/timerEventTrigger',
        'bpm/diagram',
        // Portal Resources
        'portal/profile',
        'portal/page',
        'portal/profileMember',
        //Platform resources
        'system/session',
        'system/log',
        'platform/tenant',
        'system/feature',
        'system/license',
        'system/monitoring',
        'system/i18nlocale',
        'system/i18ntranslation',
        'platform/platform',
        'platform/jvmDynamic',
        'platform/jvmStatic',
        'platform/sytemProperty',
        'platform/license',
        'tenant/bdm',
        //Living apps
        'living/application',
        'living/application-menu',
        'living/application-page',
        // BDM
        'bdm/businessData',
        'bdm/businessDataReference',
        'bdm/businessDataQuery',
        // Form
        'form/mapping',
      ],
      post:[
        // Identity resources
        'identity/user',
        'identity/personalcontactdata',
        'identity/professionalcontactdata',
        'identity/group',
        'identity/membership',
        'identity/role',
        'customuserinfo/definition',
        //BPM resources
        'bpm/process',
        'bpm/process/*/instantiation',
        'bpm/processConnector',
        'bpm/processCategory',
        'bpm/processSupervisor',
        'bpm/actorMember',
        'bpm/category',
        'bpm/case',
        'bpm/comment',
        'bpm/caseDocument',
        'bpm/userTask',
        'bpm/userTask/*/execution',
        'bpm/manualTask',
        'bpm/document',
        'bpm/command',
        'bpm/message',
        'bpm/signal',
        // Portal Resources
        'portal/profile',
        'portal/page',
        'portal/profileMember',
        'platform/tenant',
        'platform/platform',
        'tenant/bdm',
        //Living apps
        'living/application',
        'living/application-menu',
        'living/application-page',
      ],
      put:[
        // Identity resources
        'identity/user',
        'identity/personalcontactdata',
        'identity/professionalcontactdata',
        'identity/group',
        'identity/membership',
        'identity/role',
        'customuserinfo/value',
        //BPM resources
        'bpm/process',
        'bpm/processParameter',
        'bpm/actorMember',
        'bpm/category',
        'bpm/caseVariable',
        'bpm/caseDocument',
        'bpm/flowNode',
        'bpm/activity',
        'bpm/activityReplay',
        'bpm/task',
        'bpm/humanTask',
        'bpm/userTask',
        'bpm/manualTask',
        'bpm/connectorInstance',
        'bpm/document',
        'bpm/command',
        'bpm/timerEventTrigger',
        // Portal Resources
        'portal/profile',
        'portal/page',
        'platform/platform',
        //Living apps
        'living/application',
        'living/application-menu',
        'living/application-page',
      ],
      delete:[
        // Identity resources
        'identity/user',
        'identity/group',
        'identity/membership',
        'identity/role',
        'customuserinfo/definition',
        // PM resources
        'bpm/process',
        'bpm/processCategory',
        'bpm/processSupervisor',
        'bpm/actorMember',
        'bpm/category',
        'bpm/case',
        'bpm/archivedCase',
        'bpm/caseDocument',
        'bpm/document',
        'bpm/caseDocument',
        'bpm/command',
        // Portal Resources
        'portal/profile',
        'portal/page',
        'portal/profileMember',
        'platform/platform',
        //Living apps
        'living/application',
        'living/application-menu',
        'living/application-page',
        // Form
        'form/mapping',
      ]};

    const httpVerbs = [
      {type: 'get', label: 'GET', filter: true},
      {type: 'post', label: 'POST', filter: true},
      {type: 'put', label: 'PUT', filter: true},
      {type: 'delete', label: 'DELETE', filter: true},
    ];


    return {resources: resources, httpVerbs: httpVerbs};
  }

})();

