angular.module('pb.controllers').controller('PaletteCtrl', function ($scope, paletteService) {

  /**
   * The palette contains all the widgets that can be added to the page.
   */
  $scope.sections = paletteService.getSections();
});

