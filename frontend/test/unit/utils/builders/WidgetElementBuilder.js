class WidgetElementBuilder {

  constructor() {
    this.triggerRemoved = jasmine.createSpy('triggerRemoved');
    this.triggerAdded = jasmine.createSpy('triggerAdded');
    this.dimension = { xs: 12 };
  }

  static aWidget() {
    return new WidgetElementBuilder();
  }

  id(id) {
    this.id = id;
    return this;
  }

  withParentContainerRow(containerRow) {
    this.$$parentContainerRow = containerRow;
    return this;
  }
}

export default WidgetElementBuilder.aWidget;