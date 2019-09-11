package model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import project.JOutput;

public class JClass {
    // file info.
    private File file;
    private CompilationUnit cu;
    private String className;
    private String packageName;

    // id = packageName + className
    private String id;

    // body info.
    private ClassOrInterfaceDeclaration classBody;
    private List<JMethod> jMethods = new ArrayList<>();

    // declared imports in the class
    private HashMap<String, String> imports = new HashMap<>();
    // accessible classes in the same package including the class itself
    private HashMap<String, String> classes = new HashMap<>();
    // accessible fields within the class
    private HashMap<String, String> fields = new HashMap<>();
    // accessible methods within the class
    private HashMap<String, String> methods = new HashMap<>();

    private final String base = "com.github.javaparser.ast.";

    public JClass(String fullName, String code) {
        // code
        try {
            cu = JavaParser.parse(code);
            // class name
            String[] names = fullName.split("/");
            String name = names[names.length - 1];
            className = name.substring(0, name.length() - 5);

            // package name
            if (cu.getPackageDeclaration().isPresent()) {
                packageName = cu.getPackageDeclaration().get().getName().toString();
            }

            // class id
            id = packageName + "." + className;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void parse(List<JClass> files, JOutput jOutput) {
        if (cu == null) {
            return;
        }

        if (cu.getClassByName(className).isPresent()) {
            classBody = cu.getClassByName(className).get();
        } else if (cu.getInterfaceByName(className).isPresent()) {
            classBody = cu.getInterfaceByName(className).get();
        }

        if (classBody != null) {
            // imports
            for (ImportDeclaration ipt : cu.getImports()) {
                String id = ipt.getName().toString();
                String name = id.substring(id.lastIndexOf(".") + 1);
                imports.put(name, id);
            }

            // classes
            for (JClass file : files) {
                if (file.getPackageName() != null) {
                    if (file.getPackageName().equals(packageName)) {
                        classes.put(file.getClassName(), file.getPackageName() + '.' + file.getClassName());
                    }
                }
            }

            // fields, methods, constructors
            NodeList<BodyDeclaration<?>> list = classBody.getMembers();
            for (BodyDeclaration bd : list) {
                String name = bd.getClass().getName();
                if ((base + "body.FieldDeclaration").equals(name)) {
                    FieldDeclaration fd = (FieldDeclaration) bd;
                    NodeList<VariableDeclarator> nodes = fd.getVariables();
                    for (VariableDeclarator vd : nodes) {
                        String type = searchTypeForField(vd.getType().toString());
                        fields.put(vd.getName().toString(), type);
                    }
                } else if ((base + "body.MethodDeclaration").equals(name)) {
                    jMethods.add(new JMethod((MethodDeclaration) bd, this));
                } else if ((base + "body.ConstructorDeclaration").equals(name)) {
                    jMethods.add(new JMethod((ConstructorDeclaration) bd, this));
                }
            }

            // methods
            for (JMethod jm : jMethods) {
                methods.put(jm.getName(), id + "." + jm.getName() + "()");
                jm.setType(searchTypeForField(jm.getType()));
            }

            // parse body in methods
            for (JMethod jm : jMethods) {
                if (jm.getBs() != null) {
                    jOutput.cCmt++;
                    jOutput.write(jOutput.getFileComment(), jm.getComments(), jOutput.cCmt);

                    jOutput.cDoc++;
                    jOutput.write(jOutput.getFileJavadoc(), jm.getJavadocs(), jOutput.cDoc);

                    jOutput.cMet++;
                    jOutput.write(jOutput.getFileMethod(), jOutput.getProjectName() + "," + id + "," + jm.getName(), jOutput.cMet);

                    jOutput.cPar++;
                    jOutput.write(jOutput.getFileParameter(), jm.getParas(), jOutput.cPar);

                    jOutput.cMod++;
                    jOutput.write(jOutput.getFileModifiers(), jm.getModifiers(), jOutput.cMod);

                    jOutput.cRet++;
                    jOutput.write(jOutput.getFileReturn(), jm.getType(), jOutput.cRet);

                    jOutput.cSrc++;
                    jOutput.write(jOutput.getFileSourceCode(), jm.getCode(), jOutput.cSrc);

                    jm.parseBody(this, jOutput);
                }
            }
        }
    }

    public String searchTypeForField(String type) {
        int id1 = type.indexOf('<');
        int id2 = type.indexOf('(');
        int id3 = type.indexOf('[');
        int max = id1;
        if (id2 > max) {
            max = id2;
        }
        if (id3 > max) {
            max = id3;
        }
        String key = type;
        if (max > -1) {
            key = type.substring(0, max);
            if (imports.containsKey(key)) {
                type = imports.get(key) + type.substring(max);
            }
            if (classes.containsKey(key)) {
                type = classes.get(key) + type.substring(max);
            }
        } else {
            if (imports.containsKey(key)) {
                type = imports.get(key);
            }
            if (classes.containsKey(key)) {
                type = classes.get(key);
            }
        }
        return type;
    }

    public HashMap<String, String> getImports() {
        return imports;
    }

    public void setImports(HashMap<String, String> imports) {
        this.imports = imports;
    }

    // getters and setters
    public CompilationUnit getCu() {
        return cu;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCu(CompilationUnit cu) {
        this.cu = cu;
    }

    public ClassOrInterfaceDeclaration getClassBody() {
        return classBody;
    }

    public void setClassBody(ClassOrInterfaceDeclaration classBody) {
        this.classBody = classBody;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<JMethod> getjMethods() {
        return jMethods;
    }

    public void setjMethods(List<JMethod> jMethods) {
        this.jMethods = jMethods;
    }

    public HashMap<String, String> getClasses() {
        return classes;
    }

    public void setClasses(HashMap<String, String> classes) {
        this.classes = classes;
    }

    public HashMap<String, String> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, String> fields) {
        this.fields = fields;
    }

    public HashMap<String, String> getMethods() {
        return methods;
    }

    public void setMethods(HashMap<String, String> methods) {
        this.methods = methods;
    }
}
