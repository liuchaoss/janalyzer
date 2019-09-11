package project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;


public class JOutput {
    // description
    private File fileComment;
    private File fileJavadoc;
    // raw code
    private File fileSourceCode;
    // API sequence
    private File fileParsedCode;
    // method name, parameters, returns
    private File fileMethod;
    // parameter
    private File fileParameter;
    // return
    private File fileReturn;
    private File fileModifiers;

    private String projectName;
    private String fileName;
    private File fileSource;

    public int cCmt = 0;
    public int cDoc = 0;
    public int cSrc = 0;
    public int cPrs = 0;
    public int cMet = 0;
    public int cPar = 0;
    public int cRet = 0;
    public int cMod = 0;
    public int cSce = 0;

    private File fileJDK;
    private HashMap<String, String> jdk;

    public JOutput(String path) {
        fileJDK = new File(path);
        try {
            if (fileJDK.exists()) {
                System.out.println(fileJDK.delete());
            }
            System.out.println(fileJDK.createNewFile());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public JOutput(String path, String projectName, HashMap<String, String> jdk) {
        this.jdk = jdk;
        this.projectName = projectName;
        String i = projectName.substring(0, projectName.length() - 4);
        fileComment = new File(path + "\\file_" + i + "_Comment.csv");
        fileJavadoc = new File(path + "\\file_" + i + "_Javadoc.csv");
        fileSourceCode = new File(path + "\\file_" + i + "_SourceCode.csv");
        fileSource = new File(path + "\\file_" + i + "_Source.csv");
        fileParsedCode = new File(path + "\\file_" + i + "_ParsedCode.csv");
        fileMethod = new File(path + "\\file_" + i + "_Method.csv");
        fileParameter = new File(path + "\\file_" + i + "_Parameter.csv");
        fileReturn = new File(path + "\\file_" + i + "_Return.csv");
        fileModifiers = new File(path + "\\file_" + i + "_Modifiers.csv");
        try {
            if (fileComment.exists()) {
                System.out.print(fileComment.delete());
            }
            if (fileJavadoc.exists()) {
                System.out.print(fileJavadoc.delete());
            }
            if (fileSourceCode.exists()) {
                System.out.print(fileSourceCode.delete());
            }
            if (fileSource.exists()) {
                System.out.print(fileSource.delete());
            }
            if (fileParsedCode.exists()) {
                System.out.print(fileParsedCode.delete());
            }
            if (fileMethod.exists()) {
                System.out.print(fileMethod.delete());
            }
            if (fileParameter.exists()) {
                System.out.print(fileParameter.delete());
            }
            if (fileReturn.exists()) {
                System.out.print(fileReturn.delete());
            }
            if (fileModifiers.exists()) {
                System.out.print(fileModifiers.delete());
            }
            System.out.print(fileComment.createNewFile());
            System.out.print(fileJavadoc.createNewFile());
            System.out.print(fileSourceCode.createNewFile());
            System.out.print(fileSource.createNewFile());
            System.out.print(fileParsedCode.createNewFile() +  "\r\n");
            System.out.print(fileMethod.createNewFile());
            System.out.print(fileParameter.createNewFile());
            System.out.print(fileModifiers.createNewFile());
            System.out.print(fileReturn.createNewFile() + "\r\n");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void write(File file, String line) {
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(osw);

            if (line == null) {
                return;
            }
            bw.write(line + "\r\n");

            bw.flush();
            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void write(File file, String line, int idx) {
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(idx + ";");
            if (line == null) {
                line = "[]";
            }
            bw.write(line + "\r\n");

            bw.flush();
            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void write(File file, List<String> lines, String delimiter, int idx) {
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(idx + ";");
            if (lines == null) {
                bw.write("[]\r\n");
            } else {
                if (lines.isEmpty()) {
                    bw.write("[]" + "\r\n");
                } else {
                    for (String line : lines) {
                        bw.write(line + delimiter);
                    }
                    bw.write("\r\n");
                }
            }

            bw.flush();
            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public File getFileComment() {
        return fileComment;
    }

    public void setFileComment(File fileComment) {
        this.fileComment = fileComment;
    }

    public File getFileJavadoc() {
        return fileJavadoc;
    }

    public void setFileJavadoc(File fileJavadoc) {
        this.fileJavadoc = fileJavadoc;
    }

    public File getFileSourceCode() {
        return fileSourceCode;
    }

    public void setFileSourceCode(File fileSourceCode) {
        this.fileSourceCode = fileSourceCode;
    }

    public File getFileParsedCode() {
        return fileParsedCode;
    }

    public void setFileParsedCode(File fileParsedCode) {
        this.fileParsedCode = fileParsedCode;
    }

    public File getFileMethod() {
        return fileMethod;
    }

    public void setFileMethod(File fileMethod) {
        this.fileMethod = fileMethod;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFileParameter() {
        return fileParameter;
    }

    public void setFileParameter(File fileParameter) {
        this.fileParameter = fileParameter;
    }

    public File getFileReturn() {
        return fileReturn;
    }

    public void setFileReturn(File fileReturn) {
        this.fileReturn = fileReturn;
    }

    public File getFileJDK() {
        return fileJDK;
    }

    public void setFileJDK(File fileJDK) {
        this.fileJDK = fileJDK;
    }

    public File getFileModifiers() {
        return fileModifiers;
    }

    public void setFileModifiers(File fileModifiers) {
        this.fileModifiers = fileModifiers;
    }

    public HashMap<String, String> getJdk() {
        return jdk;
    }

    public void setJdk(HashMap<String, String> jdk) {
        this.jdk = jdk;
    }

    public File getFileSource() {
        return fileSource;
    }

    public void setFileSource(File fileSource) {
        this.fileSource = fileSource;
    }
}
