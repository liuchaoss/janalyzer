import java.io.IOException;
import project.JZipProject;

public class Main {
    public static void main(String[] args) throws IOException{
        JZipProject jZipProject = new JZipProject(
                "..\\codematcher\\data\\codebase_java\\",
                "..\\codematcher\\data\\jacoma_janalyzer\\",
                "jdk.txt");
        jZipProject.parse(0, jZipProject.getList().size());
    }
}
