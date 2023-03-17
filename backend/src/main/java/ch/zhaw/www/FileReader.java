package ch.zhaw.www;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.util.List;

public interface FileReader {
    public List<Prompt> readFile(File file);
}
