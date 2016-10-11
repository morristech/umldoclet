/*
 * Copyright (C) 2016 Talsma ICT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nl.talsmasoftware.umldoclet.rendering;

import nl.talsmasoftware.umldoclet.rendering.indent.IndentingPrintWriter;

/**
 * @author Sjoerd Talsma
 * @deprecated This idea is not quite ready for production code yet.
 */
public class SeparatorRenderer extends ParentAwareRenderer {

    protected final String separator;

    protected SeparatorRenderer(Renderer parent, String separator) {
        super(parent);
        this.separator = separator;
    }

    @Override
    protected IndentingPrintWriter writeTo(IndentingPrintWriter output) {
//        return output.append(separator).newline();
        return output; // think about conditionals later on!
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

}