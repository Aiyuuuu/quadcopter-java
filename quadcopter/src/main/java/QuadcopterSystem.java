import java.util.HashMap;
import java.util.Map;

enum MovementJoystickDirectionType {
    FORWARD, BACKWARD, LEFT, RIGHT, NEUTRAL}

enum Signal {
    CONTROLSIGNAL, VIDEOSIGNAL, TELEMETRYSIGNAL}

public class QuadcopterSystem {

    // Constants for safety thresholds
    public static final double MIN_SAFE_BATTERY_PERCENTAGE = 10.0;
    public static final int MAX_SAFE_DISTANCE = 2000;
    public static final double MIN_BATTERY_VOLTAGE = 9.0;
    public static final double MAX_BATTERY_VOLTAGE = 12.6;
    public static final int MAX_RPM = 12000;
    public static final int TARGET_TILT_RPM = 800;
    public static final int TARGET_YAW_RPM = 300;
    public static final int STARTING_DISTANCE = 10;
    public static final int STARTING_ALTITUDE = 0;
    public static final double STARTING_VOLTAGE = 12.0;

    // System state variables
    public int throttleJoystickPosition;
    public MovementJoystickDirectionType movementJoystickDirection;
    public boolean leftYawButton;
    public boolean rightYawButton;
    public Integer distanceDisplay;
    public Integer altitudeDisplay;
    public Double batteryPercentageDisplay;
    public Boolean LEDvideoDisplay;
    public Boolean batteryLowWarning;
    public Boolean outOfRangeWarning;
    public int distance;
    public int altitude;
    public double batteryVoltage;
    public boolean videoData;
    public boolean signalLost;
    public Map<Integer, Integer> motors;
    public boolean systemStarted;

