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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import nl.talsmasoftware.umldoclet.logging.LogSupport;
import nl.talsmasoftware.umldoclet.model.Reference;
import nl.talsmasoftware.umldoclet.rendering.indent.IndentingPrintWriter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static nl.talsmasoftware.umldoclet.model.Reference.Side.from;
import static nl.talsmasoftware.umldoclet.model.Reference.Side.to;

/**
 * Renderer for class references.
 *
 * @author Sjoerd Talsma
 */
public class ClassReferenceRenderer extends ClassRenderer {
    protected final ClassRenderer parent;
    protected Reference reference;

//    protected final String qualifiedName;
//    protected final String umlreference;

    // Additional info fields to be added to the type.
//    String cardinality1, cardinality2;
//    final Collection<String> notes = new LinkedHashSet<>();

    /**
     * Creates a new class type to be rendered.
     *
     * @param parent          The class the type is from (which is the parent of this referencerenderer).
     * @param documentedClass The class the type is to.
     * @param umlreference    The UML type itself (reversed, so inheritance is <code>&lt;|--</code>).
     */
    private ClassReferenceRenderer(ClassRenderer parent, ClassDoc documentedClass, String umlreference) {
//        this(parent, documentedClass, null, umlreference);
        this(parent, requireNonNull(documentedClass, "Referred class was <null>.").qualifiedName(), umlreference);
    }

    /**
     * Creates a new class type, but the referred class is not (yet) available for documentation.
     *
     * @param parent           The class the type is from (which is the parent of this referencerenderer).
     * @param referenceFromFqn The qualified of the referred class.
     * @param umlreference     The UML type itself (from the second argument to the first, so reversed:
     *                         inheritance is <code>&lt;|--</code>).
     */
    protected ClassReferenceRenderer(ClassRenderer parent, String referenceFromFqn, String umlreference) {
//        this(parent, null, documentedClassQualifiedName, umlreference);
        this(parent, new Reference(from(referenceFromFqn), umlreference,
                to(requireNonNull(parent, "Parent was <null>.").classDoc.qualifiedName())));
    }

//    private ClassReferenceRenderer(ClassRenderer parent, ClassDoc documentedClass, String qualifiedName, String umlreference) {
//        super(parent, documentedClass == null ? parent.classDoc : documentedClass);
//        this.parent = requireNonNull(parent, "No parent renderer for class type provided.");
//        super.children.clear();
//        this.qualifiedName = requireNonNull(documentedClass == null ? qualifiedName : documentedClass.qualifiedName(),
//                "Qualified name of documented type is required.");
//        this.umlreference = requireNonNull(umlreference, "No UML type type provided.");
//        if (diagram.config.includeAbstractSuperclassMethods() && documentedClass != null) {
//            for (MethodDoc methodDoc : documentedClass.methods(false)) {
//                if (methodDoc.isAbstract()) {
//                    children.add(new MethodRenderer(diagram, methodDoc));
//                }
//            }
//        }
//    }

    protected ClassReferenceRenderer(ClassRenderer parent, Reference reference) {
        super(parent, referredClassDoc(parent, reference));
        super.children.clear();
        this.parent = parent;
        this.reference = requireNonNull(reference, "Reference is <null>.");
        if (!reference.isSelfReference()
                && diagram.config.includeAbstractSuperclassMethods()
                && !classDoc.equals(parent.classDoc)) { // Append abstract methods from referred superclass methods.
            for (MethodDoc methodDoc : classDoc.methods(false)) {
                if (methodDoc.isAbstract()) children.add(new MethodRenderer(diagram, methodDoc));
            }
        }
    }

    private static ClassDoc referredClassDoc(ClassRenderer source, Reference reference) {
        if (source == null) return null;
        if (reference != null) for (Reference.Side side : new Reference.Side[]{reference.to, reference.from}) {
            if (!side.qualifiedName.equals(source.classDoc.qualifiedName())) {
                ClassDoc referredClassDoc = source.classDoc.findClass(side.qualifiedName);
                if (referredClassDoc != null) return referredClassDoc;
            }
        }
        return source.classDoc;
    }

    /**
     * This generator method creates a collection of references for a given class.
     *
     * @param parent The rendered class to create references for.
     * @return The references.
     */
    static Collection<ClassReferenceRenderer> referencesFor(ClassRenderer parent) {
        requireNonNull(parent, "Included class is required in order to find its references.");
        final String referentName = parent.classDoc.qualifiedName();
        LogSupport.trace("Adding references for included class {0}...", referentName);
        final Collection<ClassReferenceRenderer> references = new LinkedHashSet<>();
        final Collection<String> excludedReferences = parent.diagram.config.excludedReferences();

        // Add extended superclass type.
        ClassDoc superclass = parent.classDoc.superclass();
        final String superclassName = superclass == null ? null : superclass.qualifiedName();
        if (superclassName == null) {
            LogSupport.debug("Encountered <null> as superclass of \"{0}\".", referentName);
        } else if (excludedReferences.contains(superclassName)) {
            LogSupport.trace("Excluding superclass \"{0}\" of \"{1}\"...", superclassName, referentName);
        } else if (references.add(new ClassReferenceRenderer(parent, superclass, "<|--"))) {
            LogSupport.trace("Added type to superclass \"{0}\" from \"{1}\".", superclassName, referentName);
        } else {
            LogSupport.trace("Excluding type to superclass \"{0}\" from \"{1}\"; the type was already generated.",
                    superclassName, referentName);
        }

        // Add implemented interface references.
        for (ClassDoc interfaceDoc : parent.classDoc.interfaces()) {
            final String interfaceName = interfaceDoc == null ? null : interfaceDoc.qualifiedName();
            if (interfaceName == null) {
                LogSupport.info("Encountered <null> as implemented interface of \"{0}\".", referentName);
            } else if (excludedReferences.contains(interfaceName)) {
                LogSupport.trace("Excluding interface \"{0}\" of \"{1}\"...", interfaceName, referentName);
            } else if (references.add(new ClassReferenceRenderer(parent, interfaceDoc, "<|.."))) {
                LogSupport.trace("Added type to interface \"{0}\" from \"{1}\".", interfaceName, referentName);
            } else {
                LogSupport.debug("Excluding type to interface \"{0}\" from \"{1}\"; the type was already generated.", interfaceName, referentName);
            }
        }

        // Add type to containing classes.
        if (parent.classDoc.containingClass() != null) {
            references.add(new ClassReferenceRenderer(parent, parent.classDoc.containingClass(), "+--"));
        }

        // Support for tags defined in legacy doclet.
        references.addAll(LegacyTag.legacyReferencesFor(parent));

        return references;
    }

