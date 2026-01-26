import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

abstract class GenerateTypeScriptDTOsTask extends DefaultTask {

    @TaskAction
    void generate() {
        ParserConfiguration parserConfig = new ParserConfiguration()
        parserConfig.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        StaticJavaParser.setConfiguration(parserConfig)

        def javaDir = project.file('build/generated/sources/dto/java/main')
        def tsDir = project.file('build/generated/sources/dto/ts/main')
        if (tsDir.exists()) {
            tsDir.deleteDir()
        }
        tsDir.mkdirs()

        Map<String, String> supportedDtoTypes = this.collectSupportedRelatedObjects(javaDir)

        Map<String, String> classToRelFile = [:]
        Files.walk(javaDir.toPath())
                .filter { it.toString().endsWith('.java') }
                .forEach { Path p ->
                    def rel = javaDir.toPath().relativize(p).toString()
                    def cuTmp = StaticJavaParser.parse(p.toFile())
                    def optClass = cuTmp.findFirst(ClassOrInterfaceDeclaration)
                    if (optClass.isPresent()) {
                        def cname = optClass.get().nameAsString
                        classToRelFile[cname] = rel.replace('.java', '')
                    }
                }

        def javaPaths = Files.walk(javaDir.toPath()).filter { it.toString().endsWith('.java') }.toList()
        javaPaths.each { Path sourcePath ->
            def relativePath = javaDir.toPath().relativize(sourcePath).toString()
            def tsRelative = relativePath.replace('.java', '.ts')
            def outputFile = project.file("${tsDir}/${tsRelative}")
            outputFile.parentFile.mkdirs()

            def cu = StaticJavaParser.parse(sourcePath.toFile())
            def optionalClassDecl = cu.findFirst(ClassOrInterfaceDeclaration)
            if (!optionalClassDecl.isPresent()) {
                logger.warn("Skipping file without class or interface declaration: ${sourcePath}")
                return
            }
            def classDecl = optionalClassDecl.get()
            def className = classDecl.nameAsString

            def extendsClause = classDecl.extendedTypes.isNonEmpty() ? "extends ${classDecl.extendedTypes[0].nameAsString}" : ''
            def implementsClause = classDecl.implementedTypes.isNonEmpty() ? "implements ${classDecl.implementedTypes.collect { it.nameAsString }.join(', ')}" : ''

            def tsCode = new StringBuilder()
            tsCode.append("export class ${className} ${extendsClause} ${implementsClause} {\n")

            Set<String> imports = [] as Set

            // Extract from extends and implements
            if (classDecl.extendedTypes.isNonEmpty()) {
                def extended = classDecl.extendedTypes[0].nameAsString
                if (!this.isBasicJavaType(extended)) {
                    imports << extended
                }
            }
            classDecl.implementedTypes.each { itf ->
                def name = itf.nameAsString
                if (!this.isBasicJavaType(name)) {
                    imports << name
                }
            }

            // Fields
            classDecl.findAll(FieldDeclaration).each { FieldDeclaration f ->
                def javaType = f.elementType.toString()
                def tsType = this.convertJavaToTsType(javaType)
                def name = f.variables[0].nameAsString
                def visibility = f.isPrivate() ? 'private ' : 'public '
                tsCode.append("  ${visibility}${name}?: ${tsType};\n")
                this.extractTypes(javaType, imports)
            }

            // Extract from methods
            classDecl.findAll(MethodDeclaration).each { MethodDeclaration m ->
                this.extractTypes(m.type.toString(), imports)
                m.parameters.each { param ->
                    this.extractTypes(param.type.toString(), imports)
                }
            }

            // Methods
            boolean hasRelatedObjectsField = classDecl.getFieldByName('relatedObjects').isPresent()
            boolean hasRelatedGetters = false

            classDecl.findAll(MethodDeclaration).each { MethodDeclaration m ->
                if (m.nameAsString == 'getRelatedObjects') {
                    tsCode.append("  public getRelatedObjects(): RelatedObjectsDTOv1 | undefined {\n")
                    tsCode.append("    return this.relatedObjects;\n")
                    tsCode.append("  }\n")
                } else if (m.nameAsString == 'setRelatedObjects') {
                    tsCode.append("  public setRelatedObjects(relatedObjects: RelatedObjectsDTOv1): void {\n")
                    tsCode.append("    this.relatedObjects = relatedObjects;\n")
                    tsCode.append("  }\n")
                } else if (this.isRelatedGetter(m)) {
                    hasRelatedGetters = true
                    def converted = this.convertRelatedGetter(m)
                    tsCode.append(converted)
                } else if (className == 'TwinDTOv2' && m.nameAsString == 'getFieldByKey') {
                    tsCode.append("  public getFieldByKey(fieldKey: string | null): TwinFieldDTOv3 | undefined {\n")
                    tsCode.append("    this.initFieldsObjects();\n")
                    tsCode.append("    if (this.fieldsByKey == null || fieldKey == null) return undefined;\n")
                    tsCode.append("    return this.fieldsByKey[fieldKey];\n")
                    tsCode.append("  }\n")
                } else if (className == 'TwinDTOv2' && m.nameAsString == 'getFieldById') {
                    tsCode.append("  public getFieldById(fieldId: string | null): TwinFieldDTOv3 | undefined {\n")
                    tsCode.append("    this.initFieldsObjects();\n")
                    tsCode.append("    if (this.fieldsById == null || fieldId == null) return undefined;\n")
                    tsCode.append("    return this.fieldsById[fieldId];\n")
                    tsCode.append("  }\n")
                } else if (className == 'TwinDTOv2' && m.nameAsString == 'initFieldsObjects') {
                    tsCode.append("  private initFieldsObjects(): void {\n")
                    tsCode.append("    if (this.fieldsByKey !== undefined || this.fieldsMap == null)\n")
                    tsCode.append("      return;\n")
                    tsCode.append("    if (Object.keys(this.fieldsMap).length === 0) {\n")
                    tsCode.append("      this.fieldsByKey = {};\n")
                    tsCode.append("      this.fieldsById = {};\n")
                    tsCode.append("      return;\n")
                    tsCode.append("    }\n")
                    tsCode.append("    this.fieldsByKey = {};\n")
                    tsCode.append("    this.fieldsById = {};\n")
                    tsCode.append("    for (const [key, entry] of Object.entries(this.fieldsMap)) {\n")
                    tsCode.append("      const fieldDto = new TwinFieldDTOv3();\n")
                    tsCode.append("      fieldDto.twinClassFieldId = key;\n")
                    tsCode.append("      fieldDto.value = entry.value;\n")
                    tsCode.append("      fieldDto.relatedObjects = this.getRelatedObjects();\n")
                    tsCode.append("      this.fieldsById[key] = fieldDto;\n")
                    tsCode.append("      this.fieldsByKey[entry.key] = fieldDto;\n")
                    tsCode.append("    }\n")
                    tsCode.append("  }\n")
                }
            }

            // Add helper methods if needed
            if (hasRelatedObjectsField && hasRelatedGetters) {
                tsCode.append("  public getRelatedObject<T>(relatedObjectClass: new () => T, id: string | null | undefined): T | undefined {\n")
                tsCode.append("    if (id == null || id === undefined) return undefined;\n")
                tsCode.append("    return this.getRelatedObjects()?.get(relatedObjectClass, id);\n")
                tsCode.append("  }\n")
                tsCode.append("  public getRelatedObjectList<T>(relatedObjectClass: new () => T, ids: string[] | null | undefined): T[] | undefined {\n")
                tsCode.append("    if (ids == null || ids === undefined) return undefined;\n")
                tsCode.append("    return ids.map(id => this.getRelatedObject(relatedObjectClass, id)).filter((x): x is T => x !== undefined);\n")
                tsCode.append("  }\n")
            }

            tsCode.append("}\n")

            // Special handling for RelatedObjectsDTOv1
            if (className == 'RelatedObjectsDTOv1') {
                def getMethod = new StringBuilder()
                getMethod.append("  public get<T>(relatedObjectClass: new () => T, id: string | null | undefined): T | undefined {\n")
                getMethod.append("    if (id == null || id === undefined) return undefined;\n")
                supportedDtoTypes.each { type, field ->
                    getMethod.append("    if (relatedObjectClass === ${type}) {\n")
                    getMethod.append("      return this.${field}?.[id] as T | undefined;\n")
                    getMethod.append("    }\n")
                }
                getMethod.append("    return undefined;\n")
                getMethod.append("  }\n")
                tsCode.insert(tsCode.lastIndexOf('}'), getMethod.toString())
            }

            // Generate imports
            def currentRelPath = Path.of(relativePath).parent ?: Path.of("")
            def importLines = imports.findAll { it != className && !this.isBasicJavaType(it) && classToRelFile.containsKey(it) }
                    .collect { String it ->
                        def importRelFile = classToRelFile[it]
                        def importRelPath = Path.of(importRelFile).parent ?: Path.of("")
                        def rel = currentRelPath.relativize(importRelPath)
                        def relPathStr = rel.toString().replace('\\', '/')
                        def prefix = relPathStr ? relPathStr + '/' : ''
                        def importFrom = "./${prefix}${it}"
                        "import { ${it} } from '${importFrom}';"
                    }
                    .join("\n")

            outputFile.text = (importLines ? importLines + "\n\n" : "") + tsCode.toString()
        }
    }