    // Constructor to initialize the system with starting values
    public QuadcopterSystem() {
        throttleJoystickPosition = 0;
        movementJoystickDirection = MovementJoystickDirectionType.NEUTRAL;
        leftYawButton = false;
        rightYawButton = false;
        distance = STARTING_DISTANCE;
        altitude = STARTING_ALTITUDE;
        batteryVoltage = STARTING_VOLTAGE;
        videoData = false;
        signalLost = false;
        systemStarted = false;

        motors = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            motors.put(i, 0);
        }
    }

    // Helper method to validate throttle joystick position range
    public boolean isThrottleJoystickPositionInRange(int position) {
        return position >= 0 && position <= 100;
    }

    // Helper method to validate battery voltage range
    public boolean isBatteryVoltageInRange(double voltage) {
        return voltage >= MIN_BATTERY_VOLTAGE && voltage <= MAX_BATTERY_VOLTAGE;
    }

    // Helper method to validate distance range
    public boolean isDistanceInRange(int dist) {
        return dist >= 0 && dist <= MAX_SAFE_DISTANCE;
    }

    // Helper method to validate RPM range for motors
    public boolean areMotorsRPMsInRange() {
        return motors.values().stream().allMatch(rpm -> rpm >= 0 && rpm <= MAX_RPM);
    }

    // Calculate throttle RPM based on joystick position
    public int calculateThrottleRPM(int throttlePosition) {
        if (!isThrottleJoystickPositionInRange(throttlePosition)) {
            throw new IllegalArgumentException("Throttle joystick position out of range");
        }
        int allowedThrottleRPM = MAX_RPM - TARGET_TILT_RPM;
        return (int) ((allowedThrottleRPM - 800) * (throttlePosition / 100.0)) + 800;
    }

    // Getter for RPM of specific motor
    public int getRPM(int motorId) {
        return motors.getOrDefault(motorId, 0);
    }

    // Setter for RPM of specific motor with post-condition check
    public void setRPM(int motorId, int rpm) {
        if (!systemStarted) throw new IllegalStateException("System not started");
        if (rpm < 0 || rpm > MAX_RPM) throw new IllegalArgumentException("RPM out of range");
        motors.put(motorId, rpm);
        assert motors.get(motorId) == rpm : "Failed to set RPM correctly";
    }

    // Set RPM for all motors with post-condition check
    public void setAllRPMs(int rpm) {
        if (!systemStarted) throw new IllegalStateException("System not started");
        if (rpm < 0 || rpm > MAX_RPM) throw new IllegalArgumentException("RPM out of range");
        motors.replaceAll((id, oldRpm) -> rpm);
        assert motors.values().stream().allMatch(val -> val == rpm) : "Failed to set all RPMs correctly";
    }

    // Initialize system with pre- and post-condition checks
    public void initializeSystem() {
        if (systemStarted) throw new IllegalStateException("System is already started");
        if (!isThrottleJoystickPositionInRange(throttleJoystickPosition) || movementJoystickDirection != MovementJoystickDirectionType.NEUTRAL
                || leftYawButton || rightYawButton || !isDistanceInRange(distance) || altitude != 0
                || !isBatteryVoltageInRange(batteryVoltage) || signalLost || !areMotorsRPMsInRange()) {
            throw new IllegalStateException("System cannot be initialized with the current state.");
        }
        systemStarted = true;
        setAllRPMs(800);
        updateState();
        assert systemStarted && motors.values().stream().allMatch(rpm -> rpm == 800) : "System initialization failed";
    }

    // Decode and process telemetry signal
    public void decodeTelemetrySignal() {
        if (!systemStarted) throw new IllegalStateException("System not started");
        if (distance<0) throw new IllegalStateException("Distance cannot be negative");
        if (altitude<0) throw new IllegalStateException("Altitude cannot be negative");
        batteryPercentageDisplay = Math.floor(
                (((batteryVoltage - MIN_BATTERY_VOLTAGE) / (MAX_BATTERY_VOLTAGE - MIN_BATTERY_VOLTAGE)) * 100) * 100 + 0.5) / 100;
        distanceDisplay = distance;
        altitudeDisplay = altitude;
        batteryLowWarning = batteryPercentageDisplay <= MIN_SAFE_BATTERY_PERCENTAGE;
        outOfRangeWarning = distance >= MAX_SAFE_DISTANCE;
    }

    // Control motors based on throttle and movement direction
    public void baseControl() {
        if (!systemStarted) throw new IllegalStateException("System not started");
        setAllRPMs(calculateThrottleRPM(throttleJoystickPosition));

        // Tilt motors based on direction
        switch (movementJoystickDirection) {
            case FORWARD -> {
                setRPM(3, getRPM(3) + TARGET_TILT_RPM);
                setRPM(4, getRPM(4) + TARGET_TILT_RPM);
            }
            case BACKWARD -> {
                setRPM(1, getRPM(1) + TARGET_TILT_RPM);
                setRPM(2, getRPM(2) + TARGET_TILT_RPM);
            }
            case LEFT -> {
                setRPM(2, getRPM(2) + TARGET_TILT_RPM);
                setRPM(3, getRPM(3) + TARGET_TILT_RPM);
            }
            case RIGHT -> {
                setRPM(1, getRPM(1) + TARGET_TILT_RPM);
                setRPM(4, getRPM(4) + TARGET_TILT_RPM);
            }
            default -> {}
        }
    }

    // Control yaw by adjusting motor RPM
    public void yawControl() {
        if (!systemStarted) throw new IllegalStateException("System not started");

        if (rightYawButton && !leftYawButton) {
            setRPM(2, getRPM(2) - TARGET_YAW_RPM);
            setRPM(4, getRPM(4) - TARGET_YAW_RPM);
        } else if (leftYawButton && !rightYawButton) {
            setRPM(1, getRPM(1) - TARGET_YAW_RPM);
            setRPM(3, getRPM(3) - TARGET_YAW_RPM);
        }
    }

    // Update the system state
    public void updateState() {
        if (!systemStarted) throw new IllegalStateException("System not started");

        if (signalLost || batteryVoltage <= MIN_BATTERY_VOLTAGE || batteryVoltage > MAX_BATTERY_VOLTAGE) {
            setAllRPMs(0);
            systemStarted = false;
            videoData = false;
        } else {
            videoData = true;
            baseControl();
            yawControl();
            decodeTelemetrySignal();
        }
    }
}
