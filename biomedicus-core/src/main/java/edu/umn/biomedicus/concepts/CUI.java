package edu.umn.biomedicus.concepts;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CUI {
    public static final Pattern CUI_PATTERN = Pattern.compile("C([\\d]{7})");

    private final int identifier;

    public CUI(int identifier) {
        this.identifier = identifier;
    }

    public CUI(String wordForm) {
        Matcher matcher = CUI_PATTERN.matcher(wordForm);
        if (matcher.find()) {
            String identifier = matcher.group(1);
            this.identifier = Integer.parseInt(identifier);
        } else {
            throw new IllegalArgumentException("Word form does not match CUI pattern");
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CUI cui = (CUI) o;

        return identifier == cui.identifier;
    }

    @Override
    public int hashCode() {
        return identifier;
    }

    @Override
    public String toString() {
        return String.format("C%07d", identifier);
    }
}
