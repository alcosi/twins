import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
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
        Map<String, String> supportedDtoTypes = [:]
        Set<String> classesWithRelatedAnnotation = [] as Set

        try {
            // Configure JavaParser with SymbolSolver
            CombinedTypeSolver typeSolver = new CombinedTypeSolver()
            typeSolver.add(new ReflectionTypeSolver())
            typeSolver.add(new JavaParserTypeSolver(sourceDir))
            typeSolver.add(new JavaParserTypeSolver(project.file('src/main/java')))
            ParserConfiguration parserConfig = new ParserConfiguration()
            parserConfig.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
            parserConfig.setSymbolResolver(new JavaSymbolSolver(typeSolver))
            StaticJavaParser.setConfiguration(parserConfig)
            System.out.println("ParserConfiguration with SymbolSolver loaded: " + ParserConfiguration.class)
            supportedDtoTypes = collectSupportedRelatedObjects()
            // Parse RelatedObjectsDTOv1 to extract supported DTO types from Map fields
            // --- collect names of classes that have @RelatedObject ---
            Files.walk(sourceDir.toPath())
                    .filter { it.toString().endsWith('.java') }
                    .forEach { Path p ->
                        def cuTmp = StaticJavaParser.parse(p.toFile())
                        cuTmp.findAll(ClassOrInterfaceDeclaration).each { c ->
                            def hasAnn = c.getFields().any { f ->
                                f.getAnnotations().any { a -> a.nameAsString == 'RelatedObject' }
                            }
                            if (hasAnn) classesWithRelatedAnnotation << c.nameAsString
                        }
                    }

        } catch (Exception e) {
            System.err.println("Failed to configure JavaParser or parse RelatedObjectsDTOv1: " + e.getMessage())
            throw e
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
                    def relatedObjectsFile = false
                    def isTwinDTOV2File = false
                    def isResponseRelatedObjects = false
                    def importJsonIgnore = false
                    def importContainsRelatedObjects = false
                    def importRelatedObjectsDTOv1 = false
                    def importList = false
                    def importCollections = false
                    def importMap = false
                    def importHashMap = false
                    def relatedFields = []

                    // --- Collect fields annotated with @RelatedObject ---
                    cu.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        void visit(ClassOrInterfaceDeclaration n, Void arg) {
                            super.visit(n, arg)
                            if (n.nameAsString == 'RelatedObjectsDTOv1') {
                                relatedObjectsFile = true
                                return
                            }
                            if (n.nameAsString == 'TwinDTOv2') {
                                isTwinDTOV2File = true
                                return
                            }
                            if (n.isClassOrInterfaceDeclaration()) {
                                logger.lifecycle("Checking inheritance for class: ${n.nameAsString}")
                                isResponseRelatedObjects = isExtendingResponseRelatedObjects(n)
                                logger.lifecycle("Class ${n.nameAsString} extends ResponseRelatedObjectsDTOv1: ${isResponseRelatedObjects}")
                            }
                        }
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

                                    // Validate type against supportedDtoTypes
                                    if (!supportedDtoTypes.containsKey(type)) {
                                        logger.error("Invalid RelatedObject type '${type}' in ${sourcePath}. Supported types: ${supportedDtoTypes}")
                                        throw new IllegalStateException("Invalid RelatedObject type '${type}' in ${sourcePath}. Supported types: ${supportedDtoTypes}")
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

//todo think over, how to add this method

//                    if (relatedObjectsFile) {
//                        // Generate RelatedObjectsDTOv1 with the get method
//                        def classDecl = cu.findFirst(ClassOrInterfaceDeclaration).get()
//
//                        // Generate new get method
//                        def getMethod = classDecl.addMethod('get', Modifier.Keyword.PUBLIC)
//                        getMethod.addTypeParameter('T')
//                        getMethod.addParameter('Class<T>', 'relatedObjectClass')
//                        getMethod.addParameter('Object', 'id')
//                        getMethod.setType('T')
//                        getMethod.setComment(new JavadocComment(
//                                "Generated by GenerateRelatedObjectsTask"
//                        ))
//
//                        // Build the method body
//                        def methodBody = new StringBuilder()
//                        methodBody.append("{\n")
//                        supportedDtoTypes.each { type ->
//                            def fieldType = type.key
//                            def fieldName = type.value
//                            methodBody.append("    if (relatedObjectClass == ${fieldType}.class) {\n")
//                            methodBody.append("        return (T) ${fieldName}.get(id);\n")
//                            methodBody.append("    } else ")
//                        }
//                        methodBody.append("{\n")
//                        methodBody.append("        return null;\n")
//                        methodBody.append("    }\n")
//                        methodBody.append("}\n")
//                        getMethod.setBody(StaticJavaParser.parseBlock(methodBody.toString()))
//                        outputFile.text = cu.toString()
//                        return
//                    }

                    if (isTwinDTOV2File) {
                        // Add private fields
                        def classDecl = cu.findFirst(ClassOrInterfaceDeclaration).get()
                        if (!classDecl.getFieldByName('fieldsByKey').isPresent()) {
                            def fieldsByKeyField = classDecl.addField('Map<String, TwinFieldDTOv3>', 'fieldsByKey', com.github.javaparser.ast.Modifier.Keyword.PRIVATE)
                            fieldsByKeyField.addAnnotation('JsonIgnore')
                            importJsonIgnore = true
                        }
                        if (!classDecl.getFieldByName('fieldsById').isPresent()) {
                            def fieldsByIdField = classDecl.addField('Map<UUID, TwinFieldDTOv3>', 'fieldsById', com.github.javaparser.ast.Modifier.Keyword.PRIVATE)
                            fieldsByIdField.addAnnotation('JsonIgnore')
                            importJsonIgnore = true
                        }

                        // Add getFieldByKey method if missing
                        if (!classDecl.getMethodsByName('getFieldByKey')) {
                            def getFieldByKey = classDecl.addMethod('getFieldByKey', com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                            getFieldByKey.addAnnotation('JsonIgnore')
                            importJsonIgnore = true
                            getFieldByKey.setType('TwinFieldDTOv3')
                            getFieldByKey.addParameter('String', 'fieldKey')
                            getFieldByKey.setBody(StaticJavaParser.parseBlock('''{
                                initFieldsObjects();
                                if (fieldsByKey == null || fieldKey == null) return null;
                                return fieldsByKey.get(fieldKey);
                            }'''))
                            getFieldByKey.setComment(new JavadocComment("Generated by GenerateRelatedObjectsTask"))
                        }

                        // Add getFieldById method if missing
                        if (!classDecl.getMethodsByName('getFieldById')) {
                            def getFieldById = classDecl.addMethod('getFieldById', com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                            getFieldById.addAnnotation('JsonIgnore')
                            importJsonIgnore = true
                            getFieldById.setType('TwinFieldDTOv3')
                            getFieldById.addParameter('UUID', 'fieldId')
                            getFieldById.setBody(StaticJavaParser.parseBlock('''{
                                initFieldsObjects();
                                if (fieldsById == null || fieldId == null) return null;
                                return fieldsById.get(fieldId);
                            }'''))
                            getFieldById.setComment(new JavadocComment("Generated by GenerateRelatedObjectsTask"))
                        }

                        // Add initFieldsObjects method if missing
                        if (!classDecl.getMethodsByName('initFieldsObjects')) {
                            def initFieldsObjects = classDecl.addMethod('initFieldsObjects', com.github.javaparser.ast.Modifier.Keyword.PRIVATE)
                            initFieldsObjects.addAnnotation('JsonIgnore')
                            importJsonIgnore = true
                            initFieldsObjects.setType('void')
                            initFieldsObjects.setBody(StaticJavaParser.parseBlock('''{
                                if (fieldsByKey != null || fieldsMap == null)
                                    return;
                                else if (fieldsMap.isEmpty()) {
                                    fieldsByKey = Collections.emptyMap();
                                    fieldsById = Collections.emptyMap();
                                    return;
                                }
                                fieldsByKey = new HashMap<>(fieldsMap.size());
                                fieldsById = new HashMap<>(fieldsMap.size());
                                for (var entry : fieldsMap.entrySet()) {
                                    var fieldDto = new TwinFieldDTOv3();
                                    fieldDto
                                            .setTwinClassFieldId(entry.getKey())
                                            .setValue(entry.getValue().getValue())
                                            .setRelatedObjects(getRelatedObjects());

                                    fieldsById.put(entry.getKey(), fieldDto);
                                    fieldsByKey.put(entry.getValue().getKey(), fieldDto);
                                }
                            }'''))
                            initFieldsObjects.setComment(new JavadocComment("Generated by GenerateRelatedObjectsTask"))
                        }
                        importCollections = true
                        importMap = true
                        importHashMap = true
                    }

                    if (!hasRelatedObjects && !isResponseRelatedObjects && !isTwinDTOV2File) {
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

                    // --- check if any ancestor class name is in classesWithRelatedAnnotation ---
                    boolean parentHasRelatedObjects = false
                    try {
                        def resolvedDecl = classDecl.resolve()
                        def ancestors = resolvedDecl.getAllAncestors()
                        parentHasRelatedObjects = ancestors.any { anc ->
                            try {
                                def qn = anc.qualifiedName
                                def simpleName = qn?.substring(qn.lastIndexOf('.') + 1)
                                return classesWithRelatedAnnotation.contains(qn) || classesWithRelatedAnnotation.contains(simpleName)
                            } catch (Exception ignored) {
                                return false
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Cannot resolve ancestors for ${classDecl.nameAsString}: ${e.message}")
                    }

                    // Add private field "relatedObjects" only if parent doesn't have it
                    if (!isResponseRelatedObjects && !parentHasRelatedObjects) {
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
                        getter.setComment(new JavadocComment(
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
                    if (!cu.getImports().any { it.nameAsString == 'java.util.Map' } && importMap)
                        cu.addImport('java.util.Map')
                    if (!cu.getImports().any { it.nameAsString == 'java.util.Collections' } && importCollections)
                        cu.addImport('java.util.Collections')
                    if (!cu.getImports().any { it.nameAsString == 'java.util.HashMap' } && importHashMap)
                        cu.addImport('java.util.HashMap')
                    if (!cu.getImports().any { it.nameAsString == 'com.fasterxml.jackson.annotation.JsonIgnore' } && importJsonIgnore)
                        cu.addImport('com.fasterxml.jackson.annotation.JsonIgnore')

                    // --- Write generated class to output directory ---
                    outputFile.text = cu.toString()
                }
    }

    private Map<String, String> collectSupportedRelatedObjects() {
        Map<String, String> supportedDtoTypes = [:]
        def relatedObjectsFile = project.file('src/main/java/org/twins/core/dto/rest/related/RelatedObjectsDTOv1.java')
        if (relatedObjectsFile.exists()) {
            def cu = StaticJavaParser.parse(relatedObjectsFile)
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                void visit(FieldDeclaration n, Void arg) {
                    super.visit(n, arg)
                    n.variables.each { variable ->
                        def type = variable.type
                        if (type instanceof ClassOrInterfaceType && type.nameAsString == 'Map') {
                            def typeArguments = type.asClassOrInterfaceType().typeArguments.orElse([])
                            if (typeArguments.size() == 2) {
                                def valueType = typeArguments[1].toString()
                                supportedDtoTypes[valueType] = variable.nameAsString
                            }
                        }
                    }
                }
            }, null)
            logger.lifecycle("Supported DTO types from RelatedObjectsDTOv1 Map fields: ${supportedDtoTypes}")
        } else {
            logger.error("RelatedObjectsDTOv1.java not found")
            throw new IllegalStateException("RelatedObjectsDTOv1.java not found")
        }
        return supportedDtoTypes
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