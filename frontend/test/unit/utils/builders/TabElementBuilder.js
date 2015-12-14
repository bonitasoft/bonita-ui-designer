class TabElementBuilder {

  constructor() {
    this.container = {
      rows: []
    };
    this.triggerRemoved = jasmine.createSpy('triggerRemoved');
  }

  static aTab() {
    return new TabElementBuilder();
  }

  withRow(row) {
    this.container.rows.push(row);
    return this;
  }

  title(title) {
    this.title = title;
    return this;
  }
}

export default TabElementBuilder.aTab;