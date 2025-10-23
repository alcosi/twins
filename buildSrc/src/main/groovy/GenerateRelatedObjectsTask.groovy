import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

abstract class GenerateRelatedObjectsTask extends DefaultTask {

    @TaskAction
    void generate() {
        def sourceDir = project.file('src/main/java/org/twins/core/dto/rest')
        def outputDir = project.file('build/generated/sources/dto/java/main/org/twins/core/dto/rest')
        try {
            // Configure JavaParser with SymbolSolver
            CombinedTypeSolver typeSolver = new CombinedTypeSolver();
            typeSolver.add(new ReflectionTypeSolver());
            typeSolver.add(new JavaParserTypeSolver(sourceDir));
            typeSolver.add(new JavaParserTypeSolver(project.file('src/main/java')))
            ParserConfiguration parserConfig = new ParserConfiguration();
            parserConfig.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
            parserConfig.setSymbolResolver(new JavaSymbolSolver(typeSolver));
            StaticJavaParser.setConfiguration(parserConfig);
            System.out.println("ParserConfiguration with SymbolSolver loaded: " + ParserConfiguration.class);
        } catch (Exception e) {
            System.err.println("Failed to configure JavaParser: " + e.getMessage());
            throw e;
        }

        if (outputDir.exists()) {
            outputDir.deleteDir()
        }
        outputDir.mkdirs()

        Files.walk(sourceDir.toPath())
                .filter { it.toString().endsWith('.java') }
                .forEach { Path sourcePath ->
                    def sourceFile = sourcePath.toFile()
                    def relativePath = sourceDir.toPath().relativize(sourcePath).toString()
                    def outputFile = project.file("${outputDir}/${relativePath}")
                    outputFile.parentFile.mkdirs()

                    def cu = StaticJavaParser.parse(sourceFile)
                    def hasRelatedObjects = false
                    def isResponseRelatedObjects = false
                    def importJsonIgnore = false
                    def importContainsRelatedObjects = false
                    def importRelatedObjectsDTOv1 = false
                    def importList = false
                    def relatedFields = []

                    // --- Collect fields annotated with @RelatedObject ---
                    cu.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        void visit(FieldDeclaration n, Void arg) {
                            super.visit(n, arg)
                            n.annotations.each { AnnotationExpr ann ->
                                if (ann.nameAsString == 'RelatedObject') {
                                    hasRelatedObjects = true
                                    def type = ''
                                    def name = ''
                                    if (ann instanceof NormalAnnotationExpr) {
                                        ann.pairs.each { MemberValuePair pair ->
                                            if (pair.nameAsString == 'type') {
                                                type = pair.value.toString().replace('.class', '')
                                            } else if (pair.nameAsString == 'name') {
                                                name = pair.value.toString().replaceAll('"', '')
                                            }
                                        }
                                    }

                                    def fieldName = n.variables.first().nameAsString
                                    def elementType = n.elementType.toString()
                                    def isCollection = elementType.startsWith("List") ||
                                            elementType.startsWith("Set") ||
                                            elementType.startsWith("Collection")

                                    // Determine getter name: use annotation "name" if present, otherwise fallback
                                    def getterName = name ? "get${name.capitalize()}" :
                                            "get${fieldName.capitalize().replaceAll('Ids?$', '').replaceAll('Id$', '')}"

                                    relatedFields << [
                                            fieldName    : fieldName,
                                            getterName   : getterName,
                                            type         : type,
                                            isCollection : isCollection
                                    ]
                                }
                            }
                        }
                        @Override
                        void visit(ClassOrInterfaceDeclaration n, Void arg) {
                            super.visit(n, arg);
                            if (n.isClassOrInterfaceDeclaration() ) {
                                logger.lifecycle("Checking inheritance for class: ${n.nameAsString}")
                                isResponseRelatedObjects = isExtendingResponseRelatedObjects(n)
                                logger.lifecycle("Class ${n.nameAsString} extends ResponseRelatedObjectsDTOv1: ${isResponseRelatedObjects}")
                            }
                        }
                    }, null)

                    if (!hasRelatedObjects && !isResponseRelatedObjects) {
                        logger.lifecycle("Skipping file: ${sourcePath}")
                        outputFile.text = sourceFile.text
                        return
                    }

                    if (hasRelatedObjects) {
                        // --- Remove @RelatedObject annotations ---
                        cu.accept(new VoidVisitorAdapter<Void>() {
                            @Override
                            void visit(FieldDeclaration n, Void arg) {
                                super.visit(n, arg)
                                n.getAnnotations().removeIf(ann -> ann.getNameAsString().equals("RelatedObject"))
                            }
                        }, null)

                        cu.getImports().removeIf { imp ->
                            imp.getNameAsString().endsWith("RelatedObject")
                        }
                    }

