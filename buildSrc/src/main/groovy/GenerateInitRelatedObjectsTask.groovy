import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

abstract class GenerateInitRelatedObjectsTask extends DefaultTask {
    @TaskAction
    void generate() {
        System.out.println("Starting GenerateInitRelatedObjectsTask...");
        try {
            // Configure JavaParser with SymbolSolver
            CombinedTypeSolver typeSolver = new CombinedTypeSolver();
            typeSolver.add(new ReflectionTypeSolver());
            typeSolver.add(new JavaParserTypeSolver(getProject().file("build/generated/sources/dto/java/main")));
            ParserConfiguration parserConfig = new ParserConfiguration();
            parserConfig.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
            parserConfig.setSymbolResolver(new JavaSymbolSolver(typeSolver));
            StaticJavaParser.setConfiguration(parserConfig);
            System.out.println("ParserConfiguration with SymbolSolver loaded: " + ParserConfiguration.class);
        } catch (Exception e) {
            System.err.println("Failed to configure JavaParser: " + e.getMessage());
            throw e;
        }

        Path sourceDir = getProject().file("build/generated/sources/dto/java/main").toPath();
        Path outputDir = getProject().file("generated").toPath(); // rewrite to same dir

        // Traverse files in sourceDir (generated)
        try {
            Files.walk(sourceDir)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(sourcePath -> {
                        System.out.println("Processing file: " + sourcePath);
                        File sourceFile = sourcePath.toFile();
                        Path relativePath = sourceDir.relativize(sourcePath);
                        File outputFile = new File(outputDir.toFile(), relativePath.toString());
                        outputFile.getParentFile().mkdirs();

                        CompilationUnit cu;
                        try {
                            cu = StaticJavaParser.parse(sourceFile);
                        } catch (Exception e) {
                            System.err.println("Error parsing file " + sourcePath + ": " + e.getMessage());
                            return;
                        }

                        // Find classes that extend ResponseRelatedObjectsDTOv1
                        boolean[] modified = {false};
                        cu.accept(new VoidVisitorAdapter<Void>() {
                            @Override
                            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                                super.visit(n, arg);
                                if (n.isClassOrInterfaceDeclaration() && n.getExtendedTypes().stream().anyMatch(t -> t.getNameAsString().equals("ResponseRelatedObjectsDTOv1"))) {
                                    modified[0] = processClass(n);
                                }
                            }
                        }, null);

                        // Write the modified file only if there were changes
                        if (modified[0]) {
                            try {
                                Files.writeString(sourcePath, cu.toString()); // Overwrite the source file in generated
                            } catch (Exception e) {
                                System.err.println("Error writing file " + sourcePath + ": " + e.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error walking through source directory: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean processClass(ClassOrInterfaceDeclaration classDecl) {
        List<FieldDeclaration> fieldsToProcess = new ArrayList<>();
        boolean modified = false;

        // Find fields whose type is ContainsRelatedObjects or a collection of ContainsRelatedObjects
        classDecl.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(FieldDeclaration n, Void arg) {
                super.visit(n, arg);
                Type fieldType = n.getVariable(0).getType();
                if (isContainsRelatedObjects(fieldType) || isCollectionOfContainsRelatedObjects(fieldType)) {
                    fieldsToProcess.add(n);
                }
            }
        }, null);

        // For each such field, add a getFieldName() method
        for (FieldDeclaration field : fieldsToProcess) {
            String fieldName = field.getVariable(0).getNameAsString();
            String capitalizedFieldName = capitalize(fieldName);
            String getterName = "get" + capitalizedFieldName;

            // Check if the method already exists
            if (classDecl.getMethodsByName(getterName).isEmpty()) {
                MethodDeclaration getter = classDecl.addMethod(getterName, Modifier.Keyword.PUBLIC);
                getter.setType(field.getVariable(0).getType());
                getter.setBody(StaticJavaParser.parseBlock(
                        "{\n" +
                                "    initWithRelatedObjects(" + fieldName + ");\n" +
                                "    return " + fieldName + ";\n" +
                                "}"
                ));
                modified = true;
                System.out.println("Added getter method: " + getterName + " for field: " + fieldName);
            } else {
                System.out.println("Getter method " + getterName + " already exists, skipping.");
            }
        }

        return modified;
    }

    private boolean isContainsRelatedObjects(Type type) {
        if (type.isClassOrInterfaceType()) {
            try {
                ResolvedType resolvedType = type.resolve();
                return resolvedType.isReferenceType() &&
                        resolvedType.asReferenceType().getAllInterfacesAncestors().stream()
                                .anyMatch(i -> i.getQualifiedName().equals("org.twins.core.dto.rest.related.ContainsRelatedObjects"));
            } catch (Exception e) {
                System.err.println("Failed to resolve type " + type + ": " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private boolean isCollectionOfContainsRelatedObjects(Type type) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classType = type.asClassOrInterfaceType();
            try {
                com.github.javaparser.resolution.types.ResolvedType resolvedType = type.resolve();
                if (resolvedType.isReferenceType() &&
                        resolvedType.asReferenceType().getAllInterfacesAncestors().stream()
                                .anyMatch(i -> i.getQualifiedName().equals("java.util.Collection"))) {
                    if (classType.getTypeArguments().isPresent() && !classType.getTypeArguments().get().isEmpty()) {
                        Type elementType = classType.getTypeArguments().get().get(0);
                        return isContainsRelatedObjects(elementType);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to resolve collection type " + type + ": " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
