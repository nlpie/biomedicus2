/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.concepts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.umn.biomedicus.concepts.UmlsSemanticTypeGroup.*;

/**
 * Represents a semantic type in the UMLS Metathesaurus.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public enum UmlsSemanticType {
    // Activities & Behaviors
    ACTIVITY("T052", "Activity", ACTIVITIES_AND_BEHAVIORS),

    BEHAVIOR("T053", "Behavior", ACTIVITIES_AND_BEHAVIORS),

    DAILY_OR_RECREATIONAL_ACTIVITY("T056", "Daily or Recreational Activity", ACTIVITIES_AND_BEHAVIORS),

    EVENT("T051", "Event", ACTIVITIES_AND_BEHAVIORS),

    GOVERNMENT_OR_REGULATORY_ACTIVITY("T064", "Governmental or Regulatory Activity", ACTIVITIES_AND_BEHAVIORS),

    INDIVIDUAL_BEHAVIOR("T055", "Individual Behavior", ACTIVITIES_AND_BEHAVIORS),

    MACHINE_ACTIVITY("T066", "Machine Activity", ACTIVITIES_AND_BEHAVIORS),

    OCCUPATIONAL_ACTIVITY("T057", "Occupational Activity", ACTIVITIES_AND_BEHAVIORS),

    SOCIAL_BEHAVIOR("T054", "Social Behavior", ACTIVITIES_AND_BEHAVIORS),

    // Anatomy
    ANATOMICAL_STRUCTURE("T017", "Anatomical Structure", ANATOMY),

    BODY_LOCATION_OR_REGION("T029", "Body Location or Region", ANATOMY),

    BODY_PART_ORGAN_OR_ORGAN_COMPONENT("T023", "Body Part, Organ, or Organ Component", ANATOMY),

    BODY_SPACE_OR_JUNCTION("T030", "Body Space or Junction", ANATOMY),

    BODY_SUBSTANCE("T031", "Body Substance", ANATOMY),

    BODY_SYSTEM("T022", "Body System", ANATOMY),

    CELL("T025", "Cell", ANATOMY),

    CELL_COMPONENT("T026", "Cell Component", ANATOMY),

    EMBRYONIC_STRUCTURE("T018", "Embryonic Structure", ANATOMY),

    FULLY_FORMED_ANATOMICAL_STRUCTURE("T021", "Fully Formed Anatomical Structure", ANATOMY),

    TISSUE("T024", "Tissue", ANATOMY),

    // Chemicals & Drugs
    AMINO_ACID_PEPTIDE_OR_PROTEIN("T116", "Amino Acid, Peptide, or Protein", CHEMICALS_AND_DRUGS),

    ANTIBIOTIC("T195", "Antibiotic", CHEMICALS_AND_DRUGS),

    BIOLOGICALLY_ACTIVE_SUBSTANCE("T123", "Biologically Active Substance", CHEMICALS_AND_DRUGS),

    BIOMEDICAL_OR_DENTAL_MATERIAL("T122", "Biomedical or Dental Material", CHEMICALS_AND_DRUGS),

    CARBOHYDRATE("T118", "Carbohydrate", CHEMICALS_AND_DRUGS),

    CHEMICAL("T103", "Chemical", CHEMICALS_AND_DRUGS),

    CHEMICAL_VIEWED_FUNCTIONALLY("T120", "Chemical Viewed Functionally", CHEMICALS_AND_DRUGS),

    CHEMICAL_VIEWED_STRUCTURALLY("T104", "Chemical Viewed Structurally", CHEMICALS_AND_DRUGS),

    CLINICAL_DRUG("T200", "Clinical Drug", CHEMICALS_AND_DRUGS),

    EICOSANOID("T111", "Eicosanoid", CHEMICALS_AND_DRUGS),

    ELEMENT_ION_OR_ISOTOPE("T196", "Element, Ion, or Isotope", CHEMICALS_AND_DRUGS),

    ENZYME("T126", "Enzyme", CHEMICALS_AND_DRUGS),

    HAZARDOUS_OR_POISONOUS_SUBSTANCE("T131", "Hazardous or Poisonous Substance", CHEMICALS_AND_DRUGS),

    HORMONE("T125", "Hormone", CHEMICALS_AND_DRUGS),

    IMMUNOLOGIC_FACTOR("T129", "Immunologic Factor", CHEMICALS_AND_DRUGS),

    INDICATOR_REAGENT_OR_DIAGNOSTIC_AID("T130", "Indicator, Reagent, or Diagnostic Aid", CHEMICALS_AND_DRUGS),

    INORGANIC_CHEMICAL("T197", "Inorganic Chemical", CHEMICALS_AND_DRUGS),

    LIPID("T119", "Lipid", CHEMICALS_AND_DRUGS),

    NEUROREACTIVE_SUBSTANCE_OR_BIOGENIC_AMINE("T124", "Neuroreactive Substance or Biogenic Amine", CHEMICALS_AND_DRUGS),

    NUCLEIC_ACID_NUCLEOSIDE_OR_NUCLEOTIDE("T114", "Nucleic Acid, Nucleoside, or Nucleotide", CHEMICALS_AND_DRUGS),

    ORGANIC_CHEMICAL("T109", "Organic Chemical", CHEMICALS_AND_DRUGS),

    ORGANOPHOSPHORUS_COMPOUND("T115", "Organophosphorus Compound", CHEMICALS_AND_DRUGS),

    PHARMACOLOGIC_SUBSTANCE("T121", "Pharmacologic Substance", CHEMICALS_AND_DRUGS),

    RECEPTOR("T192", "Receptor", CHEMICALS_AND_DRUGS),

    STEROID("T110", "Steroid", CHEMICALS_AND_DRUGS),

    VITAMIN("T127", "Vitamin", CHEMICALS_AND_DRUGS),

    // Concepts & Ideas
    CLASSIFICATION("T185", "Classification", CONCEPTS_AND_IDEAS),

    CONCEPTUAL_ENTITY("T077", "Conceptual Entity", CONCEPTS_AND_IDEAS),

    FUNCTIONAL_CONCEPT("T169", "Functional Concept", CONCEPTS_AND_IDEAS),

    GROUP_ATTRIBUTE("T102", "Group Attribute", CONCEPTS_AND_IDEAS),

    IDEA_OR_CONCEPT("T078", "Idea or Concept", CONCEPTS_AND_IDEAS),

    INTELLECTUAL_PRODUCT("T170", "Intellectual Product", CONCEPTS_AND_IDEAS),

    LANGUAGE("T171", "Language", CONCEPTS_AND_IDEAS),

    QUALITATIVE_CONCEPT("T080", "Qualitative Concept", CONCEPTS_AND_IDEAS),

    QUANTITATIVE_CONCEPT("T081", "Quantitative Concept", CONCEPTS_AND_IDEAS),

    REGULATION_OR_LAW("T089", "Regulation or Law", CONCEPTS_AND_IDEAS),

    SPATIAL_CONCEPT("T082", "Spatial Concept", CONCEPTS_AND_IDEAS),

    TEMPORAL_CONCEPT("T079", "Temporal Concept", CONCEPTS_AND_IDEAS),

    // Devices
    DRUG_DELIVERY_DEVICE("T203", "Drug Delivery Device", DEVICES),

    MEDICAL_DEVICE("T074", "Medical Device", DEVICES),

    RESEARCH_DEVICE("T075", "Research Device", DEVICES),

    // Disorders
    ACQUIRED_ABNORMALITY("T020", "Acquired Abnormality", DISORDERS),

    ANATOMICAL_ABNORMALITY("T190", "Anatomical Abnormality", DISORDERS),

    CELL_OR_MOLECULAR_DYSFUNCTION("T049", "Cell or Molecular Dysfunction", DISORDERS),

    CONGENITAL_ABNORMALITY("T019", "Congenital Abnormality", DISORDERS),

    DISEASE_OR_SYNDROME("T047", "Disease or Syndrome", DISORDERS),

    EXPERIMENTAL_MODEL_OF_DISEASE("T050", "Experimental Model of Disease", DISORDERS),

    FINDING("T033", "Finding", DISORDERS),

    INJURY_OR_POISONING("T037", "Injury or Poisoning", DISORDERS),

    MENTAL_OR_BEHAVIORAL_DYSFUNCTION("T048", "Mental or Behavioral Dysfunction", DISORDERS),

    NEOPLASTIC_PROCESS("T191", "Neoplastic Process", DISORDERS),

    PATHOLOGIC_FUNCTION("T046", "Pathologic Function", DISORDERS),

    SIGN_OR_SYMPTOM("T184", "Sign or Symptom", DISORDERS),

    // Genes & Molecular Sequences
    AMINO_ACID_SEQUENCE("T087", "Amino Acid Sequence", GENES_AND_MOLECULAR_SEQUENCES),

    CARBOHYDRATE_SEQUENCE("T088", "Carbohydrate Sequence", GENES_AND_MOLECULAR_SEQUENCES),

    GENE_OR_GENOME("T028", "Gene or Genome", GENES_AND_MOLECULAR_SEQUENCES),

    MOLECULAR_SEQUENCE("T085", "Molecular Sequence", GENES_AND_MOLECULAR_SEQUENCES),

    NUCLEOTIDE_SEQUENCE("T086", "Nucleotide Sequence", GENES_AND_MOLECULAR_SEQUENCES),

    // Geographic Areas
    GEOGRAPHIC_AREA("T083", "Geographic Area", GEOGRAPHIC_AREAS),

    // Living Beings
    AGE_GROUP("T100", "Age Group", LIVING_BEINGS),

    AMPHIBIAN("T011", "Amphibian", LIVING_BEINGS),

    ANIMAL("T008", "Animal", LIVING_BEINGS),

    ARCHAEON("T194", "Archaeon", LIVING_BEINGS),

    BACTERIUM("T007", "Bacterium", LIVING_BEINGS),

    BIRD("T012", "Bird", LIVING_BEINGS),

    EUKARYOTE("T204", "Eukaryote", LIVING_BEINGS),

    FAMILY_GROUP("T099", "Family Group", LIVING_BEINGS),

    FISH("T013", "Fish", LIVING_BEINGS),

    FUNGUS("T004", "Fungus", LIVING_BEINGS),

    GROUP("T096", "Group", LIVING_BEINGS),

    HUMAN("T016", "Human", LIVING_BEINGS),

    MAMMAL("T015", "Mammal", LIVING_BEINGS),

    ORGANISM("T001", "Organism", LIVING_BEINGS),

    PATIENT_OR_DISABLED_GROUP("T101", "Patient or Disabled Group", LIVING_BEINGS),

    PLANT("T002", "Plant", LIVING_BEINGS),

    POPULATION_GROUP("T098", "Population Group", LIVING_BEINGS),

    PROFESSIONAL_OR_OCCUPATIONAL_GROUP("T097", "Professional or Occupational Group", LIVING_BEINGS),

    REPTILE("T014", "Reptile", LIVING_BEINGS),

    VERTEBRATE("T010", "Vertebrate", LIVING_BEINGS),

    VIRUS("T005", "Virus", LIVING_BEINGS),

    // Objects
    ENTITY("T071", "Entity", OBJECTS),

    FOOD("T168", "Food", OBJECTS),

    MANUFACTURED_OBJECT("T073", "Manufactured Object", OBJECTS),

    PHYSICAL_OBJECT("T072", "Physical Object", OBJECTS),

    SUBSTANCE("T167", "Substance", OBJECTS),

    // Occupations
    BIOMEDICAL_OCCUPATION_OR_DISCIPLINE("T091", "Biomedical Occupation or Discipline", OCCUPATIONS),

    OCCUPATION_OR_DISCIPLINE("T090", "Occupation or Discipline", OCCUPATIONS),

    // Organizations
    HEALTH_CARE_RELATED_ORGANIZATION("T093", "Health Care Related Organization", ORGANIZATIONS),

    ORGANIZATION("T092", "Organization", ORGANIZATIONS),

    PROFESSIONAL_SOCIETY("T094", "Professional Society", ORGANIZATIONS),

    SELF_HELP_OR_RELIEF_ORGANIZATION("T095", "Self-help or Relief Organization", ORGANIZATIONS),

    // Phenomena
    BIOLOGIC_FUNCTION("T038", "Biologic Function", PHENOMENA),

    ENVIRONMENTAL_EFFECT_OF_HUMANS("T069", "Environmental Effect of Humans", PHENOMENA),

    HUMAN_CAUSED_PHENOMENON_OR_PROCESS("T068", "Human-caused Phenomenon or Process", PHENOMENA),

    LABORATORY_OR_TEST_RESULT("T034", "Laboratory or Test Result", PHENOMENA),

    NATURAL_PHENOMENON_OR_PROCESS("T070", "Natural Phenomenon or Process", PHENOMENA),

    PHENOMENON_OR_PROCESS("T067", "Phenomenon or Process", PHENOMENA),

    // Physiology
    CELL_FUNCTION("T043", "Cell Function", PHYSIOLOGY),

    CLINICAL_ATTRIBUTE("T201", "Clinical Attribute", PHYSIOLOGY),

    GENETIC_FUNCTION("T045", "Genetic Function", PHYSIOLOGY),

    MENTAL_PROCESS("T041", "Mental Process", PHYSIOLOGY),

    MOLECULAR_FUNCTION("T044", "Molecular Function", PHYSIOLOGY),

    ORGANISM_ATTRIBUTE("T032", "Organism Attribute", PHYSIOLOGY),

    ORGANISM_FUNCTION("T040", "Organism Function", PHYSIOLOGY),

    ORGAN_OR_TISSUE_FUNCTION("T042", "Organ or Tissue Function", PHYSIOLOGY),

    PHYSIOLOGIC_FUNCTION("T039", "Physiologic Function", PHYSIOLOGY),

    // Procedures
    DIAGNOSTIC_PROCEDURE("T060", "Diagnostic Procedure", PROCEDURES),

    EDUCATIONAL_ACTIVITY("T065", "Educational Activity", PROCEDURES),

    HEALTH_CARE_ACTIVITY("T058", "Health Care Activity", PROCEDURES),

    LABORATORY_PROCEDURE("T059", "Laboratory Procedure", PROCEDURES),

    MOLECULAR_BIOLOGY_RESEARCH_TECHNIQUE("T063", "Molecular Biology Research Technique", PROCEDURES),

    RESEARCH_ACTIVITY("T062", "Research Activity", PROCEDURES),

    THERAPEUTIC_OR_PREVENTIVE_PROCEDURE("T061", "Therapeutic or Preventive Procedure", PROCEDURES);

    private static Map<String, UmlsSemanticType> tuiMap = Collections.unmodifiableMap(
            Arrays.stream(UmlsSemanticType.values())
                    .collect(Collectors.toMap(UmlsSemanticType::tui, Function.identity()))
    );

    public static UmlsSemanticType forTui(String tui) {
        return tuiMap.get(tui);
    }

    private final String tui;
    private final String value;
    private final UmlsSemanticTypeGroup group;

    UmlsSemanticType(String tui, String value, UmlsSemanticTypeGroup group) {
        this.tui = tui;
        this.value = value;
        this.group = group;
    }

    public String tui() {
        return tui;
    }

    public UmlsSemanticTypeGroup getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return value;
    }
}
