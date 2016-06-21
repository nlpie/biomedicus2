/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima;

import edu.umn.biomedicus.uima.common.Views;
import edu.umn.biomedicus.uima.type1_5.DocumentId;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;

import java.io.IOException;

/**
 *
 */
public class TestCollectionReader extends CollectionReader_ImplBase {
    private static final String document = "REASON FOR CONSULT:  Evaluation of alcohol withdrawal and dependance as well as evaluation of anxiety.\n" +
            "\n" +
            "HISTORY OF PRESENT ILLNESS:  This is a 50-year-old male who was transferred from Sugar Land ER to ABCD Hospital for admission to the MICU for acute alcohol withdrawal.  The patient had been on a drinking binge for the past 12 days prior to admission and had not been eating.  He reported that he called 911 secondary to noticing bilious vomiting and dry heave.  The patient has been drinking for the past 25 years and has noted it to be a problem for at least the past 3 years.  He has been away from work secondary to alcohol cravings and drinking.  He has also experienced marital and family conflict as a result of his drinking habit.  On average, the patient drinks 5 to 8 glasses or cups of vodka or rum per day, and on the weekend, he tends to drink more heavily.  He reports a history of withdrawal symptoms, but denied history of withdrawal seizures.  His longest period of sobriety was one year, and this was due to the assistance of attending AA meetings.  The patient reports problems with severe insomnia, more so late insomnia and low self esteem as a result of feeling guilty about what he has done to his family due to his drinking habit.  He reports anxiety that is mostly related to concern about his wife's illness and fear of his wife leaving him secondary to his drinking habits.  He denies depressive symptoms.  He denies any psychotic symptoms or perceptual disturbances.  There are no active symptoms of withdrawal at this time.\n" +
            "\n" +
            "PAST PSYCHIATRIC HISTORY:  There are no previous psychiatric hospitalizations or evaluations.  The patient denies any history of suicidal attempts.  There is no history of inpatient rehabilitation programs.  He has attended AA for periodic moments throughout the past few years.  He has been treated with Antabuse before.\n" +
            "\n" +
            "PAST MEDICAL HISTORY:  The patient has esophagitis, hypertension, and fatty liver (recently diagnosed).\n" +
            "\n" +
            "MEDICATIONS:  His outpatient medications include Lotrel 30 mg p.o. q.a.m. and Restoril 30 mg p.o. q.h.s.\n" +
            "\n" +
            "ALLERGIES:  No known drug allergies.\n" +
            "\n" +
            "FAMILY HISTORY:  Distant relatives with alcohol dependance.  No other psychiatric illnesses in the family.\n" +
            "\n" +
            "SOCIAL HISTORY:  The patient has been divorced twice.  He has two daughters one from each marriage, ages 15 and 22.  He works as a geologist at Petrogas.  He has limited contact with his children.  He reports that his children's mothers have turned them against him.  He and his wife have experienced marital discord secondary to his alcohol use.  His wife is concerned that he may loose his job because he has skipped work before without reporting to his boss.  There are no other illicit drugs except alcohol that the patient reports.\n" +
            "\n" +
            "PHYSICAL EXAMINATION:  VITAL SIGNS:  Temperature 98, pulse 89, and respiratory rate 20, and blood pressure is 129/83.\n" +
            "\n" +
            "MENTAL STATUS EXAMINATION:  This is a well-groomed male.  He appears his stated age.  He is lying comfortably in bed.  There are no signs of emotional distress.  He is pleasant and engaging.  There are no psychomotor abnormalities.  No signs of tremulousness.  His speech is with normal rate, volume, and inflection.  Mood is reportedly okay.  Affect euthymic.  Thought content, no suicidal or homicidal ideations.  No delusions.  Thought perception, there are no auditory or visual hallucinations.  Thought process, Logical and goal directed.  Insight and judgment are fair.  The patient knows he needs to stop drinking and knows the hazardous effects that drinking will have on his body.\n" +
            "\n" +
            "LABORATORY DATA:  CBC:  WBC 5.77, H&H 14 and 39.4 respectively, and platelets 102,000.  BMP:  Sodium 140, potassium 3, chloride 104, bicarbonate 26, BUN 13, creatinine 0.9, glucose 117, calcium 9.5, magnesium 2.1, phosphorus 2.9, PT 13.4, and INR 1.0.  LFTs:  ALT 64, AST 69, direct bilirubin 0.5, total bilirubin 1.3, protein 5.8, and albumin 4.2.  PFTs within normal limits.\n" +
            "\n" +
            "IMAGING:  CAT scan of the abdomen and pelvis reveals esophagitis and fatty liver.  No splenomegaly.\n" +
            "\n" +
            "ASSESSMENT:  This is a 50-year-old male with longstanding history of alcohol dependence admitted secondary to alcohol withdrawal found to have derangement in liver function tests and a fatty liver.  The patient currently has no signs of withdrawal.  The patient's anxiety is likely secondary to situation surrounding his wife and their marital discord and the effect of chronic alcohol use.  The patient had severe insomnia that is likely secondary to alcohol use.  Currently, there are no signs of primary anxiety disorder in this patient.\n" +
            "\n" +
            "DIAGNOSES:  Axis I:  Alcohol dependence.\n" +
            "Axis II:  Deferred.\n" +
            "Axis III:  Fatty liver, esophagitis, and hypertension.\n" +
            "Axis IV:  Marital discord, estranged from children.\n" +
            "Axis V:  Global assessment of functioning equals 55.\n" +
            "\n";

    private int returned = 0;

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException {
        CAS systemCas = aCAS.createView(Views.SYSTEM_VIEW);
        JCas systemView;
        try {
            systemView = systemCas.getJCas();
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        systemView.setDocumentText(document);

        DocumentId documentId = new DocumentId(systemView);
        documentId.setDocumentId("1");
        documentId.addToIndexes();

        returned++;
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return returned != 1;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }

    @Override
    public void close() throws IOException {

    }
}
