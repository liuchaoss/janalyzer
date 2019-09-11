package project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.List;

import model.JClass;

public class JZipFolder {

    private String path;
    private List<JClass> files = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        JOutput jOutput = new JOutput("f:\\jdk.csv");
        JZipFolder zipFileReader = new JZipFolder("f:\\src.zip");
        zipFileReader.parse(jOutput);
    }

    public JZipFolder(String path) {
        this.path = path;
    }

    public void parse(JOutput jOutput) throws IOException {
        ZipFile zipFile = new ZipFile(path);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry != null) {
                if (entry.getName().endsWith(".java")) {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    String code = inputStream2str(inputStream);
                    this.files.add(new JClass(entry.getName(), code));
                }
            }
        }
        zipFile.close();

        for (JClass file : files) {
//            System.out.println(file.getClassName());
            file.parse(files, jOutput);
        }
    }

    private String inputStream2str(InputStream inputStream) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = inputStream.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        inputStream.close();
        return out.toString();
    }


    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public List<JClass> getFiles() {
        return files;
    }

    public void setFiles(List<JClass> files) {
        this.files = files;
    }
}
