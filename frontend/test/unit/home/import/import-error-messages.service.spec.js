describe('Import error messages service', function() {

  var importErrorMessagesService;

  beforeEach(angular.mock.module('bonitasoft.designer.home.import'));
  beforeEach(inject(function(_importErrorMessagesService_) {
    importErrorMessagesService = _importErrorMessagesService_;
  }));

  it('should get error message for SERVER_ERROR error', () => {
    var error = { message: 'Some error appears', type: 'SERVER_ERROR' };

    var context = importErrorMessagesService.getErrorContext(error, 'page');

    expect(context).toEqual({
      cause: 'A server error occurred.',
      consequence: 'The page has not been imported.',
      additionalInfos: 'Check the log files'
    });
  });

  it('should get error message for PAGE_NOT_FOUND error', () => {
    var error = { message: 'Some error appears', type: 'PAGE_NOT_FOUND', infos: { modelfile: 'thefile.json' } };

    var context = importErrorMessagesService.getErrorContext(error, 'page');

    expect(context).toEqual({
      cause: 'Incorrect zip structure.',
      consequence: 'The page has not been imported.',
      additionalInfos: 'Check that the zip archive contains the file thefile.json.'
    });
  });

  it('should get error message for UNEXPECTED_ZIP_STRUCTURE error', () => {
    var error = { message: 'Some error appears', type: 'UNEXPECTED_ZIP_STRUCTURE' };

    var context = importErrorMessagesService.getErrorContext(error, 'widget');

    expect(context).toEqual({
      cause: 'Incorrect zip structure.',
      consequence: 'The widget has not been imported.',
      additionalInfos: 'Check that the zip file structure matches a standard UI Designer export.'
    });
  });

  it('should get error message for CANNOT_OPEN_ZIP error', () => {
    var error = { message: 'Some error appears', type: 'CANNOT_OPEN_ZIP' };

    var context = importErrorMessagesService.getErrorContext(error, 'widget');

    expect(context).toEqual({
      cause: 'Corrupted zip archive.',
      consequence: 'The widget has not been imported.'
    });
  });

  it('should get error to display object for JSON_STRUCTURE error', () => {
    var error = { message: 'Some error appears', type: 'JSON_STRUCTURE', infos: { modelfile: 'thefile.json' } };

    var context = importErrorMessagesService.getErrorContext(error, 'page');

    expect(context).toEqual({
      cause: 'Invalid thefile.json json file structure',
      consequence: 'The page has not been imported.',
      additionalInfos: 'Check that the json file structure matches a standard UI Designer export.'
    });
  });

  it('should get error response message when type is unknown', () => {
    var error = { message: 'Some error appears', type: 'unknown' };

    var context = importErrorMessagesService.getErrorContext(error, 'page');

    expect(context).toEqual({
      cause: 'Some error appears',
      consequence: 'The page has not been imported.'
    });
  });

  it('should compute consequence according to artefact type', function() {
    var error = { message: 'Some error appears', type: 'unknown' };

    var context = importErrorMessagesService.getErrorContext(error, 'page');
    expect(context.consequence).toEqual('The page has not been imported.');

    context = importErrorMessagesService.getErrorContext(error, 'widget');
    expect(context.consequence).toEqual('The widget has not been imported.');

    context = importErrorMessagesService.getErrorContext(error, 'unknowntype');
    expect(context.consequence).toEqual('The unknowntype has not been imported.');

    context = importErrorMessagesService.getErrorContext(error);
    expect(context.consequence).toEqual('The artifact has not been imported.');
  });
});
