package project;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JZipProject {
    private List<File> list = new ArrayList<>();
    private String path;
    private HashMap<String, String> jdk = new HashMap<>();

    public JZipProject(String folder_path, String path, String path_jdk) {
        this.path = path;
        readFile(path_jdk);
        System.out.println(jdk);
        System.out.println(jdk.size());
        findFiles(folder_path, "*.zip", list);
    }

    private void readFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
            String line = "";
            line = br.readLine();
            while (line != null) {
                line = br.readLine();
                if (line != null){
                    String[] tokens = line.split(",");
                    jdk.put(tokens[1], tokens[0]);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse(int idx_start, int idx_end) throws IOException {
        for (int i = idx_start; i < idx_end; i++) {
            File file = list.get(i);
            JOutput jOutput = new JOutput(this.path, file.getName(), jdk);
            JZipFolder jZipFolder = new JZipFolder(file.getAbsolutePath());
            jZipFolder.parse(jOutput);
        }
    }

    private void findFiles(String path, String name, List<File> list) {
        String tempName;
        File baseDir = new File(path);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            System.out.println("failed:" + path + "not a directory!");
        } else {
            String[] file_list = baseDir.list();
            if (file_list != null) {
                for (String str : file_list) {
                    File read_file = new File(path + "//" + str);
                    if (!read_file.isDirectory()) {
                        tempName = read_file.getName();
                        if (wildcardMatch(name, tempName)) {
                            list.add(read_file.getAbsoluteFile());
                        }
                    } else {
                        findFiles(path + "//" + str, name, list);
                    }
                }
            }
        }
    }

    private boolean wildcardMatch(String pattern, String str) {
        int patternLength = pattern.length();
        int strLength = str.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                while (strIndex < strLength) {
                    if (wildcardMatch(pattern.substring(patternIndex + 1),
                            str.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else if (ch == '?') {
                strIndex++;
                if (strIndex > strLength) {
                    return false;
                }
            } else {
                if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return (strIndex == strLength);
    }

    public List<File> getList() {
        return list;
    }

    public void setList(List<File> list) {
        this.list = list;
    }
}
