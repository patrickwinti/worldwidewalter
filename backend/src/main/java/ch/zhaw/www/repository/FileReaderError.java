package ch.zhaw.www.repository;

public abstract class FileReaderError extends RuntimeException {
    FileReaderError(String message) {
        super(message);
    }

    /**
     * Exception for when the requested deck could not be found
     */
    public static class DeckNotFoundError extends FileReaderError {
        public DeckNotFoundError() {
            super("Deck not found");
        }
    }

    /**
     * Exception for when the loaded file does not have the expected format
     */
    public static class WrongFileFormatError extends FileReaderError {
        public WrongFileFormatError() {
            super("The given file does not have the proper format, please check the format ans try again");
        }
    }

    /**
     * Exception for when a statement could not find a WALTER placeholder match
     */

    public static class NoPlaceholderError extends FileReaderError {
        /**
         * @param line line where the error occurred
         */
        public NoPlaceholderError(String line) {
            super(String.format("No WALTER placeholder for line = %s could be found", line));
        }
    }
}

