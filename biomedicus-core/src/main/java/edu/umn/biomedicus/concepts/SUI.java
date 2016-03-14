package edu.umn.biomedicus.concepts;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class SUI {
    public static final Pattern SUI_PATTERN = Pattern.compile("S([\\d]{7})");

    private final int identifier;

    public SUI(int identifier) {
        this.identifier = identifier;
    }

    public SUI(String wordForm) {
        Matcher matcher = SUI_PATTERN.matcher(wordForm);
        if (matcher.find()) {
            String identifier = matcher.group(1);
            this.identifier = Integer.parseInt(identifier);
        } else {
            throw new IllegalArgumentException("Word form does not match SUI pattern");
        }
    }

    public int identifier() {
        return identifier;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SUI sui = (SUI) o;

        return identifier == sui.identifier;
    }

    @Override
    public int hashCode() {
        return identifier;
    }

    @Override
    public String toString() {
        return String.format("S%07d", identifier);
    }
}
