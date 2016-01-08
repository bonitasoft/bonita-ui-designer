(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.home.import')
    .service('importErrorMessagesService', importErrorMessagesService);

  function importErrorMessagesService(gettextCatalog, gettext) {

    const ERRORS = {
      'SERVER_ERROR': {
        cause: gettext('A server error occurred.'),
        additionalInfos: gettext('Check the log files')
      },
      'PAGE_NOT_FOUND': {
        cause: gettext('Incorrect zip structure.'),
        additionalInfos: gettext('Check that the zip archive contains the file {{ infos.modelfile }}.')
      },
      'MODEL_NOT_FOUND': {
        cause: gettext('Incorrect zip structure.'),
        additionalInfos: gettext('Check that the zip archive contains one of following files {{ infos.modelfiles }}.')
      },
      'UNEXPECTED_ZIP_STRUCTURE': {
        cause: gettext('Incorrect zip structure.'),
        additionalInfos: gettext('Check that the zip file structure matches a standard UI Designer export.')
      },
      'CANNOT_OPEN_ZIP': {
        cause: gettext('Corrupted zip archive.')
      },
      'JSON_STRUCTURE': {
        cause: gettext('Invalid {{ infos.modelfile }} json file structure'),
        additionalInfos: gettext('Check that the json file structure matches a standard UI Designer export.')
      }
    };

    return {
      getErrorContext
    };

    function getErrorContext(error, artifactType) {
      var errorInfos = ERRORS[error.type] || { cause: error.message };
      var errorContext = { consequence: getConsequence(artifactType) };
      Object.keys(errorInfos)
        .forEach((name) => errorContext[name] = gettextCatalog.getString(errorInfos[name], error));
      return errorContext;
    }

    /**
     * Need multiple keys to manage gender translations
     */
    function getConsequence(artifactType) {
      artifactType = artifactType || gettext('artifact');
      if (artifactType === 'page') {
        return gettext('The page has not been imported.');
      }

      if (artifactType === 'widget') {
        return gettext('The widget has not been imported.');
      }

      return gettextCatalog.getString('The {{ artifactType }} has not been imported.', { artifactType: gettextCatalog.getString(artifactType) });
    }
  }
})();
