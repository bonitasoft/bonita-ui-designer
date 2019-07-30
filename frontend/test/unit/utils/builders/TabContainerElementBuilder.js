class TabContainerElementBuilder {

  constructor() {
    this.container = {
      rows: []
    };
    this.triggerRemoved = jasmine.createSpy('triggerRemoved');
  }

  static aTabContainer() {
    return new TabContainerElementBuilder();
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

export default TabContainerElementBuilder.aTabContainer;
