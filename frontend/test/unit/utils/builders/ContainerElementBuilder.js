export default class ContainerElementBuilder {

  constructor() {
    this.rows = [];
  }

  static aContainer() {
    return new ContainerElementBuilder();
  }

  withRow(row) {
    this.rows.push(row);
    return this;
  }
}
