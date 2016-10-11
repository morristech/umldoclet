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

import com.sun.javadoc.ProgramElementDoc;
import nl.talsmasoftware.umldoclet.rendering.indent.IndentingPrintWriter;

import static java.util.Objects.requireNonNull;

/**
 * @author Sjoerd Talsma
 */
public class DependencyRenderer extends ParentAwareRenderer {

    protected final ProgramElementDoc element;

    protected DependencyRenderer(Renderer parent, ProgramElementDoc element) {
        super(parent);
        this.element = requireNonNull(element);
    }

    @Override
    protected IndentingPrintWriter writeTo(IndentingPrintWriter output) {
        // Todo render dependencies.
        return output;
    }

}