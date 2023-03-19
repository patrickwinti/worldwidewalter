package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.util.List;

/**
 * Interface to read and parse the "WALTER" prompts from a file
 */
public interface FileReader {
    List<Prompt> readFile (File file) throws Exception;
}

