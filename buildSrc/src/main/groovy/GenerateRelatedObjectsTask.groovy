import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

abstract class GenerateRelatedObjectsTask extends DefaultTask {

    @TaskAction
    void generate() {
        def parserConfig = new ParserConfiguration()
        parserConfig.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        StaticJavaParser.setConfiguration(parserConfig)

        def sourceDir = project.file('src/main/java/org/twins/core/dto/rest')
        def outputDir = project.file('build/generated/sources/dto/java/main/org/twins/core/dto/rest')

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
                    }, null)

                    if (!hasRelatedObjects) {
                        outputFile.text = sourceFile.text
                        return
                    }

                    // --- Remove @RelatedObject annotations ---
                    cu.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(FieldDeclaration n, Void arg) {
                            super.visit(n, arg)
                            n.getAnnotations().removeIf(ann -> ann.getNameAsString().equals("RelatedObject"))
                        }
                    }, null)

                    // --- Work with class declaration ---
                    def classDecl = cu.findFirst(ClassOrInterfaceDeclaration).get()

                    // Add interface implementation if missing
                    if (!classDecl.implementedTypes.any { it.nameAsString == 'ContainsRelatedObjects' }) {
                        classDecl.addImplementedType('ContainsRelatedObjects')
                    }

                    // Add required imports
                    if (!cu.getImports().any { it.nameAsString == 'org.twins.core.dto.rest.related.ContainsRelatedObjects' })
                        cu.addImport('org.twins.core.dto.rest.related.ContainsRelatedObjects')
                    if (!cu.getImports().any { it.nameAsString == 'org.twins.core.dto.rest.related.RelatedObjectsDTOv1' })
                        cu.addImport('org.twins.core.dto.rest.related.RelatedObjectsDTOv1')
                    if (!cu.getImports().any { it.nameAsString == 'jakarta.persistence.Transient' })
                        cu.addImport('jakarta.persistence.Transient')
                    if (!cu.getImports().any { it.nameAsString == 'java.util.List' })
                        cu.addImport('java.util.List')

                    // Add private field "relatedObjects"
                    classDecl.addField('RelatedObjectsDTOv1', 'relatedObjects', com.github.javaparser.ast.Modifier.Keyword.PRIVATE)

                    // --- Generate getter methods for all related fields ---
                    relatedFields.each { field ->
                        def getter = classDecl.addMethod(field.getterName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                        getter.addAnnotation('Transient')
                        getter.setComment(new com.github.javaparser.ast.comments.JavadocComment(
                                "Generated by GenerateRelatedObjectsTask"
                        ))

                        if (field.isCollection) {
                            getter.setType("List<${field.type}>")
                            getter.setBody(StaticJavaParser.parseBlock("""
                            {
                                return (List<${field.type}>) getRelatedObjectList(${field.type}.class, ${field.fieldName});
                            }
                            """))
                        } else {
                            getter.setType(field.type)
                            getter.setBody(StaticJavaParser.parseBlock("""
                            {
                                return (${field.type}) getRelatedObject(${field.type}.class, ${field.fieldName});
                            }
                            """))
                        }
                    }

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

                    // --- Write generated class to output directory ---
                    outputFile.text = cu.toString()
                }
    }
}
