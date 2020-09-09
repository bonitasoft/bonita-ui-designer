/* globals __dirname */
var path = require('path');

describe('UI designer: home', function() {
  var pagePath = path.resolve(__dirname, '../../fixtures/page-testImport.zip');
  var widgetPath = path.resolve(__dirname, '../../fixtures/widget-testImport.zip');
  var fragmentPath =  path.resolve(__dirname, '../../fixtures/fragment-testImport.zip');

  function getListedFragments() {
    $('.tab-fragment').click();
    return element.all(by.css('.ArtifactList-fragment'));
  }

  path = path.resolve(__dirname, '../../fixtures/widget-testImport.zip');

  function getListedPages() {
    $('.tab-page').click();
    return element.all(by.css('.ArtifactList-page'));
  }

  function getListedWidgets() {
    $('.tab-widget').click();
    return element.all(by.css('.ArtifactList-widget'));
  }

  beforeEach(function() {
    browser.get('/bonita/#/en/home');
  });

  it('should list pages', function() {
    var elements = getListedPages();
    expect(elements.count()).toBeGreaterThan(0);
  });

  it('should import a page and display import report', function() {
    var button = $('.HomeImport');
    var input = $('.file-upload-input');
    var upload = element(by.cssContainingText('.modal-footer .btn', 'Import'));
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

    $('.ui-alert .close').click();

    pages.count().then(function(nb) {
      nbPages = nb;
    });

    button.click();
    input.sendKeys(pagePath);
    upload.click();

    //try to reimport expecting a confirmation message on overridden elements
    expect(modal.isPresent()).toBe(true);
    expect($('.modal-title').getText()).toEqual('Import testImport');

    $('button.btn-primary').click();
    //modal dismiss too slowly for test to execute
    //we need to wait for angular to finish processing modal
    browser.waitForAngular();

    expect(modal.isPresent()).toBe(false);
    pages = getListedPages();
    pages.count().then(function(nb) {
      expect(nb).toEqual(nbPages);
    });
    var successText = $$('alerts .ui-alert-success p').first().getText();
    expect(successText).toContain('Overridden artifacts:');
    expect(successText).toContain('Widget testWidgetImport');
    expect(successText).toContain('Page testImport successfully imported.');

    $('.ui-alert .close').click();

    var nbElements;
    getListedPages().count().then(function(nb) {
      nbElements = nb;
    });
    $$('.ArtifactList-page .Artifact-delete').first().click();
    $('.modal-footer .btn-primary').click();
    //we need to wait for angular to finish processing modal
    browser.waitForAngular();
    getListedPages().count().then(function(nb) {
      expect(nb).toBe(nbElements -1 );
    });
  });

  it('should list widgets', function() {
    var elements = getListedWidgets();
    expect(elements.count()).toBeGreaterThan(0);
  });


  it('should import a custom widget and display an import report', function() {
    var button = $('.HomeImport');
    var input = $('.file-upload-input');
    var upload = element(by.cssContainingText('.modal-footer .btn', 'Import'));
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
    console.log('\n\n\n'+modal.isPresent()+'\n\n\n');
    expect(modal.isPresent()).toBe(false);
    widgets.count().then(function(nb) {
      expect(nb).toEqual(nbWidgets + 1);
    });

    expect($$('alerts .ui-alert-success p').first().getText()).toBe('Widget testImport successfully imported.');

    var nbElements = 0;
    getListedWidgets().count().then(function(nb) {
      nbElements = nb;
    });
    $$('.ArtifactList-widget .Artifact-delete').first().click();
    $('.modal-footer .btn-primary').click();
    //we need to wait for angular to finish processing modal
    browser.waitForAngular();
    getListedWidgets().count().then(function(nb) {
      expect(nb).toBe(nbElements -1 );
    });
  });

  it('should create page', function(){
    $('.HomeCreate').click();
    $('.modal-body input[name="name"]').sendKeys('Test');
    element(by.css('#type-fragment')).click();
    $('.modal-footer button[type="submit"]').click();
    expect($('.EditorHeader').isPresent()).toBeTruthy();
  });

  it('should list fragments', function(){
    var elements = getListedFragments();
    expect(elements.count()).toBeGreaterThan(0);
  });

  it('should import a fragment and display an import report', function(){
    var button = $('.HomeImport');
    var input = $('.file-upload-input');
    var upload = element(by.cssContainingText('.modal-footer .btn', 'Import'));
    var modal  = $('.modal');
    var fragments = getListedFragments();

    var nbFragment;

    fragments.count().then(function(nb){
      nbFragment = nb;
    });

    button.click();
    input.sendKeys(fragmentPath);
    upload.click();
    expect(modal.isPresent()).toBe(false);
    fragments.count().then(function(nb){
      expect(nb).toEqual(nbFragment + 1);
    });

    expect($$('alerts .ui-alert-success p').first().getText()).toBe('Fragment testImport successfully imported.');

    $('.ui-alert .close').click();

    var nbElements;
    getListedFragments().count().then(function(nb) {
      nbElements = nb;
    });
    $$('.ArtifactList-fragment .Artifact-delete').get(0).click();
    $('.modal-footer .btn-primary').click();
    //we need to wait for angular to finish processing modal
    browser.waitForAngular();
    browser.executeScript('$(".modal").removeClass("fade");');
    getListedFragments().count().then(function(nb) {
      expect(nb).toBe(nbElements -1 );
    });
  });
});
