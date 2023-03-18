package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.util.List;

public interface FileReader {
    List<Prompt> readFile(File file);
}

