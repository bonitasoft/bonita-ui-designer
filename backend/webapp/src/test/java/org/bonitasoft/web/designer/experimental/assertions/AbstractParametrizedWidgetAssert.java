/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.experimental.assertions;

import static java.lang.String.format;

import org.assertj.core.api.AbstractAssert;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.AbstractParametrizedWidget;

/**
 * {@link AbstractParametrizedWidget} specific assertions - Generated by CustomAssertionGenerator.
 */
public class AbstractParametrizedWidgetAssert extends AbstractAssert<AbstractParametrizedWidgetAssert, AbstractParametrizedWidget> {

    /**
     * Creates a new </code>{@link AbstractParametrizedWidgetAssert}</code> to make assertions on actual AbstractParametrizedWidget.
     *
     * @param actual the AbstractParametrizedWidget we want to make assertions on.
     */
    public AbstractParametrizedWidgetAssert(AbstractParametrizedWidget actual) {
        super(actual, AbstractParametrizedWidgetAssert.class);
    }

    /**
     * An entry point for AbstractParametrizedWidgetAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one's can write directly : <code>assertThat(myAbstractParametrizedWidget)</code> and get specific assertion with code completion.
     *
     * @param actual the AbstractParametrizedWidget we want to make assertions on.
     * @return a new </code>{@link AbstractParametrizedWidgetAssert}</code>
     */
    public static AbstractParametrizedWidgetAssert assertThat(AbstractParametrizedWidget actual) {
        return new AbstractParametrizedWidgetAssert(actual);
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget's alignment is equal to the given one.
     *
     * @param alignment the given alignment to compare the actual AbstractParametrizedWidget's alignment to.
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget's alignment is not equal to the given one.
     */
    public AbstractParametrizedWidgetAssert hasAlignment(String alignment) {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("\nExpected <%s> alignment to be:\n  <%s>\n but was:\n  <%s>", actual, alignment, actual.getAlignment());

        // check
        if (!actual.getAlignment().equals(alignment)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget's cssClasses is equal to the given one.
     *
     * @param cssClasses the given cssClasses to compare the actual AbstractParametrizedWidget's cssClasses to.
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget's cssClasses is not equal to the given one.
     */
    public AbstractParametrizedWidgetAssert hasCssClasses(String cssClasses) {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("\nExpected <%s> cssClasses to be:\n  <%s>\n but was:\n  <%s>", actual, cssClasses, actual.getCssClasses());

        // check
        if (!actual.getCssClasses().equals(cssClasses)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget is isDisplayed.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget is not isDisplayed.
     */
    public AbstractParametrizedWidgetAssert isDisplayed() {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("Expected actual AbstractParametrizedWidget to be isDisplayed but was not.", actual);

        // check
        if (!actual.getIsDisplayed())
            throw new AssertionError(errorMessage);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget is not isDisplayed.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget is isDisplayed.
     */
    public AbstractParametrizedWidgetAssert isNotDisplayed() {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("Expected actual AbstractParametrizedWidget not to be isDisplayed but was.", actual);

        // check
        if (actual.getIsDisplayed())
            throw new AssertionError(errorMessage);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget's label is equal to the given one.
     *
     * @param label the given label to compare the actual AbstractParametrizedWidget's label to.
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget's label is not equal to the given one.
     */
    public AbstractParametrizedWidgetAssert hasLabel(String label) {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("\nExpected <%s> label to be:\n  <%s>\n but was:\n  <%s>", actual, label, actual.getLabel());

        // check
        if (!actual.getLabel().equals(label)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget is labelHidden.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget is not labelHidden.
     */
    public AbstractParametrizedWidgetAssert isLabelHidden() {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("Expected actual AbstractParametrizedWidget to be labelHidden but was not.", actual);

        // check
        if (!actual.isLabelHidden())
            throw new AssertionError(errorMessage);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget is not labelHidden.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget is labelHidden.
     */
    public AbstractParametrizedWidgetAssert isNotLabelHidden() {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("Expected actual AbstractParametrizedWidget not to be labelHidden but was.", actual);

        // check
        if (actual.isLabelHidden())
            throw new AssertionError(errorMessage);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget is readonly.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget is not readonly.
     */
    public AbstractParametrizedWidgetAssert isReadonly() {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("Expected actual AbstractParametrizedWidget to be readonly but was not.", actual);

        // check
        if (!actual.isReadonly())
            throw new AssertionError(errorMessage);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget is not readonly.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget is readonly.
     */
    public AbstractParametrizedWidgetAssert isNotReadonly() {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("Expected actual AbstractParametrizedWidget not to be readonly but was.", actual);

        // check
        if (actual.isReadonly())
            throw new AssertionError(errorMessage);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual AbstractParametrizedWidget's widgetId is equal to the given one.
     *
     * @param widgetId the given widgetId to compare the actual AbstractParametrizedWidget's widgetId to.
     * @return this assertion object.
     * @throws AssertionError - if the actual AbstractParametrizedWidget's widgetId is not equal to the given one.
     */
    public AbstractParametrizedWidgetAssert hasWidgetId(String widgetId) {
        // check that actual AbstractParametrizedWidget we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        String errorMessage = format("\nExpected <%s> widgetId to be:\n  <%s>\n but was:\n  <%s>", actual, widgetId, actual.getWidgetId());

        // check
        if (!actual.getWidgetId().equals(widgetId)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

}
