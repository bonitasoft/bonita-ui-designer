package org.bonitasoft.web.designer.generator;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.capitalize;

public class Formatter {

    public static final String BY_UPPER_CASE_LETTER = "(?=\\p{Upper})";
    public static final String TO_REPLACE = "((?<=[a-z])(?=[A-Z]))|((?<=[A-Z])(?=[A-Z][a-z]))";

    public static String toDisplayName(String name) {
        String displayName = "";
        if (name != null) {
            var words = name.split(BY_UPPER_CASE_LETTER);
            displayName = capitalize(join(" ", words))
                    .replaceAll(TO_REPLACE, " ");
        }
        return displayName;
    }
}
