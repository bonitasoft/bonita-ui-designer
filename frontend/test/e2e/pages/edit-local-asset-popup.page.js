(() => {
  'use strict';

  class EditLocalAssetPopUp {
    get fileContent() {
      return element(by.css('.EditAssetPopUp .ace_content')).getText();
    }

    set fileContent(content) {
      element(by.css('.EditAssetPopUp .ace_text-input')).clear().sendKeys(content);
    }

    save() {
      element(by.cssContainingText('.EditAssetPopUp .modal-footer button', 'Update')).click();
    }

    cancel() {
      element(by.cssContainingText('.modal-footer button', 'Cancel')).click();
    }

    isOpen() {
      return element(by.css('.EditAssetPopUp')).isPresent();
    }
  }

  module.exports = EditLocalAssetPopUp;

})();
