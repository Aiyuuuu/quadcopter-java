import java.util.InputMismatchException;
import java.util.Scanner;

public class mainCLI {

    public static void main(String[] args) {
        QuadcopterSystem quadcopter = new QuadcopterSystem();
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            // Display the current state of the quadcopter
            displaySystemState(quadcopter);

            // Display menu options
            System.out.println("\nPlease choose an option:");
            System.out.println("1) Update Telemetry Variables (distance, altitude, batteryVoltage, signalLost)");
            System.out.println("2) Initialize System");
            System.out.println("3) Update Controls");
            System.out.println("4) Exit");

            int choice = getIntInput(scanner, "Enter your choice: ");

            try {
                switch (choice) {
                    case 1 -> updateTelemetryVariables(quadcopter, scanner);
                    case 2 -> {quadcopter.initializeSystem(); 
                        System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");}
                    case 3 -> {updateControls(quadcopter, scanner);
                          System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");}
                    case 4 -> exit = true;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Exiting program.");
        scanner.close();
    }

    // Method to display the current state of the quadcopter system
    private static void displaySystemState(QuadcopterSystem quadcopter) {
        System.out.println("\nCurrent Quadcopter System State:");

         // Telemetry Variables
         System.out.println("\n--- Telemetry Variables ---");
         System.out.printf("Distance: %d meters%n", quadcopter.distance);
         System.out.printf("Altitude: %d meters%n", quadcopter.altitude);
         System.out.printf("Battery Voltage: %.2f V%n", quadcopter.batteryVoltage);
         System.out.printf("Distance Display: %d meters%n", quadcopter.distanceDisplay);
         System.out.printf("Altitude Display: %d meters%n", quadcopter.altitudeDisplay);
         System.out.printf("Battery Percentage Display: %.2f%%%n", quadcopter.batteryPercentageDisplay);
    
        // Control Variables
        System.out.println("\n--- Control Variables ---");
        System.out.printf("Throttle Joystick Position: %d%%%n", quadcopter.throttleJoystickPosition);
        System.out.printf("Movement Joystick Direction: %s%n", quadcopter.movementJoystickDirection);
        System.out.printf("Left Yaw Button: %b%n", quadcopter.leftYawButton);
        System.out.printf("Right Yaw Button: %b%n", quadcopter.rightYawButton);
        System.out.println("Motor RPMs: " + quadcopter.motors);

        // System States
        System.out.println("\n--- System States ---");
        System.out.printf("System Started: %b%n", quadcopter.systemStarted);
        System.out.printf("Signal Lost: %b%n", quadcopter.signalLost);
        System.out.printf("Battery Low Warning: %b%n", quadcopter.batteryLowWarning);
        System.out.printf("Out of Range Warning: %b%n", quadcopter.outOfRangeWarning);
        System.out.printf("Video Data Available: %b%n", quadcopter.videoData);
    }

    // Method to update specific telemetry variables in the quadcopter system
    private static void updateTelemetryVariables(QuadcopterSystem quadcopter, Scanner scanner) {
        boolean updating = true;

        while (updating) {
            System.out.println("\nSelect a telemetry variable to update:");
            System.out.println("1) Distance");
            System.out.println("2) Altitude");
            System.out.println("3) Battery Voltage");
            System.out.println("4) Signal Lost");
            System.out.println("5) Done updating");

            int option = getIntInput(scanner, "Enter your choice: ");
            switch (option) {
                case 1 -> quadcopter.distance = getIntInput(scanner, "Enter new distance (meters): ");
                case 2 -> quadcopter.altitude = getIntInput(scanner, "Enter new altitude (meters): ");
                case 3 -> quadcopter.batteryVoltage = getDoubleInput(scanner, "Enter new battery voltage (V): ");
                case 4 -> quadcopter.signalLost = getBooleanInput(scanner, "Is the signal lost? (true/false): ");
                case 5 -> {
                    updating = false;
                    quadcopter.updateState(); // Automatically update state after telemetry updates
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }

        System.out.println("Telemetry variables updated successfully.");
        System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }

    // Method to update control variables in the quadcopter system
    private static void updateControls(QuadcopterSystem quadcopter, Scanner scanner) {
        boolean updating = true;

        while (updating) {
            System.out.println("\nSelect a control variable to update:");
            System.out.println("1) Throttle Joystick Position");
            System.out.println("2) Movement Joystick Direction");
            System.out.println("3) Left Yaw Button");
            System.out.println("4) Right Yaw Button");
            System.out.println("5) Done updating");

            int option = getIntInput(scanner, "Enter your choice: ");
            switch (option) {
                case 1 -> quadcopter.throttleJoystickPosition = getIntInput(scanner, "Enter throttle joystick position (0-100): ");
                case 2 -> {
                    System.out.println("Select movement joystick direction:");
                    System.out.println("1) FORWARD");
                    System.out.println("2) BACKWARD");
                    System.out.println("3) LEFT");
                    System.out.println("4) RIGHT");
                    System.out.println("5) NEUTRAL");

                    int directionChoice = getIntInput(scanner, "Enter choice for direction: ");
                    quadcopter.movementJoystickDirection = switch (directionChoice) {
                        case 1 -> MovementJoystickDirectionType.FORWARD;
                        case 2 -> MovementJoystickDirectionType.BACKWARD;
                        case 3 -> MovementJoystickDirectionType.LEFT;
                        case 4 -> MovementJoystickDirectionType.RIGHT;
                        default -> MovementJoystickDirectionType.NEUTRAL;
                    };
                }
                case 3 -> quadcopter.leftYawButton = getBooleanInput(scanner, "Is the left yaw button pressed? (true/false): ");
                case 4 -> quadcopter.rightYawButton = getBooleanInput(scanner, "Is the right yaw button pressed? (true/false): ");
                case 5 -> {
                    updating = false;
                    quadcopter.updateState(); // Automatically update state after control updates
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }

        System.out.println("Control variables updated successfully.");
        System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }

    // Utility method to safely get an integer input
    private static int getIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.next(); // Clear invalid input
            }
        }
    }

    // Utility method to safely get a double input
    private static double getDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Clear invalid input
            }
        }
    }

    // Utility method to safely get a boolean input
    private static boolean getBooleanInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return scanner.nextBoolean();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter true or false.");
                scanner.next(); // Clear invalid input
            }
        }
    }
}
