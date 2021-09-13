import { Directive, Input, OnInit, ViewContainerRef, TemplateRef } from '@angular/core';
import propertiesValues from '../../assets/propertiesValues';
import { uidModel } from './uid-model-directive';
import { BindingFactory, EnumBinding, VariableContext } from '@bonitasoft/ui-designer-context-binding';

/**
 * Allow us to declare a directive to setup a structural directive like following
 * ex:
 * <div *reference="'uuid'; let properties;"></div>
 */
@Directive({
    selector: '[properties-values]'
})
export class PropertiesValues implements OnInit {
    @Input('properties-values') reference: string = '';

    // Properties input
    @Input('properties') properties: any = {};

    private context: VariableContext = {};
    private model: uidModel;

    constructor(private templateRef: TemplateRef<any>,
        private viewContainerRef: ViewContainerRef, private uidModel: uidModel) {
        this.model = uidModel;
    }
    ngOnInit(): void {
        const propertyValues = propertiesValues.get(this.reference);

        BindingFactory.createPropertiesBinding(
            propertyValues,
            this.model.getVariableAccessors(),
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
