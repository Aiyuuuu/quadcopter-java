import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuadcopterSystemTest {

    private QuadcopterSystem quadcopter;

    @BeforeEach
    void setUp() {
        // Create a new instance of QuadcopterSystem before each test
        quadcopter = new QuadcopterSystem();
    }

    @Test
    void testInitializeSystem() {
        // Test successful initialization
        assertDoesNotThrow(() -> quadcopter.initializeSystem());
        assertTrue(quadcopter.systemStarted, "System should be started after initialization.");
        quadcopter.motors.values().forEach(rpm -> assertEquals(800, rpm, 
        "Motor RPM should be set to 800 after initialization."));
    }

    @Test
    void testInitializeSystemAlreadyStarted() {
        // Test attempting to initialize an already started system
        quadcopter.initializeSystem();
        assertThrows(IllegalStateException.class, () -> quadcopter.initializeSystem(),
         "Initializing an already started system should throw an exception.");
    }

    @Test
    void testInitializeSystemAtLowBattery() {
        // Test attempting to initialize an already started system
        quadcopter.batteryVoltage = 8.5;
        assertThrows(IllegalStateException.class, () -> quadcopter.initializeSystem(), 
        "Initializing at low battery should throw an exception");
    }

    @Test
    void testInitializeSystemAtAltitude() {
        // Test attempting to initialize an already started system
        quadcopter.altitude = 100;
        assertThrows(IllegalStateException.class, () -> quadcopter.initializeSystem(), 
        "Initializing at an altitude should throw an exception");
    }

    @Test
    void testSetRPMOutOfRange() {
        // Test setting RPM outside valid range
        quadcopter.initializeSystem();
        assertThrows(IllegalArgumentException.class, () -> quadcopter.setRPM(1, 13000), 
        "Setting RPM above MAX_RPM should throw an exception.");
    }

    @Test
    void testSetAllRPMsOutOfRange() {
        // Test setting RPM outside valid range
        quadcopter.initializeSystem();
        assertThrows(IllegalArgumentException.class, () -> quadcopter.setAllRPMs(13000), 
        "Setting RPMs above MAX_RPM should throw an exception.");
    }
   
    @Test
    void testBatteryLowWarning() {
        // Test low battery warning based on voltage level
        quadcopter.initializeSystem();
        quadcopter.batteryVoltage = 9.2;
        quadcopter.decodeTelemetrySignal();
        assertTrue(quadcopter.batteryLowWarning, 
        "Battery low warning should be triggered when voltage is below threshold.");
    }

    @Test
    void testOutOfRangeWarning() {
        // Test out-of-range warning based on distance
        quadcopter.initializeSystem();
        quadcopter.distance = 2500; // Beyond MAX_SAFE_DISTANCE
        quadcopter.decodeTelemetrySignal();
        assertTrue(quadcopter.outOfRangeWarning, 
        "Out of range warning should be triggered when distance exceeds MAX_SAFE_DISTANCE.");
    }

    @Test
    void testNegativeTelemetryValues() {
        // Test out-of-range warning based on distance
        quadcopter.initializeSystem();
        quadcopter.distance = -40; // Beyond MAX_SAFE_DISTANCE
        assertThrows(IllegalStateException.class, () -> quadcopter.decodeTelemetrySignal(), 
        "Setting negative telemetry values should throw an exception.");
        quadcopter.distance = 10;
        quadcopter.altitude = -18;
        assertThrows(IllegalStateException.class, () -> quadcopter.decodeTelemetrySignal(), 
        "Setting negative telemetry values should throw an exception.");
    }

    @Test
    void testThrottleControl() {
        // Test motor RPMs with forward movement control
        quadcopter.initializeSystem();
        quadcopter.throttleJoystickPosition = 60;
        quadcopter.baseControl();
        int allowedThrottleRPM = QuadcopterSystem.MAX_RPM - QuadcopterSystem.TARGET_TILT_RPM;
        double RPM = ((allowedThrottleRPM - 800) * (quadcopter.throttleJoystickPosition / 100.0)) + 800;

        assertEquals(RPM, quadcopter.getRPM(1), "Motor 1 RPM should match calculated throttle RPM.");
        assertEquals(RPM, quadcopter.getRPM(2), "Motor 2 RPM should match calculated throttle RPM.");
        assertEquals(RPM, quadcopter.getRPM(3), "Motor 3 RPM should match calculated throttle RPM.");
        assertEquals(RPM, quadcopter.getRPM(4), "Motor 4 RPM should match calculated throttle RPM.");
    }

    @Test
    void testBaseMovement() {
        // Test motor RPMs with forward movement control
        quadcopter.initializeSystem();
        quadcopter.movementJoystickDirection = MovementJoystickDirectionType.FORWARD;
        quadcopter.baseControl();
        assertEquals(800, quadcopter.getRPM(1), "Motor 1 RPM should match calculated throttle RPM.");
        assertEquals(800, quadcopter.getRPM(2), "Motor 2 RPM should match calculated throttle RPM.");
        assertEquals(800+QuadcopterSystem.TARGET_TILT_RPM, quadcopter.getRPM(3), 
        "Motor 3 RPM should be throttle + tilt RPM for forward.");
        assertEquals(800+QuadcopterSystem.TARGET_TILT_RPM, quadcopter.getRPM(4), 
        "Motor 4 RPM should be throttle + tilt RPM for forward.");
    }


    @Test
    void testYawControl() {
        // Test yaw control for left yaw button press
        quadcopter.initializeSystem();
        quadcopter.leftYawButton = true;
        quadcopter.yawControl();

        assertEquals(800-QuadcopterSystem.TARGET_YAW_RPM, quadcopter.getRPM(1),
         "Motor 1 RPM should decrease by TARGET_YAW_RPM for left yaw.");
        assertEquals(800, quadcopter.getRPM(2), "Motor 2 RPM should remain unchanged.");
        assertEquals(800-QuadcopterSystem.TARGET_YAW_RPM, quadcopter.getRPM(3), 
        "Motor 3 RPM should decrease by TARGET_YAW_RPM for left yaw.");
        assertEquals(800, quadcopter.getRPM(4), "Motor 4 RPM should remain unchanged.");
    }

    @Test
    void testSignalLost() {
        // Test system shutdown when signal is lost
        quadcopter.initializeSystem();
        quadcopter.signalLost = true;
        quadcopter.updateState();

        assertFalse(quadcopter.systemStarted, "System should shut down if signal is lost.");
        assertEquals(0, quadcopter.getRPM(1), "All motors should stop if signal is lost.");
        assertEquals(0, quadcopter.getRPM(2), "All motors should stop if signal is lost.");
        assertEquals(0, quadcopter.getRPM(3), "All motors should stop if signal is lost.");
        assertEquals(0, quadcopter.getRPM(4), "All motors should stop if signal is lost.");
    }

    @Test
    void testBatteryDead() {
        // Test system shutdown when signal is lost
        quadcopter.initializeSystem();
        quadcopter.batteryVoltage = 8;
        quadcopter.updateState();

        assertFalse(quadcopter.systemStarted, "System should shut down if battery dies.");
        assertEquals(0, quadcopter.getRPM(1), "All motors should stop if battery dies.");
        assertEquals(0, quadcopter.getRPM(2), "All motors should stop if battery dies.");
        assertEquals(0, quadcopter.getRPM(3), "All motors should stop if battery dies.");
        assertEquals(0, quadcopter.getRPM(4), "All motors should stop if battery diest.");
    }
}
