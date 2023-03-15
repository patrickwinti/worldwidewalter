package ch.zhaw.www;

import java.io.File;
import java.util.HashMap;

public interface FileReader {
    public HashMap<String, String> readFile(File file);
}