    Map<String, String> collectSupportedRelatedObjects(File javaDir) {
        Map<String, String> supported = [:]
        def relatedFilePath = new File(javaDir, 'org/twins/core/dto/rest/related/RelatedObjectsDTOv1.java')
        if (!relatedFilePath.exists()) {
            logger.error("RelatedObjectsDTOv1.java not found in generated sources")
            throw new IllegalStateException("RelatedObjectsDTOv1.java not found")
        }
        def cu = StaticJavaParser.parse(relatedFilePath)
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            void visit(FieldDeclaration n, Void arg) {
                super.visit(n, arg)
                def type = n.elementType.toString()
                if (type.startsWith('Map<')) {
                    def typeArgs = n.elementType.asClassOrInterfaceType().typeArguments.orElse(null)
                    if (typeArgs && typeArgs.size() == 2) {
                        def valueType = typeArgs[1].toString()
                        def name = n.variables[0].nameAsString
                        supported[valueType] = name
                    }
                }
            }
        }, null)
        return supported
    }

    String convertJavaToTsType(String javaType) {
        javaType = javaType.trim()
        if (javaType.isEmpty()) return 'any'
        if (javaType in ['String', 'UUID', 'LocalDateTime', 'LocalDate', 'LocalTime', 'Instant', 'ZonedDateTime', 'BigDecimal']) return 'string'
        if (javaType in ['boolean', 'Boolean']) return 'boolean'
        if (javaType in ['int', 'long', 'double', 'float', 'Integer', 'Long', 'Double', 'Float']) return 'number'
        if (!javaType.contains('<')) return javaType
        def openIndex = javaType.indexOf('<')
        def base = javaType.substring(0, openIndex).trim()
        def genericsStr = javaType.substring(openIndex + 1, javaType.length() - 1).trim()
        def subTypes = this.splitTopLevelCommas(genericsStr)
        def convertedSubs = subTypes.collect { this.convertJavaToTsType(it) }
        if (base in ['List', 'Collection', 'Set']) {
            return convertedSubs[0] + '[]'
        } else if (base == 'Map') {
            def key = convertedSubs[0]
            def value = convertedSubs[1]
            return "{ [key: ${key}]: ${value} }"
        } else {
            return base + '<' + convertedSubs.join(', ') + '>'
        }
    }

    void extractTypes(String javaType, Set<String> imports) {
        javaType = javaType.trim()
        if (javaType.isEmpty()) return
        if (this.isBasicJavaType(javaType)) return
        if (!javaType.contains('<')) {
            imports << javaType
            return
        }
        def openIndex = javaType.indexOf('<')
        def base = javaType.substring(0, openIndex).trim()
        if (!(base in ['List', 'Set', 'Map', 'Collection'])) {
            if (!this.isBasicJavaType(base)) {
                imports << base
            }
        }
        def genericsStr = javaType.substring(openIndex + 1, javaType.length() - 1).trim()
        def subTypes = this.splitTopLevelCommas(genericsStr)
        subTypes.each { this.extractTypes(it, imports) }
    }

    private List<String> splitTopLevelCommas(String s) {
        List<String> parts = []
        int level = 0
        StringBuilder current = new StringBuilder()
        for (char c : s.toCharArray()) {
            if (c == '<') level++
            else if (c == '>') level--
            else if (c == ',' && level == 0) {
                parts << current.toString().trim()
                current = new StringBuilder()
                continue
            }
            current.append(c)
        }
        if (current.length() > 0) parts << current.toString().trim()
        return parts
    }

    boolean isBasicJavaType(String type) {
        return type in ['String', 'UUID', 'int', 'long', 'double', 'float', 'boolean', 'Object', 'void', 'Boolean', 'Integer', 'Long', 'Double', 'Float', 'LocalDateTime', 'LocalDate', 'LocalTime', 'Instant', 'ZonedDateTime', 'BigDecimal']
    }

    boolean isRelatedGetter(MethodDeclaration m) {
        if (!m.body.isPresent()) return false
        BlockStmt body = m.body.get()
        if (body.statements.size() != 1) return false
        def stmt = body.statements[0]
        if (!stmt.isReturnStmt()) return false
        ReturnStmt returnStmt = stmt.asReturnStmt()
        if (!returnStmt.expression.isPresent() || !returnStmt.expression.get().isMethodCallExpr()) return false
        MethodCallExpr call = returnStmt.expression.get().asMethodCallExpr()
        def callName = call.nameAsString
        return callName in ['getRelatedObject', 'getRelatedObjectList'] && call.arguments.size() == 2
    }

    String convertRelatedGetter(MethodDeclaration m) {
        BlockStmt body = m.body.get()
        ReturnStmt returnStmt = body.statements[0].asReturnStmt()
        MethodCallExpr call = returnStmt.expression.get().asMethodCallExpr()
        def callName = call.nameAsString
        def typeClass = call.arguments[0].toString().replace('.class', '')
        def field = call.arguments[1].toString()
        def tsReturnType = this.convertJavaToTsType(m.type.toString())
        def tsMethodName = m.nameAsString
        def sb = new StringBuilder()
        sb.append("  public ${tsMethodName}(): ${tsReturnType} | undefined {\n")
        if (callName == 'getRelatedObject') {
            sb.append("    return this.getRelatedObjects()?.get(${typeClass}, this.${field});\n")
        } else {
            sb.append("    return this.getRelatedObjectList(${typeClass}, this.${field});\n")
        }
        sb.append("  }\n")
        return sb.toString()
    }
}