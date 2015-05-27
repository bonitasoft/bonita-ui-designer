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
package org.bonitasoft.web.designer.model.asset;


import static junitparams.JUnitParamsRunner.$;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AssetTest {

    private BeanValidator beanValidator = new DesignerConfig().beanValidator();

    private Asset asset;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {
        asset = anAsset().withScope(AssetScope.PAGE).build();

    }

    /**
     * AssetNames injected in the test {@link #should_be_valid_when_name_is_valid(String)}. A name can be a filename or an URL
     */
    protected Object[] validNames() {
        return $(
                $("test.js"),
                $("jquery-1.11.3.min.js"),
                $("jquery-1.11.3.min-SNAPSHOT.js"),
                $("myimage_test.jpg"),
                $("myimage_test.PNG"),
                $("https://code.jquery.com/jquery-2.1.4.min.js"),
                $("http://code.jquery.com/jquery-2.1.4.min.js"),
                $("https://code.jquery.com/jquery-2.1.4.min.JS"),
                $("https://code.jquery.com/jquery version 2.1.4.min.js"),
                $("https://code.jquery.com/jquery2.1.4.min.js")
        );
    }

    /**
     * AssetNames injected in the test {@link #should_be_invalid_when_name_is_invalid(String, String)}
     * <ul>
     * <li>The  value is the asset name (example jquery-1.11.3.min.js)</li>
     * <li>Second value is the expected error message</li>
     * </ul>
     */
    protected Object[] invalidNames() {
        String errorMessage = "Asset name should be a filename containing only alphanumeric characters and no space or an external URL";
        return $(
                //Not null
                $(null, "Asset name should not be blank"),
                //No space in name
                $("my name.js", errorMessage),
                //No special characters
                $("myé&name.js", errorMessage),
                //URL without protocole
                $("code.jquery.com/jquery-2.1.4.min.js", errorMessage),
                //absolute path
                $("./jquery-2.1.4.min.JS", errorMessage)
        );
    }

    @Test
    @Parameters(method = "validNames")
    public void should_be_valid_when_name_is_valid(String name) {
        asset.setName(name);
        beanValidator.validate(asset);
    }

    @Test
    @Parameters(method = "invalidNames")
    public void should_be_invalid_when_name_is_invalid(String name, String expectedErrorMessage) {
        exception.expect(ConstraintValidationException.class);
        exception.expectMessage(expectedErrorMessage);
        asset.setName(name);

        beanValidator.validate(asset);
    }

    @Test
    public void should_be_invalid_when_type_null() {
        exception.expect(ConstraintValidationException.class);
        exception.expectMessage("Asset type may not be null");
        asset.setType(null);

        beanValidator.validate(asset);
    }

}