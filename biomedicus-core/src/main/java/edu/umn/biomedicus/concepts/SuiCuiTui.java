package edu.umn.biomedicus.concepts;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class SuiCuiTui {
    private static final Pattern PATTERN = Pattern.compile("S([\\d]{7})C([\\d]{7})T([\\d]{3})");

    private final SUI sui;

    private final CUI cui;

    private final TUI tui;

    public SuiCuiTui(SUI sui, CUI cui, TUI tui) {
        this.sui = sui;
        this.cui = cui;
        this.tui = tui;
    }

    public static SuiCuiTui fromString(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.find()) {
            SUI sui = new SUI(Integer.parseInt(matcher.group(1)));
            CUI cui = new CUI(Integer.parseInt(matcher.group(2)));
            TUI tui = new TUI(Integer.parseInt(matcher.group(3)));

            return new SuiCuiTui(sui, cui, tui);
        } else {
            throw new IllegalArgumentException("String does not match SuiCuiTui");
        }
    }

    public SUI sui() {
        return sui;
    }

    public CUI cui() {
        return cui;
    }

    public TUI tui() {
        return tui;
    }

    public UmlsConcept toConcept(double confidence) {
        return new UmlsConcept(cui, tui, sui, confidence);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuiCuiTui suiCuiTui = (SuiCuiTui) o;

        if (!sui.equals(suiCuiTui.sui)) return false;
        if (!cui.equals(suiCuiTui.cui)) return false;
        return tui.equals(suiCuiTui.tui);

    }

    @Override
    public int hashCode() {
        int result = sui.hashCode();
        result = 31 * result + cui.hashCode();
        result = 31 * result + tui.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return sui.toString() + cui.toString() + tui.toString();
    }
}
