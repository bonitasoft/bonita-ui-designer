import { Directive, Input, OnInit, ViewContainerRef, TemplateRef } from '@angular/core';
import propertiesValues from '../../assets/propertiesValues';
import { uidModel } from './uid-model-directive';
import { BindingContextFactory, BindingFactory, EnumBinding, VariableContext } from '@bonitasoft/ui-designer-context-binding';

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
        this.scope.collection = collection;
    }

    @Input('properties-valuesIndex') set setIndex(index: number) {
        this.scope.index = index;
    }

    @Input('properties-valuesItem') set setItem(item: any) {
        this.scope.item = item;
    }

    @Input('properties-valuesCount') set setCount(count: number) {
        this.scope.count = count;
    }

    private properties: any = {};
    private context: VariableContext = {};
    private model: uidModel;
    private scope: any = {};

    private bindingContextFactory: BindingContextFactory;

    constructor(private templateRef: TemplateRef<any>,
                private viewContainerRef: ViewContainerRef, private uidModel: uidModel) {
        this.model = uidModel;
        this.bindingContextFactory = new BindingContextFactory();
    }

    async ngOnInit(): Promise<void> {
        const propertyValues = propertiesValues.get(this.reference);

        let variablesAccessors = new Map(this.model.getVariableAccessors());

        BindingFactory.createPropertiesBinding(
            propertyValues,
            this.bindingContextFactory.addSpecificKeywordOnAccessors(variablesAccessors, this.scope),
            this.properties);

        this.properties.isBound = function (propertyName: string) {
            return !!propertyValues[propertyName] &&
                propertyValues[propertyName].type === EnumBinding.Variable &&
                !!propertyValues[propertyName].value;
        }

        this.context = {
            $implicit: this.properties,
        };
        this.viewContainerRef.createEmbeddedView(this.templateRef, this.context);
    }
}
