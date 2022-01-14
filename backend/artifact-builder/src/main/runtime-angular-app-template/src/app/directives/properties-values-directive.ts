import { Directive, Input, OnInit, ViewContainerRef, TemplateRef } from '@angular/core';
import propertiesValues from '../../assets/propertiesValues';
import { uidModel } from './uid-model-directive';
import { BindingContextFactory, BindingFactory, EnumBinding, ModelFactory } from '@bonitasoft/ui-designer-context-binding';

/**
 * Allow us to declare a directive to setup a structural directive like following
 * ex:
 * <div *reference="'uuid'; let properties;"></div>
 */
@Directive({
    selector: '[properties-values]'
})
export class PropertiesValues implements OnInit {
    private collection: Array<any> | undefined;
    @Input('properties-values') reference: string = '';

    @Input('properties-valuesCollection') set setCollection(collection: Array<any>) {
        this._context.collection = collection;
    }

    @Input('properties-valuesIndex') set setIndex(index: number) {
        this._context.index = index;
    }

    @Input('properties-valuesItem') set setItem(item: any) {
        this._context.item = item;
    }

    @Input('properties-valuesCount') set setCount(count: number) {
        this._context.count = count;
    }

    private properties: any = {};
    private _context: any = {};

    private bindingContextFactory: BindingContextFactory;
    private modelFactory: ModelFactory;

    constructor(private templateRef: TemplateRef<any>,
                private viewContainerRef: ViewContainerRef, private _uidModelDirective: uidModel) {
        this.modelFactory = this._uidModelDirective.getModelFactory();
        this.bindingContextFactory = new BindingContextFactory(this.modelFactory);
    }

    async ngOnInit(): Promise<void> {
        const propertyValues = propertiesValues.get(this.reference);

        BindingFactory.createPropertiesBinding(
            propertyValues,
            this.bindingContextFactory.addSpecificKeywordOnAccessors(this._context),
            this.properties);

        this.properties.isBound = function (propertyName: string) {
            return !!propertyValues[propertyName] &&
                propertyValues[propertyName].type === EnumBinding.Variable &&
                !!propertyValues[propertyName].value;
        }
        this.viewContainerRef.createEmbeddedView(this.templateRef, { $implicit: this.properties });
    }
}
