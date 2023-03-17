package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.nio.file.Path;
import java.util.List;

public interface FileReader {
    List<Prompt> readFile(Path filePath);
}

