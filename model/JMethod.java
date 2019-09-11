package model;

import java.util.HashMap;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import project.JOutput;

public class JMethod {
    private BlockStmt bs;

    private boolean isConstructor = false;

    // method parameters
    private HashMap<String, String> parameters = new HashMap<>();
    private String paras = "";

    // method name and returned type
    private String name;
    private String type;
    private String modifiers;
    private String code;

    // method comments
    private String comments = "[]";
    private String javadocs = "[]";

    // method body
    private JBody jBody;
    private final String regexp = "(\r\n|\r|\n|\n\r|\\r\\n|\\r|\\n|\\n\\r|\\\\r\\\\n|\\\\r|\\\\n|\\\\n\\\\r)";

    public JMethod(MethodDeclaration md, JClass jClass) {
        code = md.toString();
        comments = md.getAllContainedComments().toString();
        if (md.getJavadocComment().isPresent()) {
            javadocs = md.getJavadocComment().get().toString();
            if (javadocs.equals("Optional.empty")) {
                javadocs = "[]";
            }
        }
        name = md.getName().getIdentifier();
        type = md.getType().toString();
        if (md.getBody().isPresent()) {
            bs = md.getBody().get();
        }
        for (Parameter p : md.getParameters()) {
            String namePar = p.getName().getIdentifier();
            String typePar = jClass.searchTypeForField(p.getType().toString());
            parameters.put(namePar, typePar);
            paras += typePar + "," + namePar + ";";
        }
        paras = paras.replaceAll(regexp, ";");
        type = type.replaceAll(regexp, ";");
        type = type.substring(type.indexOf(';')+1);
        comments = comments.replaceAll(regexp, ";");
        javadocs = javadocs.replaceAll(regexp, ";");
        code = code.replaceAll(regexp, ";");
        modifiers = md.getModifiers().toString().toLowerCase();
    }

    public JMethod(ConstructorDeclaration md, JClass jClass) {
        code = md.toString();
        isConstructor = true;
        type = "void";
        comments = md.getAllContainedComments().toString();
        javadocs = md.getJavadocComment().toString();
        if (javadocs.equals("Optional.empty")) {
            javadocs = "[]";
        }
        name = md.getName().getIdentifier();
        if (!md.getBody().isEmpty()) {
            bs = md.getBody();
        }
        for (Parameter p : md.getParameters()) {
            String namePar = p.getName().getIdentifier();
            String typePar = jClass.searchTypeForField(p.getType().toString());
            parameters.put(namePar, typePar);
            paras += typePar + "," + namePar + ";";
        }
        paras = paras.replaceAll(regexp, ";");
        comments = comments.replaceAll(regexp, ";");
        javadocs = javadocs.replaceAll(regexp, ";");
        code = code.replaceAll(regexp, ";");
        modifiers = md.getModifiers().toString().toLowerCase();
    }

    public void parseBody(JClass jClass, JOutput jOutput) {
        if (bs != null) {
            jBody = new JBody(bs, parameters, jClass, jOutput);
        }
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JBody getjBody() {
        return jBody;
    }

    public void setjBody(JBody jBody) {
        this.jBody = jBody;
    }

    public BlockStmt getBs() {
        return bs;
    }

    public void setBs(BlockStmt bs) {
        this.bs = bs;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getJavadocs() {
        return javadocs;
    }

    public void setJavadocs(String javadocs) {
        this.javadocs = javadocs;
    }

    public String getParas() {
        return paras;
    }

    public void setParas(String paras) {
        this.paras = paras;
    }

    public String getRegexp() {
        return regexp;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
