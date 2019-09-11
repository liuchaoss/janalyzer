package model;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import project.JOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class _JBody {
    private JClass jClass;
    // class related imports, classes within the same package, fields, methods
    private HashMap<String, String> imports;
    private HashMap<String, String> classes;
    private HashMap<String, String> fields;
    private HashMap<String, String> methods;
    private HashMap<String, String> jdk;

    // method parameters
    private HashMap<String, String> parameters;

    // dynamic local variables in the method body
    private HashMap<String, String> variables;

    // parsed statements in the method body for printing out.
    private List<String> statements;

    private final String base = "com.github.javaparser.ast.";
    private final String regexp = "(\r\n|\r|\n|\n\r|\\r\\n|\\r|\\n|\\n\\r|\\\\r\\\\n|\\\\r|\\\\n|\\\\n\\\\r)";

    public _JBody(BlockStmt bs, HashMap<String, String> parameters, JClass jClass, JOutput jOutput) {
        this.jdk = jOutput.getJdk();
        this.jClass = jClass;
        this.imports = jClass.getImports();
        this.classes = jClass.getClasses();
        this.fields = jClass.getFields();
        this.methods = jClass.getMethods();
        this.parameters = parameters; // the parameters of method itself.
        this.variables = new HashMap<>();
        this.statements = new ArrayList<>();

        String source = "";
        for (Statement stmt: bs.getStatements()){
            source += stmt.toString() + ";";
        }
        source = source.replaceAll(regexp, ";");
        jOutput.cSce++;
        jOutput.write(jOutput.getFileSource(), source, jOutput.cSce);

        NodeList<Statement> list = bs.getStatements();
        if (!list.isEmpty()) {
            if (list.getParentNode().isPresent()) {
                access(list.getParentNode().get().getChildNodes());
            }
        }

        List<String> code = new ArrayList<>();
        for (String c: statements){
            String cc = c.replaceAll(regexp, ";");
            code.add(cc);
        }
        if (statements.isEmpty()){
            statements.add("[]");
        }
        jOutput.cPrs++;
        jOutput.write(jOutput.getFileParsedCode(), code, ";", jOutput.cPrs);
    }

    private List<Node> node2list(Node node) {
        List<Node> list = new ArrayList<>();
        list.add(node);
        return list;
    }

    private void access(List<Node> list) {
        for (Node node : list) {
            List<Node> childNodes = node.getChildNodes();
            String classStr = node.getClass().getTypeName();
                if ((base + "expr.AssignExpr").equals(classStr)) {
                access(node2list(childNodes.get(1)));
                access(node2list(childNodes.get(0)));
                continue;
            } else if ((base + "expr.VariableDeclarationExpr").equals(classStr)) {
                // type(2) variable(0) = expression(1)
                // type(1) variable(0)
                List<Node> nodes = childNodes.get(0).getChildNodes();
                String name = nodes.get(0).toString();
                if (nodes.size() == 3) {
                    // taking int x = 1 for example
                    //0 SimpleName: x
                    //1 IntegerLiteralExpr: 1
                    //2 PrimitiveType: int
                    access(node2list(nodes.get(1)));
                } // like int x， if the right expr is null?
                String type = jClass.searchTypeForField(nodes.get(nodes.size() - 1).toString()); // if inner class? X.Y a = b.c;
                this.variables.put(name, type);
                this.statements.add("VariableDeclarationExpr," + type + "," + name);
                continue;
            } else if ((base + "expr.FieldAccessExpr").equals(classStr)) { // don't need general field access expression? like person.name
                if ((base + "expr.ThisExpr").equals(node.getChildNodes().get(0).getClass().getTypeName())) {
                    // This.field
                    String name = childNodes.get(1).toString();
                    String type = fields.get(name);
                    this.statements.add("FieldAccessExpr," + type + "," + node.toString());
                } // add model like world.this   I don't known what it really means
                /*else{
                    access(node.getChildNodes());
                }*/
                continue;
            } else if ((base + "expr.NameExpr").equals(classStr)) {
                // name
                String name = node.toString();
//                String type = parameters.get(name);
                String type = this.jdk.get(name);
                if (this.imports.containsKey(name)) {
                    type = this.imports.get(name);
                }
                if (this.classes.containsKey(name)){
                    type = this.classes.get(name);
                }
                if (this.fields.containsKey(name)){
                    type = this.fields.get(name);
                }
                if (this.parameters.containsKey(name)) {
                    type = this.parameters.get(name);
                }
                if (this.variables.containsKey(name)) {
                    type = this.variables.get(name);
                }
                if (type == null) {
                    type = name;
                }
                this.statements.add("NameExpr," + type + "," + name);
                continue;
            } else if ((base + "expr.MethodCallExpr").equals(classStr)) {
                    // without packagenames, like f(a,b);
                    if ((base + "expr.SimpleName").equals(childNodes.get(0).getClass().getTypeName())){
                        String name = childNodes.get(0).toString();
                        String type = name;
                        if (methods.containsKey(type)) {
                            type = methods.get(type);
                        }
                        this.statements.add("MethodCallExpr," + type + "," + name + "()");
                        for (int idx = 1; idx < childNodes.size(); idx++) {
                            access(node2list(childNodes.get(idx)));
                        }
                    } else  {
                        // with packagenames like a.f(x,y);
                        Node left = childNodes.get(0);
                        Node right = childNodes.get(1);
                        String fullPath = getFullPackagePath(left.toString());
                        this.statements.add("MethodCallExpr," + fullPath + "," + right.toString() + "()");
                        for (int idx = 1; idx < childNodes.size(); idx++) {
                            access(node2list(childNodes.get(idx)));
                        }
                }
            } else if ((base + "expr.SimpleName").equals(classStr)) {
                String name = node.toString();
                this.statements.add("SimpleName," + name + "," + name);
                continue;
            } else if ((base + "expr.ObjectCreationExpr").equals(classStr)) {
                if (childNodes.size() > 1) {
                    for (int idx = 1; idx < childNodes.size(); idx++) {
                        access(node2list(childNodes.get(idx)));
                    }
                }
                String name = childNodes.get(0).toString();
                String type = jClass.searchTypeForField(name);
                statements.add("ObjectCreationExpr," + type + "," + name);
                continue;
            } else if ((base + "stmt.ForStmt").equals(classStr)) {
                statements.add("ForStmt,for,for");
                access(node.getChildNodes());
                statements.add("EndForStmt,end,end");
                continue;
            } else if ((base + "stmt.ForeachStmt").equals(classStr)) {
                statements.add("ForeachStmt,foreach, foreach");
                access(node.getChildNodes());
                statements.add("EndForeachStmt,end,end");
                continue;
            } else if ((base + "stmt.WhileStmt").equals(classStr)) {
                statements.add("WhileStmt,While");
                access(node.getChildNodes());
                statements.add("EndWhileStmt,end,end");
                continue;
            } else if ((base + "stmt.DoStmt").equals(classStr)) {
                statements.add("DoStmt,do,do");
                access(node.getChildNodes());
                statements.add("EndDoStmt,end,end");
                continue;
            } else if ((base + "stmt.IfStmt").equals(classStr)) {
                statements.add("IfStmt,if,if");
                access(node.getChildNodes());
                statements.add("EndIfStmt,end,end");
                continue;
            } else if ((base + "stmt.SwitchStmt").equals(classStr)) {
                statements.add("SwitchStmt,switch,switch");
                access(node.getChildNodes());
                statements.add("EndSwitchStmt,end,end");
                continue;
            } else if ((base + "stmt.SwitchEntryStmt").equals(classStr)) {
                statements.add("SwitchEntryStmt,case,case");
                access(node.getChildNodes());
                statements.add("EndSwitchEntryStmt,end,end");
                continue;
            } else if ((base + "stmt.BreakStmt").equals(classStr)) {
                access(node.getChildNodes());
                statements.add("BreakStmt,break,break");
                continue;
            } else if ((base + "stmt.ReturnStmt").equals(classStr)) {
                access(node.getChildNodes());
                statements.add("ReturnStmt,return,return");
                continue;
            }
            access(node.getChildNodes());
        }
    }


//    public String searchObjectByKey(String key, JObject jObject) {
//        String type = null;
//        if (jObject.getFields().containsKey(key)) {
//            type = jObject.getFields().get(key);
//        } else if (this.parameters.containsKey(key)) {
//            type = this.parameters.get(key);
//        } else if (this.variables.containsKey(key)) {
//            type = this.variables.get(key);
//        }
//        return type;
//    }

    public String searchTypeByObject(String type) {
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
            key = key.substring(0, max);
            if (parameters.containsKey(key)) {
                type = parameters.get(key) + type.substring(max);
            }
            if (variables.containsKey(key)) {
                type = parameters.get(key) + type.substring(max);
            }
        } else {
            if (parameters.containsKey(key)) {
                type = parameters.get(key);
            }
            if (variables.containsKey(key)) {
                type = variables.get(key);
            }
        }
        return type;
    }

