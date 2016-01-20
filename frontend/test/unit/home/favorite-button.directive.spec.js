describe('favorite button', function() {

  var $scope, element, artifact, controller, pageRepo;

  beforeEach(angular.mock.module('bonitasoft.designer.home'));

  beforeEach(inject(function($compile, $rootScope, _pageRepo_) {
    $scope = $rootScope.$new();
    artifact = {
      type: 'page',
      id: 'pageId'
    };
    pageRepo = _pageRepo_;
    $scope.repository = pageRepo;
    $scope.artifact = artifact;
    element = $compile('<favorite-button artifact-repository="repository" artifact="artifact"></favorite-button>')($scope);
    $scope.$apply();
    controller = element.controller('favoriteButton');
  }));

  describe('controller', function() {

    it('should mark an artifact as favorite', function() {
      spyOn(pageRepo, 'markAsFavorite');
      artifact.favorite = false;

      controller.toggleFavorite();

      expect(artifact.favorite).toBeTruthy();
      expect(pageRepo.markAsFavorite).toHaveBeenCalledWith(artifact.id);
    });

    it('should unmark an artifact as favorite', function() {
      spyOn(pageRepo, 'unmarkAsFavorite');
      artifact.favorite = true;

      controller.toggleFavorite();

      expect(artifact.favorite).toBeFalsy();
      expect(pageRepo.unmarkAsFavorite).toHaveBeenCalledWith(artifact.id);
    });

    it('should say if artifact is favorite or not', function() {
      artifact.favorite = true;
      expect(controller.isFavorite()).toBeTruthy();

      artifact.favorite = false;
      expect(controller.isFavorite()).toBeFalsy();
    });
  });

  describe('directive', function() {

    it('should toggle favorite state while clicking on it', function() {
      spyOn(controller, 'toggleFavorite');

      element.find('.Artifact-favoriteButton').click();

      expect(controller.toggleFavorite).toHaveBeenCalled();
    });

    it('should have a special class name when artifact is marked as favorite', function() {
      artifact.favorite = true;
      $scope.$apply();
      expect(element.find('.Artifact-favoriteButton--checked').length).toBe(1);

      artifact.favorite = false;
      $scope.$apply();
      expect(element.find('.Artifact-favoriteButton--checked').length).toBe(0);
    });
  });
});
