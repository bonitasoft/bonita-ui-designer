describe('Migration service', function() {
  var migration, alerts, $localStorage;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services', 'ui.bootstrap.modal'));

  beforeEach(inject(function ($injector) {
    $localStorage = $injector.get('$localStorage');
    alerts = $injector.get('alerts');
    migration = $injector.get('migration');
  }));

  it('should do nothing when page is not migrable', function() {
    // when we load resolution
    spyOn(migration, 'migrationConfirm');

    expect(function() {
      migration.handleMigrationStatus('myPage', {compatible: true, migration: false});
    }).not.toThrow();
    expect(migration.migrationConfirm).not.toHaveBeenCalled();
  });

  it('should throw an error when artifact is not compatible', function() {
    // when we load resolution
    spyOn(migration, 'migrationConfirm');

    expect(function() {
      migration.handleMigrationStatus('myPage', {compatible: false, migration: false});
    }).toThrow({message: 'myPage is not compatible with this UI Designer version. A newer version is required.'});
    expect(migration.migrationConfirm).not.toHaveBeenCalled();
  });

  it('should call migration confirmation when page is migrable', function () {
    // when we load resolution
    spyOn(migration, 'migrationConfirm');

    expect(function() {
      migration.handleMigrationStatus('myPage', {compatible: true, migration: true});
    }).not.toThrow();
    expect(migration.migrationConfirm).toHaveBeenCalled();
  });

  it('should not call migration confirmation when page is migrable and notShowMeAgain is checked', function () {
    // when we load resolution
    spyOn(migration, 'migrationConfirm');
    $localStorage.bonitaUIDesigner = {
      doNotShowMigrationMessageAgain: true
    };

    expect(function() {
      migration.handleMigrationStatus('myPage', {compatible: true, migration: true});
    }).not.toThrow();
    expect(migration.migrationConfirm).not.toHaveBeenCalled();
  });

  it('should clear lastReport when migrationStatus is called', function () {
    // when we load resolution
    migration.lastReport = {'status': 'warning'};
    spyOn(migration, 'migrationConfirm');

    expect(function () {
      migration.handleMigrationStatus('myPage', {compatible: true, migration: false});
    }).not.toThrow();
    expect(migration.migrationConfirm).not.toHaveBeenCalled();
    expect(migration.lastReport).toEqual({});
  });

  it('should fill lastReport when migration is on success', function () {
    let migrationReport = {'status': 'success'};
    spyOn(alerts, 'addInfo');

    expect(function() {
      migration.handleMigrationNotif('myPage', migrationReport);
    }).not.toThrow();
    expect(alerts.addInfo).toHaveBeenCalled();
    expect(migration.lastReport).toEqual(migrationReport);
  });

  it('should throw an error when migration is on error', function () {
    let migrationReport = {'status': 'error'};
    spyOn(alerts, 'addInfo');

    expect(function() {
      migration.handleMigrationNotif('myPage', migrationReport);
    }).toThrow();
    expect(alerts.addInfo).not.toHaveBeenCalled();
    expect(migration.lastReport).toEqual(migrationReport);
  });
});
