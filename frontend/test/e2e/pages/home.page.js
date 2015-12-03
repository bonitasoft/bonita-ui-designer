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
}
