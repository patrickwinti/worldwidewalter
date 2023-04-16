package ch.zhaw.www.repository;

public abstract class FileReaderError extends RuntimeException {
    private static final String ERROR_MESSAGE = "The given file does not have the proper format, please check the format and try again";

    FileReaderError(String message) {
        super(message);
    }

    /**
     * Exception for when the loaded file does not have the expected format
     */
    public static class WrongFileFormatException extends FileReaderError {
        public WrongFileFormatException() {
            super(ERROR_MESSAGE);
        }
    }
}

