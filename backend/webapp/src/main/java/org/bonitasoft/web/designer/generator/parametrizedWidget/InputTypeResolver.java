/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.generator.parametrizedWidget;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;

import org.bonitasoft.web.designer.generator.mapping.ContractInputDataHandler;
import org.bonitasoft.web.designer.model.contract.ContractInput;

public class InputTypeResolver {

    public static enum InputType {
        TEXT, NUMERIC, LOCAL_DATE, LOCAL_DATE_TIME, OFFSET_DATE_TIME, BOOLEAN, FILE;
    }

    public InputType getContractInputType(ContractInput input) {
        if (aTextInput(input)) {
            return InputType.TEXT;
        } else if (aNumericInput(input)) {
            return InputType.NUMERIC;
        } else if (aLocalDateInput(input) || aDateInput(input)) {
            return InputType.LOCAL_DATE;
        } else if (aLocalDateTimeInput(input)) {
            return InputType.LOCAL_DATE_TIME;
        } else if (aOffsetDateTimeInput(input)) {
            return InputType.OFFSET_DATE_TIME;
        } else if (aBooleanInput(input)) {
            return InputType.BOOLEAN;
        } else if (aFileInput(input)) {
            return InputType.FILE;
        }
        throw new IllegalArgumentException(String.format("The type of the contract input %s is unknown", input.getName()));
    }

    public boolean isSupported(ContractInput input) {
        return !ContractInputDataHandler.shouldGenerateWidgetForInput(input)
                && (aTextInput(input)
                        || aNumericInput(input)
                        || aDateInput(input)
                        || aLocalDateInput(input)
                        || aLocalDateTimeInput(input)
                        || aOffsetDateTimeInput(input)
                        || aBooleanInput(input)
                        || aFileInput(input));
    }

    public boolean isDateInput(ContractInput input) {
        return aLocalDateInput(input) || aDateInput(input) || aLocalDateTimeInput(input) || aOffsetDateTimeInput(input);
    }

    /**
     * @deprecated Type Date is deprecated in studio, prefer use type LocalDate.
     */
    @Deprecated
    private boolean aDateInput(ContractInput input) {
        return Date.class.getName().equals(input.getType());
    }

    private boolean aLocalDateInput(ContractInput input) {
        return LocalDate.class.getName().equals(input.getType());
    }

    private boolean aLocalDateTimeInput(ContractInput input) {
        return LocalDateTime.class.getName().equals(input.getType());
    }

    private boolean aOffsetDateTimeInput(ContractInput input) {
        return OffsetDateTime.class.getName().equals(input.getType());
    }

    private boolean aNumericInput(ContractInput input) {
        try {
            Class<?> clazz = Class.forName(input.getType());
            return Number.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean aTextInput(ContractInput input) {
        return String.class.getName().equals(input.getType());
    }

    private boolean aBooleanInput(ContractInput input) {
        return input.getType() != null && input.getType().toLowerCase(Locale.ENGLISH).endsWith("boolean");
    }

    private boolean aFileInput(ContractInput input) {
        return input.getType() != null && input.getType().equals(File.class.getName());
    }

}
