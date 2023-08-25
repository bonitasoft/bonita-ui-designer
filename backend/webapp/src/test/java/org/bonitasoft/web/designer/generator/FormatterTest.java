package org.bonitasoft.web.designer.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.generator.Formatter.toDisplayName;

public class FormatterTest {

    @Test
    public void firstName() {
        var name = "firstName";

        var displayName = toDisplayName(name);

        assertThat(displayName).isEqualTo("First Name");
    }
    @Test
    public void isValid() {
        var name = "isValid";

        var displayName = toDisplayName(name);

        assertThat(displayName).isEqualTo("Is Valid");
    }

    @Test
    public void creationLocalDateTime() {
        var name = "creationLocalDateTime";

        var displayName = toDisplayName(name);

        assertThat(displayName).isEqualTo("Creation Local Date Time");
    }
    @Test
    public void name() {
        var name = "name";

        var displayName = toDisplayName(name);

        assertThat(displayName).isEqualTo("Name");
    }
    @Test
    public void name2() {
        var name = "name2";

        var displayName = toDisplayName(name);

        assertThat(displayName).isEqualTo("Name2");
    }

    @Test
    public void name_null() {
        String name = null;

        var displayName = toDisplayName(name);

        //TODO: check if this is OK for user ?
        assertThat(displayName).isEmpty();
    }

    @Test
    public void my_point_name() {
        var name = "my.name";

        var displayName = toDisplayName(name);

        assertThat(displayName).isEqualTo("My.name");
    }

    @Test
    public void my_underscore_name() {
        var name = "my_name";

        var displayName = toDisplayName(name);

        assertThat(displayName).isEqualTo("My_name");
    }
}
