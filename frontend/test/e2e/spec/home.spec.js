import { default as HomePage } from '../pages/home.page';

describe('home page', function() {

  var home;
  const PAGE_NAMES = [ 'Person', 'empty' ];
  const WIDGET_NAMES = [ 'awesomeWidget', 'favoriteWidget' ];

  beforeEach(function() {
    home = HomePage.get();
  });

  it('should list artifacts ordered by last update date descendant', function() {
    expect(home.getListedPageNames()).toEqual(PAGE_NAMES);
    expect(home.getListedWidgetNames()).toEqual(WIDGET_NAMES);
    expect(home.getListedFormNames()).toEqual(['emptyForm']);
    expect(home.getListedLayoutNames()).toEqual(['emptyLayout']);
  });

  it('should list favorite artifacts ordered by last update date descendant', function() {
    expect(home.getFavoritePageNames()).toEqual(['Person']);
    expect(home.getFavoriteWidgetNames()).toEqual(['favoriteWidget']);
  });

  it('should navigate to a page', function() {
    $$('.ArtifactList-page a').first().click();

    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
    $('.EditorHeader-icon').click();
    expect($('.HomeHeader-title').getText()).toBe('UI Designer');
  });

  it('should navigate to a widget', function() {
    $$('.ArtifactList-widget a').first().click();

    expect($('.EditorHeader-brand').getText()).toBe('CUSTOM WIDGET EDITOR');
  });

  it('should create a layout', function() {
    home.createLayout('testLayout');
    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
    browser.get('#/');
    home.openTab('layout');
    $$('.ArtifactList-layout a').first().click();
    $$('.BottomPanel-toggle').last().click();

    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');

    $('#save').click();
    $$('.EditorHeader-back').click();
    expect($('.HomeHeader-title').getText()).toBe('UI Designer');
  });

  it('should create a form', function() {
    home.createForm('testForm');
    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');
    browser.get('#/');
    home.openTab('form');
    $$('.ArtifactList-form a').first().click();
    $$('.BottomPanel-toggle').last().click();

    expect($('.EditorHeader-brand').getText()).toBe('PAGE EDITOR');

    $('#save').click();
    $('.EditorHeader-icon').click();
    expect($('.HomeHeader-title').getText()).toBe('UI Designer');
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
    expect($('.NewArtifact .tooltip-inner').isDisplayed()).toBeTruthy();

    nameInput.clear();
    nameInput.sendKeys('page-name');

    expect(createPageButton.isEnabled()).toBeFalsy();
    expect($('.NewArtifact .tooltip-inner').isDisplayed()).toBeTruthy();

    nameInput.clear();
    nameInput.sendKeys('pageName');

    expect(createPageButton.isEnabled()).toBeTruthy();
    expect($$('.NewArtifact .tooltip-inner').count()).toBe(0);
  });

  it('should create a widget', function() {
    home.createWidget('test');
    expect($('.EditorHeader-brand').getText()).toBe('CUSTOM WIDGET EDITOR');
  });

  it('should forbid to create a widget with an already existing name', function() {
    $('.HomeCreate').click();
    element(by.css('#type-widget')).click();
    $('.modal-body input[name="name"]').sendKeys('awesomeWidget');
    expect($('.modal-footer button[type="submit"]').isEnabled()).toBeFalsy();
    expect($('.tooltip-inner').getText()).toEqual('A custom widget with this name already exists');
  });

  it('should open a modal to confirm page deletion', function() {
    //We want to delete a page
    $$('#person-page .Artifact-delete').first().click();
    //A modal is opened with a confirmation message
    expect($('#confirm-delete-popup .modal-body').getText()).toBe('Are you sure you want to delete the page Person?');
  });

  // deactivated since it fails randomly on our CI
  xit('should not delete page if user cancels deletion', function() {
    var numberOfPages = element.all(by.repeater('page in pages')).count();
    //We want to delete a page
    $$('#person-page .Artifact-delete').first().click();
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
    var btn = $$('#person-page .Artifact-export').first();
    var iframe = $$('.ExportArtifact').first();
    btn.click();

    expect(iframe.getAttribute('src')).toMatch(/\/export\/page\/person/);
  });

  it('should open a modal to confirm widget deletion', function() {
    home.openTab('widget');

    expect($('#customAwesomeWidget .Artifact-icon identicon').isPresent()).toBeTruthy();
    //We want to delete a widget
    $$('#customAwesomeWidget .Artifact-delete').first().click();
    //A modal is opened with a confirmation message
    expect($('#confirm-delete-popup .modal-body').getText()).toBe('Are you sure you want to delete the widget awesomeWidget?');
  });

  it('should rename a page', function() {

    var btnRenamePage = $$('#person-page .Artifact-rename').first();
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
    $$('#person-page .Artifact-rename').first().click();

    //The link should now be a visible input with the page name
    $('#page-name-input-0').clear();
    $('#page-name-input-0').sendKeys('page name');

    expect($('.ArtifactList-page form[name="renameArtifact"] .tooltip-inner').isDisplayed()).toBeTruthy();

    $('#page-name-input-0').clear();
    $('#page-name-input-0').sendKeys('page-name');

    expect($('.ArtifactList-page form[name="renameArtifact"] .tooltip-inner').isDisplayed()).toBeTruthy();

    $('#page-name-input-0').clear();
    $('#page-name-input-0').sendKeys('pageName');

    expect($$('.ArtifactList-page form[name="renameArtifact"] .tooltip-inner').count()).toBe(0);
  });

  it('should remove the input to rename a page on blur', function() {

    $$('#person-page .Artifact-rename').first().click();

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
    $$('#person-page .Artifact-rename').first().click();
    var input = $('#page-name-input-0');
    expect(input.getAttribute('id')).toEqual(browser.driver.switchTo().activeElement().getAttribute('id'));
  });

  it('should open help popup',  function() {
    $('.btn-bonita-help').click();

    expect($('.modal-header .modal-title').getText()).toBe('Help');
  });

  it('should filter widgets, pages and fragment by name', function() {
    expect(home.getTabCounter('page')).toEqual('2');
    expect(home.getTabCounter('widget')).toEqual('2');
    expect(home.getTabCounter('layout')).toEqual('1');
    expect(home.getTabCounter('form')).toEqual('1');
    home.search('noWidgetNoPagesAndNoFragmentHasANameLikeThat');
    expect(home.getListedPageNames()).toEqual([]);
    expect(home.getListedWidgetNames()).toEqual([]);
    expect(home.getListedLayoutNames()).toEqual([]);
    expect(home.getListedFormNames()).toEqual([]);
    expect(home.getFavoritePageNames()).toEqual([]);
    expect(home.getFavoriteWidgetNames()).toEqual([]);
    expect(home.getTabCounter('page')).toEqual('0');
    expect(home.getTabCounter('widget')).toEqual('0');
    expect(home.getTabCounter('layout')).toEqual('0');
    expect(home.getTabCounter('form')).toEqual('0');

    home.search('so');   // 'so' is contained by 'PerSOn' and 'aweSOmeWidget'
    expect(home.getFavoritePageNames()).toEqual(['Person']);
    expect(home.getListedWidgetNames()).toEqual(['awesomeWidget']);
    expect(home.getTabCounter('page')).toEqual('1');
    expect(home.getTabCounter('widget')).toEqual('1');
    expect(home.getTabCounter('layout')).toEqual('0');
    expect(home.getTabCounter('form')).toEqual('0');

  });

  it('should mark a page as favorite', function() {
    $$('#empty .Artifact-favoriteButton').first().click();
    expect(home.getFavoritePageNames()).toEqual(['Person', 'empty']);

    $$('#empty .Artifact-favoriteButton').first().click();
    expect(home.getFavoritePageNames()).toEqual(['Person']);
  });
});
