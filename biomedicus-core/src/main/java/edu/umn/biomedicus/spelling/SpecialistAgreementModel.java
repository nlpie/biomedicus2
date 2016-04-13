package edu.umn.biomedicus.spelling;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@ProvidedBy(SpecialistAgreementModel.Loader.class)
public class SpecialistAgreementModel {
    private final Map<String, Collection<String>> baseToCanonicalForm;

    private SpecialistAgreementModel(Map<String, Collection<String>> baseToCanonicalForm) {
        this.baseToCanonicalForm = baseToCanonicalForm;
    }

    public Collection<String> canonicalFormForBase(String base) {
        return baseToCanonicalForm.get(base);
    }

    public Collection<String> getCanonicalFormForBase(String base) {
        return baseToCanonicalForm.get(base);
    }

    @Singleton
    static class Loader extends DataLoader<SpecialistAgreementModel> {
        private final Path specialistAgreementModelPath;

        @Inject
        public Loader(@Setting("specialist.path") Path specialistPath) {
            this.specialistAgreementModelPath = specialistPath.resolve("LRAGR");
        }

        @Override
        protected SpecialistAgreementModel loadModel() throws BiomedicusException {
            try {
                Map<String, Collection<String>> baseToCanonicalForm = Files.lines(specialistAgreementModelPath)
                        .map(Pattern.compile("\\|")::split)
                        .map(columns -> {
                            String base = columns[4];
                            String canonical = columns[5];
                            return new AbstractMap.SimpleImmutableEntry<>(base, Collections.singleton(canonical));
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> {
                            TreeSet<String> forms = new TreeSet<>();
                            forms.addAll(first);
                            forms.addAll(second);
                            return new ArrayList<>(forms);
                        }));
                return new SpecialistAgreementModel(baseToCanonicalForm);
            } catch (IOException e) {
                throw new BiomedicusException(e);
            }
        }
    }
}
