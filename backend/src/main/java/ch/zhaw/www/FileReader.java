package ch.zhaw.www;

import ch.zhaw.www.model.Prompt;

import java.nio.file.Path;
import java.util.List;

public interface FileReader {
    public List<Prompt> readFile(Path filePath);
}
