package edu.umn.biomedicus.concepts;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class TUI {
    public static final Pattern TUI_PATTERN = Pattern.compile("T([\\d]{3})");

    private final int identifier;

    public TUI(int identifier) {
        this.identifier = identifier;
    }

    public TUI(String wordForm) {
        Matcher matcher = TUI_PATTERN.matcher(wordForm);
        if (matcher.find()) {
            String identifier = matcher.group(1);
            this.identifier = Integer.parseInt(identifier);
        } else {
            throw new IllegalArgumentException("Word form does not match TUI pattern");
        }
    }

    public int identifier() {
        return identifier;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TUI tui = (TUI) o;

        return identifier == tui.identifier;

    }

    @Override
    public int hashCode() {
        return identifier;
    }

    @Override
    public String toString() {
        return String.format("T%03d", identifier);
    }
}
