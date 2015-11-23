var path = require('path');

describe('UI designer: home', function() {
  var pagePath = path.resolve(__dirname, '../../fixtures/page-testImport.zip');
  var widgetPath = path.resolve(__dirname, '../../fixtures/widget-testImport.zip');

  function getListedPages() {
    return element.all(by.css('home-pages .Artifact-info'));
  }

  function getListedWidgets() {
    return element.all(by.css('home-widgets .Artifact-info'));
  }

  beforeEach(function() {
    browser.get('/designer/#/en/home');
  });

  it('should list pages', function() {
    var elements = getListedPages();
    expect(elements.count()).toBeGreaterThan(0);
  });

  it('should import a page and display import report', function() {
    var button = $('#pages .btn[title="Import a page or a form"]');
    var input = $('.file-upload-input');
    var upload = element(by.partialButtonText('Import'));
    var modal = $('.modal');
    var pages = getListedPages();

    var nbPages;

    pages.count().then(function(nb) {
      nbPages = nb;
    });

    button.click();
    input.sendKeys(pagePath);
    upload.click();
    //modal dismiss too slowly for test to execute
    //we need to wait for angular to finish processing modal
    browser.waitForAngular();
    expect(modal.isPresent()).toBe(false);
    pages.count().then(function(nb) {
      expect(nb).toEqual(nbPages + 1);
    });

    expect($$('alerts .ui-alert-success p').first().getText()).toContain('Page testImport successfully imported.');

    pages.count().then(function(nb) {
      nbPages = nb;
    });

    button.click();
    input.sendKeys(pagePath);
    upload.click();

    //try to reimport expecting a confirmation message on overridden elements
    expect(modal.isPresent()).toBe(true);
    expect($('.modal-title').getText()).toEqual('Import testImport');
    expect($('.ImportReport-page').getText()).toContain('testImport');

    $('button.btn-primary').click();
    //modal dismiss too slowly for test to execute
    //we need to wait for angular to finish processing modal
    browser.waitForAngular();

    expect(modal.isPresent()).toBe(false);
    pages = getListedPages();
    pages.count().then(function(nb) {
      expect(nb).toEqual(nbPages);
    });
    var successText = $$('alerts .ui-alert-success p').get(2).getText();
    expect(successText).toContain('Overridden artifacts:');
    expect(successText).toContain('Widget testWidgetImport');
    expect(successText).toContain('Page testImport successfully imported.');

  });

  it('should list widgets', function() {
    var elements = getListedWidgets();
    expect(elements.count()).toBeGreaterThan(0);
  });


  it('should import a custom widget and display an import report', function() {
    var button = $('#widgets .btn[title="Import a widget"]');
    var input = $('.file-upload-input');
    var upload = element(by.partialButtonText('Import'));
    var modal = $('.modal');
    var widgets = getListedWidgets();

    var nbWidgets;

    widgets.count().then(function(nb) {
      nbWidgets = nb;
    });

    button.click();
    input.sendKeys(widgetPath);
    upload.click();
    //we need to wait for angular to finish processing modal
    browser.waitForAngular();
    expect(modal.isPresent()).toBe(false);
    widgets.count().then(function(nb) {
      expect(nb).toEqual(nbWidgets + 1);
    });

    expect($$('alerts .ui-alert-success p').first().getText()).toBe('Widget testImport successfully imported.');
  });

});
