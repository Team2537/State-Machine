package frc.robot.subsystems

import com.revrobotics.*
import com.revrobotics.CANSparkBase.IdleMode
import edu.wpi.first.networktables.GenericEntry
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.Constants
import lib.states.AbstractStateSubsystem
import lib.states.State
import lib.states.StateEvent
import near

object LauncherSubsystem : AbstractStateSubsystem<LauncherSubsystem>() {
    private val leftLauncher: CANSparkFlex = CANSparkFlex(Constants.LAUNCHER_CONSTANTS.LEFT_LAUNCHER_PORT, CANSparkLowLevel.MotorType.kBrushless)
    private val rightLauncher: CANSparkFlex = CANSparkFlex(Constants.LAUNCHER_CONSTANTS.RIGHT_LAUNCHER_PORT, CANSparkLowLevel.MotorType.kBrushless)

    private val rollerMotor: CANSparkMax = CANSparkMax(Constants.LAUNCHER_CONSTANTS.ROLLER_MOTOR_PORT, CANSparkLowLevel.MotorType.kBrushless)

    private val rollerPID: SparkPIDController = rollerMotor.pidController

    private val tab = Shuffleboard.getTab("Test")
    private var positionEntry: GenericEntry = tab.add("Roller Position", 0.0).getEntry()

    private val rollerP: GenericEntry = tab.add("Roller P", 0.0).entry
    private val rollerI: GenericEntry = tab.add("Roller I", 0.0).entry
    private val rollerD: GenericEntry = tab.add("Roller D", 0.0).entry

    private var kP: Double = 0.025
    private var kI: Double = 0.0
    private var kD: Double = 0.0

    private var storePosition = getRollerPosition()


    private val noteDetector: Trigger

    private val noteTimer: Timer = Timer()

    var fire: Boolean = false

//    lateinit var EMPTY: State<LauncherSubsystem>
    lateinit var STORED: State<LauncherSubsystem>
    lateinit var PRIMED: State<LauncherSubsystem>
    lateinit var AT_SPEED: State<LauncherSubsystem>
    lateinit var FIRING: State<LauncherSubsystem>

    val emptyTrigger = trigger(this["empty"])
    val storedTrigger = trigger(STORED)
    val primedTrigger = trigger(PRIMED)
    val atSpeedTrigger = trigger(AT_SPEED)
    val firingTrigger = trigger(FIRING)

    init {
        leftLauncher.restoreFactoryDefaults()
        rightLauncher.restoreFactoryDefaults()
        rollerMotor.restoreFactoryDefaults()

        rightLauncher.follow(leftLauncher, true)

        configureEncoders()
        configurePID()

        leftLauncher.setIdleMode(CANSparkBase.IdleMode.kBrake)
        rightLauncher.setIdleMode(CANSparkBase.IdleMode.kBrake)
        rollerMotor.setIdleMode(CANSparkBase.IdleMode.kBrake)

        tab.addDouble("Roller Encoder Pos") { rollerMotor.encoder.position }
        tab.addDouble("Roller Velocity") { rollerMotor.encoder.velocity }
        tab.addDouble("Target Position") { storePosition }

        val irSensor = DigitalInput(Constants.LAUNCHER_CONSTANTS.INFRARED_SENSOR)
        noteDetector = Trigger { irSensor.get() }
        tab.addBoolean("IR Sensor") {irSensor.get()}
        tab.addString(  "State") { state.toString() }

        noteTimer.start()

        makeState("empty") { // EMPTY
            stop() // TODO: Maybe seperate?
            rollerPID.p = 0.0001
            rollerPID.i = 0.000001

            // Both `this["foo"]` and `get("bar")` work the same
            if(!noteDetector.asBoolean){
                return@makeState this["stored"]
            }
            return@makeState get("stored")
        }

        STORED = makeState("stored") { // STORED
            if(noteDetector.asBoolean){
                return@makeState get("empty")
            } else if (!noteDetector.asBoolean && storePosition.near(getRollerPosition(), 0.2) && inZone()){
                PRIMED
            }
            rollerPID.p = 0.0001
            rollerPID.i = 0.000001 // TODO: Tune?
            STORED

        }

        PRIMED = object : State<LauncherSubsystem> { // PRIMED
            override fun next(context: LauncherSubsystem): State<LauncherSubsystem> {
                if (getLauncherVelocity() > 6000 && leftLauncher.encoder.velocity > 6000) {
                    return AT_SPEED
                } else if (noteDetector.asBoolean) {
                    return get("empty")
                }
                return this
            }
        }

        AT_SPEED = object : State<LauncherSubsystem> { // AT_SPEED
            override fun next(context: LauncherSubsystem): State<LauncherSubsystem> {
                if(fire){
                    fire = false
                    return FIRING
                }
                return this
            }
        }
        FIRING = object : State<LauncherSubsystem> { // FIRING
            override fun next(context: LauncherSubsystem): State<LauncherSubsystem> {
                if(noteTimer.hasElapsed(0.5)){
                    return get("empty")
                }
                return this
            }
        }
        setInitialState(get("empty"))
    }

