var HomePage = require('../pages/home.page.js');

describe('home page', function() {

  var home;

  beforeEach(function() {
    home = HomePage.get();
  });

  it('should list pages and widgets ordered by last update date descendant', function() {
    expect(home.getListedPageNames()).toEqual(['empty']);
    expect(home.getListedWidgetNames()).toEqual(['awesomeWidget']);
  });

  it('should list favorite pages and widgets ordered by last update date descendant', function() {
    expect(home.getFavoritePageNames()).toEqual(['Person']);
    expect(home.getFavoriteWidgetNames()).toEqual(['favoriteWidget']);
  });

  it('should navigate to a page', function() {
    $$('.ArtifactList-page a').first().click();

    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
  });

  it('should navigate to a widget', function() {
    $$('.ArtifactList-widget a').first().click();

    expect($('.EditorHeader-brand').getText()).toBe('CUSTOM WIDGET EDITOR');
  });

  it('should create a layout', function() {
    home.createLayout('testLayout');
    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
    browser.get('#/');
    $$('.ArtifactList-layout a').first().click();

    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
  });

  it('should create a form', function() {
    home.createForm('testForm');
    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
    browser.get('#/');
    $$('.ArtifactList-form a').first().click();

    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
  });

  it('should create a page', function() {
    home.createPage('testPage');
    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
  });

  it('should not create a page with space or special characters in name', function() {
    $('.HomeCreate').click();
    let nameInput = $('.modal-body input[name="name"]');
    nameInput.sendKeys('page name');
    let createPageButton = $('.modal-footer button[type="submit"]');
    expect(createPageButton.isEnabled()).toBeFalsy();
    expect($('.NewArtifact span.text-danger').isDisplayed()).toBeTruthy();

    nameInput.clear();
    nameInput.sendKeys('page-name');

    expect(createPageButton.isEnabled()).toBeFalsy();
    expect($('.NewArtifact span.text-danger').isDisplayed()).toBeTruthy();

    nameInput.clear();
    nameInput.sendKeys('pageName');

    expect(createPageButton.isEnabled()).toBeTruthy();
    expect($$('.NewArtifact span.text-danger').count()).toBe(0);
  });

  it('should create a widget', function() {
    home.createWidget('test');
    expect($('.EditorHeader-brand').getText()).toBe('CUSTOM WIDGET EDITOR');
  });

  it('should forbid to create a widget with an already existing name', function() {
    $('.HomeCreate').click();
    $('.modal-body input[name="name"]').sendKeys('awesomeWidget');
    element(by.css('#type-widget')).click();
    expect($('.modal-footer button[type="submit"]').isEnabled()).toBeFalsy();
    expect($('.text-danger').getText()).toEqual('A custom widget with this name already exists');
  });

  it('should open a modal to confirm page deletion', function() {
    //We want to delete a page
    $$('#person .Artifact-delete').first().click();
    //A modal is opened with a confirmation message
    expect($('#confirm-delete-popup .modal-body').getText()).toBe('Are you sure you want to delete the page Person?');
  });

  // deactivated since it fails randomly on our CI
  xit('should not delete page if user cancels deletion', function() {
    var numberOfPages = element.all(by.repeater('page in pages')).count();
    //We want to delete a page
    $$('#person .Artifact-delete').first().click();
    //A modal is opened and I click on Cancel

    //Disable animation for modal
    $$('#confirm-delete-popup').allowAnimations(false);

    $$('#confirm-delete-popup .modal-footer button').get(0).click();
    browser.waitForAngular();
    expect($$('#confirm-delete-popup').count()).toBe(0);
    //and the page is not deleted
    expect(home.getListedPageNames().count()).toBe(numberOfPages);

  });

  it('should export a page', function() {
    var btn = $$('#person .Artifact-export').first();
    var iframe = $$('.ExportArtifact').first();
    btn.click();

    expect(iframe.getAttribute('src')).toMatch(/\/export\/page\/person/);
  });

  it('should open a modal to confirm widget deletion', function() {
    //We want to delete a widget
    $$('#customAwesomeWidget .Artifact-delete').first().click();
    //A modal is opened with a confirmation message
    expect($('#confirm-delete-popup .modal-body').getText()).toBe('Are you sure you want to delete the widget awesomeWidget?');
  });

  it('should rename a page', function() {

    var btnRenamePage = $$('#person .Artifact-rename').first();
    btnRenamePage.click();

    //The link should now be a visible input with the page name
    var nameInput = $('#page-name-input-0');
    expect(nameInput.getAttribute('value')).toBe('Person');
    //We can change the name
    nameInput.sendKeys('2');
    // It should remove the input
    btnRenamePage.click();
    expect(nameInput.isPresent()).toBe(false);
  });

  it('should not rename a page with space or special characters in name', function() {
    $$('#person .Artifact-rename').first().click();

    //The link should now be a visible input with the page name
    $('#page-name-input-0').clear();
    $('#page-name-input-0').sendKeys('page name');

    expect($('.ArtifactList-page form[name="renameArtifact"] span.text-danger').isDisplayed()).toBeTruthy();

    $('#page-name-input-0').clear();
    $('#page-name-input-0').sendKeys('page-name');

    expect($('.ArtifactList-page form[name="renameArtifact"] span.text-danger').isDisplayed()).toBeTruthy();

    $('#page-name-input-0').clear();
    $('#page-name-input-0').sendKeys('pageName');

    expect($$('.ArtifactList-page form[name="renameArtifact"] span.text-danger').count()).toBe(0);
  });

  it('should remove the input to rename a page on blur', function() {

    $$('#person .Artifact-rename').first().click();

    //The link should now be a visible input with the page name
    var nameInput = $('#page-name-input-0');
    expect(nameInput.isPresent()).toBe(true);

    browser
      .executeScript('$(\'#page-name-input-0\').blur();')
      .then(function() {
        expect(nameInput.isPresent()).toBe(false);
      });

  });

  it('should set autofocus on the input if we edit a page',  function() {
    $$('#person .Artifact-rename').first().click();
    var input = $('#page-name-input-0');
    expect(input.getAttribute('id')).toEqual(browser.driver.switchTo().activeElement().getAttribute('id'));
  });

  it('should open help popup',  function() {
    $('.btn-bonita-help').click();

    expect($('.modal-header .modal-title').getText()).toBe('Help');
  });

  it('should filter widgets, pages and fragment by name', function() {
    home.search('noWidgetNoPagesAndNoFragmentHasANameLikeThat');
    expect(home.getListedPageNames()).toEqual([]);
    expect(home.getListedWidgetNames()).toEqual([]);
    expect(home.getFavoritePageNames()).toEqual([]);
    expect(home.getFavoriteWidgetNames()).toEqual([]);

    home.search('so');   // 'so' is contained by 'PerSOn' and 'aweSOmeWidget'
    expect(home.getFavoritePageNames()).toEqual(['Person']);
    expect(home.getListedWidgetNames()).toEqual(['awesomeWidget']);
  });

  it('should mark a page as favorite', function() {
    $('#empty .Artifact-button-favorite').click();

    expect(home.getListedPageNames()).toEqual([]);
    expect(home.getFavoritePageNames()).toEqual(['Person', 'empty']);

    $('#empty .Artifact-button-favorite').click();

    expect(home.getListedPageNames()).toEqual(['empty']);
    expect(home.getFavoritePageNames()).toEqual(['Person']);
  });
});
