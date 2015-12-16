export default class Home {

  static get() {
    browser.get('#/');
    return new Home();
  }

  getListedPageNames() {
    return $$('.ArtifactList-page').map((element) => element.getText());
  }

  getListedWidgetNames() {
    return $$('.ArtifactList-widget').map((element) => element.getText());
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
}
