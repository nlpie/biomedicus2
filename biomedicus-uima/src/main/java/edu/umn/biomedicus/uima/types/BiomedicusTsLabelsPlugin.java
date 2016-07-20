/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.types;

import com.google.inject.Module;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.semantics.*;
import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.syntax.PartsOfSpeech;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.plugins.AbstractPlugin;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapterFactory;
import edu.umn.biomedicus.uima.labels.LabelableModule;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

import java.util.Arrays;
import java.util.Collection;

public final class BiomedicusTsLabelsPlugin extends AbstractPlugin {

    @Override
    public Collection<? extends Module> modules() {
        return Arrays.asList(
                new LabelableModule<Section>(Section.class) {
                    @Override
                    protected LabelAdapterFactory<Section> createFactory() {
                        return SectionLabelAdapter::create;
                    }
                },
                new LabelableModule<TextSegment>(TextSegment.class) {
                    @Override
                    protected LabelAdapterFactory<TextSegment> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.type.TextSegmentAnnotation");
                            return new AbstractLabelAdapter<TextSegment>(cas, type) {
                                @Override
                                protected TextSegment createLabelValue(FeatureStructure featureStructure) {
                                    return new TextSegment();
                                }
                            };
                        };
                    }
                },
                new LabelableModule<Sentence>(Sentence.class) {
                    @Override
                    protected LabelAdapterFactory<Sentence> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Sentence");
                            return new AbstractLabelAdapter<Sentence>(cas, type) {
                                @Override
                                protected Sentence createLabelValue(FeatureStructure featureStructure) {
                                    return new Sentence();
                                }
                            };
                        };
                    }
                },
                new LabelableModule<DependencyParse>(DependencyParse.class) {
                    @Override
                    protected LabelAdapterFactory<DependencyParse> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.DependencyParse");
                            Feature parseTreeFeature = type.getFeatureByBaseName("parseTree");

                            return new AbstractLabelAdapter<DependencyParse>(cas, type) {
                                @Override
                                protected void fillAnnotation(Label<DependencyParse> label, AnnotationFS annotationFS) {
                                    annotationFS.setStringValue(parseTreeFeature, label.value().parseTree());
                                }

                                @Override
                                protected DependencyParse createLabelValue(FeatureStructure featureStructure) {
                                    return new DependencyParse(featureStructure.getStringValue(parseTreeFeature));
                                }
                            };
                        };
                    }
                },
                new LabelableModule<DictionaryTerm>(DictionaryTerm.class) {
                    @Override
                    protected LabelAdapterFactory<DictionaryTerm> createFactory() {
                        return DictionaryTermLabelAdapter::create;
                    }
                },
                new LabelableModule<Negated>(Negated.class) {
                    @Override
                    protected LabelAdapterFactory<Negated> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.Negated");
                            return new AbstractLabelAdapter<Negated>(cas, type) {
                                @Override
                                protected Negated createLabelValue(FeatureStructure featureStructure) {
                                    return new Negated();
                                }
                            };
                        };
                    }
                },
                new LabelableModule<Historical>(Historical.class) {
                    @Override
                    protected LabelAdapterFactory<Historical> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.Historical");
                            return new AbstractLabelAdapter<Historical>(cas, type) {
                                @Override
                                protected Historical createLabelValue(FeatureStructure featureStructure) {
                                    return new Historical();
                                }
                            };
                        };
                    }
                },
                new LabelableModule<Probable>(Probable.class) {
                    @Override
                    protected LabelAdapterFactory<Probable> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.Probable");
                            return new AbstractLabelAdapter<Probable>(cas, type) {
                                @Override
                                protected Probable createLabelValue(FeatureStructure featureStructure) {
                                    return new Probable();
                                }
                            };
                        };
                    }
                },
                new LabelableModule<TermToken>(TermToken.class) {
                    @Override
                    protected LabelAdapterFactory<TermToken> createFactory() {
                        return new AbstractTokenLabelAdapterFactory<TermToken>() {
                            @Override
                            protected String getTypeName() {
                                return "edu.umn.biomedicus.uima.type1_6.TermToken";
                            }

                            @Override
                            protected TermToken createToken(String text, boolean hasSpaceAfter) {
                                return new TermToken(text, hasSpaceAfter);
                            }
                        };
                    }
                },
                new LabelableModule<Acronym>(Acronym.class) {
                    @Override
                    protected LabelAdapterFactory<Acronym> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.Acronym");
                            return new AbstractLabelAdapter<Acronym>(cas, type) {
                                @Override
                                protected Acronym createLabelValue(FeatureStructure featureStructure) {
                                    return new Acronym();
                                }
                            };
                        };
                    }
                },
                new LabelableModule<AcronymExpansion>(AcronymExpansion.class) {
                    @Override
                    protected LabelAdapterFactory<AcronymExpansion> createFactory() {
                        return new AbstractTokenLabelAdapterFactory<AcronymExpansion>() {
                            @Override
                            protected String getTypeName() {
                                return "edu.umn.biomedicus.uima.type1_6.AcronymExpansion";
                            }

                            @Override
                            protected AcronymExpansion createToken(String text, boolean hasSpaceAfter) {
                                return new AcronymExpansion(text, hasSpaceAfter);
                            }
                        };
                    }
                },
                new LabelableModule<ParseToken>(ParseToken.class) {
                    @Override
                    protected LabelAdapterFactory<ParseToken> createFactory() {
                        return new AbstractTokenLabelAdapterFactory<ParseToken>() {
                            @Override
                            protected String getTypeName() {
                                return "edu.umn.biomedicus.uima.type1_6.ParseToken";
                            }

                            @Override
                            protected ParseToken createToken(String text, boolean hasSpaceAfter) {
                                return new ParseToken(text, hasSpaceAfter);
                            }
                        };
                    }
                },
                new LabelableModule<PartOfSpeech>(PartOfSpeech.class) {
                    @Override
                    protected LabelAdapterFactory<PartOfSpeech> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.PartOfSpeechTag");
                            Feature partOfSpeechFeature = type.getFeatureByBaseName("partOfSpeech");
                            return new AbstractLabelAdapter<PartOfSpeech>(cas, type) {
                                @Override
                                protected void fillAnnotation(Label<PartOfSpeech> label, AnnotationFS annotationFS) {
                                    annotationFS.setStringValue(partOfSpeechFeature, label.value().toString());
                                }

                                @Override
                                protected PartOfSpeech createLabelValue(FeatureStructure featureStructure) {
                                    return PartsOfSpeech.forTag(featureStructure.getStringValue(partOfSpeechFeature));
                                }
                            };
                        };
                    }
                },
                new LabelableModule<WordIndex>(WordIndex.class) {
                    @Override
                    protected LabelAdapterFactory<WordIndex> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.WordIndex");
                            Feature indexFeature = type.getFeatureByBaseName("index");
                            return new AbstractLabelAdapter<WordIndex>(cas, type) {
                                @Override
                                protected void fillAnnotation(Label<WordIndex> label, AnnotationFS annotationFS) {
                                    annotationFS.setIntValue(indexFeature, label.value().term().indexedTerm());
                                }

                                @Override
                                protected WordIndex createLabelValue(FeatureStructure featureStructure) {
                                    IndexedTerm indexedTerm = new IndexedTerm(featureStructure.getIntValue(indexFeature));
                                    return new WordIndex(indexedTerm);
                                }
                            };
                        };
                    }
                },
                new LabelableModule<NormForm>(NormForm.class) {
                    @Override
                    protected LabelAdapterFactory<NormForm> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.NormForm");
                            Feature normFormFeature = type.getFeatureByBaseName("normForm");
                            return new AbstractLabelAdapter<NormForm>(cas, type) {
                                @Override
                                protected void fillAnnotation(Label<NormForm> label, AnnotationFS annotationFS) {
                                    annotationFS.setStringValue(normFormFeature, label.value().normalForm());
                                }

                                @Override
                                protected NormForm createLabelValue(FeatureStructure featureStructure) {
                                    return new NormForm(featureStructure.getStringValue(normFormFeature));
                                }
                            };
                        };
                    }
                },
                new LabelableModule<NormIndex>(NormIndex.class) {
                    @Override
                    protected LabelAdapterFactory<NormIndex> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.NormIndex");
                            Feature indexFeature = type.getFeatureByBaseName("index");
                            return new AbstractLabelAdapter<NormIndex>(cas, type) {
                                @Override
                                protected void fillAnnotation(Label<NormIndex> label, AnnotationFS annotationFS) {
                                    annotationFS.setIntValue(indexFeature, label.value().term().indexedTerm());
                                }

                                @Override
                                protected NormIndex createLabelValue(FeatureStructure featureStructure) {
                                    IndexedTerm indexedTerm = new IndexedTerm(featureStructure.getIntValue(indexFeature));
                                    return new NormIndex(indexedTerm);
                                }
                            };
                        };
                    }
                },
                new LabelableModule<Misspelling>(Misspelling.class) {
                    @Override
                    protected LabelAdapterFactory<Misspelling> createFactory() {
                        return cas -> {
                            Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Misspelling");
                            return new AbstractLabelAdapter<Misspelling>(cas, type) {
                                @Override
                                protected Misspelling createLabelValue(FeatureStructure featureStructure) {
                                    return new Misspelling();
                                }
                            };
                        };
                    }
                },
                new LabelableModule<SpellCorrection>(SpellCorrection.class) {
                    @Override
                    protected LabelAdapterFactory<SpellCorrection> createFactory() {
                        return new AbstractTokenLabelAdapterFactory<SpellCorrection>() {
                            @Override
                            protected String getTypeName() {
                                return "edu.umn.biomedicus.uima.type1_6.SpellCorrection";
                            }

                            @Override
                            protected SpellCorrection createToken(String text, boolean hasSpaceAfter) {
                                return new SpellCorrection(text, hasSpaceAfter);
                            }
                        };
                    }
                }
        );
    }
}
