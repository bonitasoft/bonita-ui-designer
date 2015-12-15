export default class Home {

  static get() {
    browser.get('#/');
    return new Home();
  }

  getListedPageNames() {
    return $$('home-pages .Artifact-info').map((element) => element.getText());
  }

  getListedWidgetNames() {
    return $$('home-widgets .Artifact-info').map((element) => element.getText());
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

  createPage(name){
    $('.HomeCreate').click();
    $('.modal-body input[name="name"]').sendKeys(name);
    $('.modal-footer button[type="submit"]').click();
  }
}