    override val triggers: Set<Trigger>
        get() = setOf(
                this["empty"].toTrigger(this),
                STORED.toTrigger(this),
                PRIMED.toTrigger(this),
                AT_SPEED.toTrigger(this),
                FIRING.toTrigger(this),
            )


    private fun configureEncoders() {
        leftLauncher.encoder.positionConversionFactor = 1.0
        leftLauncher.encoder.velocityConversionFactor = 1.0

        rightLauncher.encoder.positionConversionFactor = 1.0
        rightLauncher.encoder.velocityConversionFactor = 1.0

        rollerMotor.encoder.positionConversionFactor = 1.0
        rollerMotor.encoder.velocityConversionFactor = 1.0
    }

    private fun configurePID(){
        rollerPID.p = 0.0004
        rollerPID.i = 0.000004
        rollerPID.d = kD

        rollerPID.setSmartMotionAccelStrategy(SparkPIDController.AccelStrategy.kTrapezoidal, 0)
        rollerPID.setSmartMotionMaxVelocity(240.0, 0)
        rollerPID.setSmartMotionMinOutputVelocity(0.0, 0)
        rollerPID.setSmartMotionAllowedClosedLoopError(0.05, 0)
        rollerPID.setSmartMotionMaxAccel(1000.0, 0)
    }

    fun setLauncherSpeed(speed: Double) {
        leftLauncher.set(speed)
    }

    fun getLauncherVelocity(): Double {
        return leftLauncher.encoder.velocity
    }

    fun setRollerSpeed(speed: Double) {
        rollerMotor.set(speed)
    }

    fun stop() {
        setRollerMode(IdleMode.kBrake)

        leftLauncher.set(0.0)
        rollerMotor.set(0.0)
    }

    fun setRollerMode(idleMode: IdleMode){
        rollerMotor.setIdleMode(idleMode)
    }

    fun brakeRoller(){}

    fun intake(){
        storePosition = getRollerPosition() - 1
        println("Intake ran, setting to $storePosition")
        rollerPID.setReference(storePosition, CANSparkBase.ControlType.kSmartMotion)
    }

    fun getRollerPosition(): Double {
        return rollerMotor.encoder.position
    }

    // Seperate state decision making, and checking
//    fun updateState(){
//        if(state == State.EMPTY){
//            if(!noteDetector.asBoolean){
//                state = State.STORED
//                rollerPID.p = 0.0004
//                rollerPID.i = 0.000004
//                storePosition = getRollerPosition() - 1
//                rollerPID.setReference(storePosition, CANSparkBase.ControlType.kSmartMotion)
//            }
//        } else {
//            if(noteDetector.asBoolean){
//                state = State.EMPTY
//                rollerPID.p = 0.0001
//                rollerPID.i = 0.000001
//                rollerMotor.set(0.0)
//            }
//        }
//    }

    // stub code
    fun inZone(): Boolean {
        return true
    }

    override fun onEvent(event: StateEvent) {
        if(event.subsystem is LauncherSubsystem){
            // blah blah blah
        }
    }

    override fun periodic() {
//        val p = rollerP.getDouble(0.025)
//        val i = rollerI.getDouble(0.025)
//        val d = rollerD.getDouble(0.025)
//        val e = 0.000001
//        if(!kP.near(p, e)) {
//            kP = p
//            println("Pid Changed kP: ${kP}; rollerPID.p: ${rollerPID.p}")
//        }
//
//        if (!kI.near(i, e))
//        {
//            kI = i
//            println("Pid Changed kI: ${kI}; rollerPID.i: ${rollerPID.i}")
//
//        }
//
//        if (!kD.near(d, e))
//        {
//            kD = d
//            println("Pid Changed kD: ${kD}; rollerPID.d: ${rollerPID.d}")
//
//        }
//
//        rollerPID.p = kP
//        rollerPID.i = kI
//        rollerPID.d = kD

        if(!noteDetector.asBoolean){
            noteTimer.reset()
        }


//        if (abs(rollerMotor.encoder.position - storePosition) < .2) {
//            rollerMotor.set(0.0)
//        } else {
//            if (state != State.EMPTY) {
//                rollerPID.setReference(storePosition, CANSparkBase.ControlType.kSmartMotion)
//            }
//        }

        updateState()
    }

    /**
     * Checks if this subsystem is in its terminal state
     */
    override val isTerminal: Boolean
        get() = false

    /**
     * Checks if this subsystem has a possible terminal state
     */
    override val hasTerminal: Boolean
        get() = false
}