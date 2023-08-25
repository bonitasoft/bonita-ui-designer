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

import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.generator.parametrizedWidget.InputTypeResolver.InputType.*;

public class AttributeInputTypeResolverTest {

    private InputTypeResolver inputTypeResolver;

    @BeforeEach
    public void init() {
        inputTypeResolver = new InputTypeResolver();
    }

    @Test
    public void should_detect_text_input() {
        ContractInput input = new LeafContractInput("input", String.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(TEXT);
    }

    @Test
    public void should_detect_numeric_input() {
        ContractInput input = new LeafContractInput("input", Integer.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(NUMERIC);
    }

    @Test
    public void should_detect_local_date_input() {
        ContractInput input = new LeafContractInput("input", LocalDate.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(LOCAL_DATE);
        input = new LeafContractInput("input", Date.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(LOCAL_DATE);
    }

    @Test
    public void should_detect_date_time_input() {
        ContractInput input = new LeafContractInput("input", LocalDateTime.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(LOCAL_DATE_TIME);
    }

    @Test
    public void should_detect_offset_date_time_input() {
        ContractInput input = new LeafContractInput("input", OffsetDateTime.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(OFFSET_DATE_TIME);
    }

    @Test
    public void should_detect_boolean_input() {
        ContractInput input = new LeafContractInput("input", Boolean.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(BOOLEAN);
    }

    @Test
    public void should_detect_file_input() {
        ContractInput input = new LeafContractInput("input", File.class);
        assertThat(inputTypeResolver.getContractInputType(input)).isEqualTo(FILE);
    }

    @Test
    public void should_detect_date_input() {
        ContractInput input = new LeafContractInput("input", LocalDate.class);
        assertThat(inputTypeResolver.isDateInput(input)).isTrue();

        input = new LeafContractInput("input", Date.class);
        assertThat(inputTypeResolver.isDateInput(input)).isTrue();

        input = new LeafContractInput("input", LocalDateTime.class);
        assertThat(inputTypeResolver.isDateInput(input)).isTrue();

        input = new LeafContractInput("input", OffsetDateTime.class);
        assertThat(inputTypeResolver.isDateInput(input)).isTrue();

        input = new LeafContractInput("input", Boolean.class);
        assertThat(inputTypeResolver.isDateInput(input)).isFalse();
    }

}