    private String guessClassOrInterface() {
        return "<|..".equals(reference.type) || "..|>".equals(reference.type) ? "interface" : "class";
    }

    protected IndentingPrintWriter writeTypeDeclarationsTo(IndentingPrintWriter out) {
        for (final Reference.Side side : new Reference.Side[]{reference.from, reference.to}) {
            if (!diagram.encounteredTypes.add(side.qualifiedName)) {
                LogSupport.trace("Not generating type declaration for \"{0}\"; " +
                        "type was previously encountered in this diagram.", side.qualifiedName);
                continue;
            }
            final ClassDoc typeInfo = classDoc.findClass(side.qualifiedName);
            if (typeInfo == null) {
                LogSupport.trace("Generating 'unknown' class type declaration for \"{0}\"; " +
                        "we only have a class name type as declaration.", name());
                out.append(guessClassOrInterface());
                out.whitespace().append(parent.nameOf(side.qualifiedName));
                out.whitespace().append("<<(?,orchid)>>").newline();
                continue;
            }

            LogSupport.trace("Generating type declaration for \"{0}\"...", typeInfo.qualifiedName());
            out.append(umlTypeOf(typeInfo));
            out.whitespace().append(parent.nameOf(typeInfo.qualifiedName()));
            writeGenericsOf(typeInfo, out);
            if (!children.isEmpty()) writeChildrenTo(out.whitespace().append("{").newline()).append('}');
            out.newline();
        }
//        if (!diagram.encounteredTypes.add(qualifiedName)) {
//            LogSupport.trace("Not generating type declaration for \"{0}\"; " +
//                    "type was previously encountered in this diagram.", qualifiedName);
//            return out;
//        } else if (!qualifiedName.equals(classDoc.qualifiedName())) {
//            LogSupport.trace("Generating 'unknown' class type declaration for \"{0}\"; " +
//                    "we only have a class name type as declaration.", name());
//            return out.append(guessClassOrInterface()).whitespace().append(name()).append(" <<(?,orchid)>>").newline();
//        }
//
//        LogSupport.trace("Generating type declaration for \"{0}\"...", name());
//        out.append(umlType()).whitespace().append(name());
//        super.writeGenericsTo(out);
//        if (!children.isEmpty()) {
//            writeChildrenTo(out.append(" {").newline()).append('}');
//        }
//        return out.newline();
        return out;
    }

//    @Override
//    protected String name() {
//        // Optionally simplify the name within the referring class' package.
//        return parent.simplifyClassnameWithinPackage(qualifiedName);
//    }

    /**
     * @return Whether this type is to the class itself.
     */
    protected boolean isSelfReference() {
//        return this.qualifiedName.equals(this.parent.classDoc.qualifiedName());
        return reference.isSelfReference();
    }

    /**
     * @param note The note to be added to this class reference.
     */
    protected void addNote(String note) {
        this.reference = reference.addNote(note);
    }

    protected IndentingPrintWriter writeTo(IndentingPrintWriter out) {
        // Write type declaration if necessary.
        writeTypeDeclarationsTo(out);

        // Write UML type itself.
//        LogSupport.trace("Generating type: \"{0}\" {1} \"{2}\"...", qualifiedName, umlreference, parent.name());
//        out.append(name()).whitespace()
//                .append(quoted(cardinality2)).whitespace()
//                .append(umlreference).whitespace()
//                .append(quoted(cardinality1)).whitespace()
//                .append(parent.name());
        LogSupport.trace("Generating type: {0}...", reference);
        out.append(parent.simplifyClassnameWithinPackage(reference.from.qualifiedName)).whitespace()
                .append(quoted(reference.from.cardinality)).whitespace()
                .append(reference.type).whitespace()
                .append(quoted(reference.to.cardinality)).whitespace()
                .append(parent.simplifyClassnameWithinPackage(reference.to.qualifiedName));

        if (!reference.notes.isEmpty()) {
            String sep = ": ";
            for (String note : reference.notes) {
                out.append(sep).append(note);
                sep = "\\n";
            }
        }
        return out.newline().newline();
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof ClassReferenceRenderer
                && reference.equals(((ClassReferenceRenderer) other).reference));
    }

}
