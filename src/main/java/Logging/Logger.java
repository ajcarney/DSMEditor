package Logging;

public class Logger {

    public enum LOGGER_PRIORITY {
        CATASTROPHIC,
        HIGH,
        MEDIUM,
        LOW
    }


    public static void submitException(Exception e, LOGGER_PRIORITY priority) {
        switch (priority) {
            case CATASTROPHIC -> {
                System.err.println("A Catastrophic Error Occurred");
                e.printStackTrace();
            }
            case HIGH -> {
                System.err.println("A High Priority Error Occurred");
                e.printStackTrace();
            }
            case MEDIUM -> {
                System.err.println("A Medium Priority Error Occurred");
                e.printStackTrace();
            }
            case LOW -> {
                System.err.println("A Low Error Occurred");
                e.printStackTrace();
            }
        }
    }

}