//    private String parseCall(String call) {
//        String code = call.substring(0, call.indexOf("("));
//        String para = call.substring(call.indexOf("(") + 1, call.lastIndexOf(")"));
//        if (!para.trim().equals("")) {
//            String[] paras = para.split(",");
//            for (String s : paras) {
//                s = s.trim();
//                String type = jObject.getObjectByKey(s);
//                if (type != null) {
//                    code += "." + type;
//                } else {
//                    if (Pattern.compile("^-?\\d+$").matcher(s).find()) {
//                        code += ".int";
//                    } else if (Pattern.compile("^(-?\\d+)(\\.\\d+)?$").matcher(s).find()) {
//                        code += ".double";
//                    }
//                }
//            }
//        }
//        return code;
//    }

    private int countQuotation(String str) {
        int num = 0;
        char[] array = str.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (String.valueOf(array[i]).equals("\"")) {
                num++;
            }
        }
        return num;
    }

    private String deleteAllInCurves(String str) {
        boolean inCurve = false;
        String tmp = "";
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                inCurve = true;
                tmp += '(';
                continue;
            }
            if (str.charAt(i) == ')') {
                inCurve = false;
                tmp += ')';
                continue;
            }
            if (inCurve == false) {
                tmp += str.charAt(i);
            }
        }
        return tmp;
    }

    public List<String> getStatements() {
        return statements;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }

    private String getFullPackagePath(String path){
        String PathPrefixes = "";
        String firstPrefix = path.split("\\.")[0];
        if(this.jdk.containsKey(firstPrefix)){
            PathPrefixes = this.jdk.get(firstPrefix);
        }else if(this.classes.containsKey(firstPrefix)){
            PathPrefixes = this.classes.get(firstPrefix);
        }else if(this.imports.containsKey(firstPrefix)){
            PathPrefixes = this.imports.get(firstPrefix);
        }else if(this.variables.containsKey(firstPrefix)){
            PathPrefixes = this.variables.get(firstPrefix);
        }

        String subPath = path.substring(path.indexOf(".")+1);
        if(PathPrefixes ==""){
            return path;
        }
        return PathPrefixes + "." + subPath;
    }
}


