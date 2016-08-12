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

package edu.umn.biomedicus.common.text;

public final class Section {
    private final String sectionTitle;
    private final int contentStart;
    private final int level;
    private final boolean hasSubsections;
    private final String kind;

    private Section(String sectionTitle, int contentStart, int level, boolean hasSubsections, String kind) {
        this.sectionTitle = sectionTitle;
        this.contentStart = contentStart;
        this.level = level;
        this.hasSubsections = hasSubsections;
        this.kind = kind;
    }

    /**
     * Returns the title of the section.
     *
     * @return section title.
     */
    public String getSectionTitle() {
        return sectionTitle;
    }

    /**
     * Returns the content start of the section.
     *
     * @return content start.
     */
    public int contentStart() {
        return contentStart;
    }

    /**
     * Returns the level of the section, ex. 0 for a section 1 for a subsection.
     *
     * @return section level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns whether this section has subsections.
     *
     * @return
     */
    public boolean hasSubsections() {
        return hasSubsections;
    }

    public String getKind() {
        return kind;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sectionTitle;
        private int contentStart;
        private int level;
        private boolean hasSubsections;
        private String kind;

        public Builder setSectionTitle(String sectionTitle) {
            this.sectionTitle = sectionTitle;
            return this;
        }

        public Builder setContentStart(int contentStart) {
            this.contentStart = contentStart;
            return this;
        }

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder setHasSubsections(boolean hasSubsections) {
            this.hasSubsections = hasSubsections;
            return this;
        }

        public Builder setKind(String kind) {
            this.kind = kind;
            return this;
        }

        public Section build() {
            return new Section(sectionTitle, contentStart, level, hasSubsections, kind);
        }
    }
}
