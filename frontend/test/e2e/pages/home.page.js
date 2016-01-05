export default class Home {

  static get() {
    browser.get('#/');
    return new Home();
  }

  getListedPageNames() {
    this.openTab('page');
    return $$('.Artifact-section .ArtifactList-page .Artifact-name').map((element) => element.getText());
  }

  getFavoritePageNames() {
    return $$('.Favorite-section .ArtifactList-page .Artifact-name').map((element) => element.getText());
  }

  getListedWidgetNames() {
    this.openTab('widget');
    return $$('.Artifact-section .ArtifactList-widget .Artifact-name').map((element) => element.getText());
  }

  getFavoriteWidgetNames() {
    return $$('.Favorite-section .ArtifactList-widget .Artifact-name').map((element) => element.getText());
  }

  getListedFormNames() {
    this.openTab('form');
    return $$('.Artifact-section .ArtifactList-form .Artifact-name').map((element) => element.getText());
  }

  getListedLayoutNames() {
    this.openTab('layout');
    return $$('.Artifact-section .ArtifactList-layout .Artifact-name').map((element) => element.getText());
  }

  getTabCounter(type) {
    return $(`.tab-${type} .badge`).getText();
  }

  search(term) {
    $('.Search-input').clear().sendKeys(term);
  }

  filterFavorites() {
    $('input[name="favorites"]').click();
  }

  unfilterFavorites() {
    $('input[name="all"]').click();
  }

  createWidget(name){
    $('.HomeCreate').click();
    $('.modal-body input[name="name"]').sendKeys(name);
    element(by.css('#type-widget')).click();
    $('.modal-footer button[type="submit"]').click();
  }

  createForm(name){
    $('.HomeCreate').click();
    $('.modal-body input[name="name"]').sendKeys(name);
    element(by.css('#type-form')).click();
    $('.modal-footer button[type="submit"]').click();
  }

  createLayout(name){
    $('.HomeCreate').click();
    $('.modal-body input[name="name"]').sendKeys(name);
    element(by.css('#type-layout')).click();
    $('.modal-footer button[type="submit"]').click();
  }

  createPage(name){
    $('.HomeCreate').click();
    $('.modal-body input[name="name"]').sendKeys(name);
    $('.modal-footer button[type="submit"]').click();
  }

  openTab(type) {
    $(`.tab-${type}`).click();
  }
}