                    // --- Work with class declaration ---
                    def classDecl = cu.findFirst(ClassOrInterfaceDeclaration).get()



                    // Add private field "relatedObjects"
                    if (!isResponseRelatedObjects) {
                        // Add interface implementation if missing
                        if (!classDecl.implementedTypes.any { it.nameAsString == 'ContainsRelatedObjects' }) {
                            classDecl.addImplementedType('ContainsRelatedObjects')
                            importContainsRelatedObjects = true
                        }
                        def field = classDecl.addField('RelatedObjectsDTOv1', 'relatedObjects', com.github.javaparser.ast.Modifier.Keyword.PRIVATE)
                        field.addAnnotation('JsonIgnore')
                        importRelatedObjectsDTOv1 = true
                        importJsonIgnore = true
                        // --- Generate getRelatedObjects() if missing ---
                        if (!classDecl.members.any { it instanceof MethodDeclaration && it.nameAsString == 'getRelatedObjects' }) {
                            def getRelatedObjectsMethod = classDecl.addMethod('getRelatedObjects', com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                            getRelatedObjectsMethod.addAnnotation('Override')
                            getRelatedObjectsMethod.setComment(new com.github.javaparser.ast.comments.JavadocComment(
                                    "Generated by GenerateRelatedObjectsTask"
                            ))
                            getRelatedObjectsMethod.setType('RelatedObjectsDTOv1')
                            getRelatedObjectsMethod.setBody(StaticJavaParser.parseBlock('{ return relatedObjects; }'))
                        }

                        // --- Generate setRelatedObjects() if missing ---
                        if (!classDecl.members.any { it instanceof MethodDeclaration && it.nameAsString == 'setRelatedObjects' }) {
                            def setRelatedObjectsMethod = classDecl.addMethod('setRelatedObjects', com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                            setRelatedObjectsMethod.addAnnotation('Override')
                            setRelatedObjectsMethod.setComment(new com.github.javaparser.ast.comments.JavadocComment(
                                    "Generated by GenerateRelatedObjectsTask"
                            ))
                            setRelatedObjectsMethod.addParameter('RelatedObjectsDTOv1', 'relatedObjects')
                            setRelatedObjectsMethod.setType('void')
                            setRelatedObjectsMethod.setBody(StaticJavaParser.parseBlock('{ this.relatedObjects = relatedObjects; }'))
                        }
                    }

                    // --- Generate getter methods for all related fields ---
                    relatedFields.each { field ->
                        def getter = classDecl.addMethod(field.getterName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                        getter.addAnnotation('JsonIgnore')
                        importJsonIgnore = true
                        getter.setComment(new com.github.javaparser.ast.comments.JavadocComment(
                                "Generated by GenerateRelatedObjectsTask"
                        ))

                        if (field.isCollection) {
                            getter.setType("List<${field.type}>")
                            getter.setBody(StaticJavaParser.parseBlock("""
                            {
                                return getRelatedObjectList(${field.type}.class, ${field.fieldName});
                            }
                            """))
                            importList = true
                        } else {
                            getter.setType(field.type)
                            getter.setBody(StaticJavaParser.parseBlock("""
                            {
                                return getRelatedObject(${field.type}.class, ${field.fieldName});
                            }
                            """))
                        }
                    }

                    // Add required imports
                    if (!cu.getImports().any { it.nameAsString == 'org.twins.core.dto.rest.related.ContainsRelatedObjects' } && importContainsRelatedObjects)
                        cu.addImport('org.twins.core.dto.rest.related.ContainsRelatedObjects')
                    if (!cu.getImports().any { it.nameAsString == 'org.twins.core.dto.rest.related.RelatedObjectsDTOv1' } && importRelatedObjectsDTOv1)
                        cu.addImport('org.twins.core.dto.rest.related.RelatedObjectsDTOv1')
                    if (!cu.getImports().any { it.nameAsString == 'java.util.List' } && importList)
                        cu.addImport('java.util.List')
                    if (!cu.getImports().any { it.nameAsString == 'com.fasterxml.jackson.annotation.JsonIgnore' } && importJsonIgnore)
                        cu.addImport('com.fasterxml.jackson.annotation.JsonIgnore')


                    // --- Write generated class to output directory ---
                    outputFile.text = cu.toString()
                }
    }

    private boolean isExtendingResponseRelatedObjects(ClassOrInterfaceDeclaration classDecl) {
        try {
            def resolvedDecl = classDecl.resolve()
            def ancestors = resolvedDecl.getAllAncestors()
            logger.lifecycle("Ancestors for ${classDecl.nameAsString}: ${ancestors}")
            return ancestors.any { ancestor ->
                ancestor.qualifiedName == 'org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1'
            }
        } catch (Exception e) {
            logger.error("Error resolving inheritance for ${classDecl.nameAsString}: ${e.message}")
            return false
        }
    }
}
