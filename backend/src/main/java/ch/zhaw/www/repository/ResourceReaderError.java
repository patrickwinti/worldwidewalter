package ch.zhaw.www.repository;

public abstract class ResourceReaderError extends RuntimeException {
    private static final String ERROR_MESSAGE = "The given resource does not have the proper format, please check the format and try again";
    
    ResourceReaderError(String message) {
        super(message);
    }
    
    /**
     * Exception for when the loaded file does not have the expected format
     */
    public static class WrongResourceFormatException extends ResourceReaderError {
        public WrongResourceFormatException() {
            super(ERROR_MESSAGE);
        }
    }
}

