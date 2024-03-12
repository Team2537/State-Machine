package frc.robot

import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import edu.wpi.first.wpilibj2.command.button.Trigger
import frc.robot.Constants.OperatorConstants
import frc.robot.commands.*
import frc.robot.subsystems.LauncherSubsystem
import lib.states.StateChangeEvent
import lib.states.StateEvent
import lib.states.StateEventListener
import lib.states.stateTrigger

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the [Robot]
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 *
 * In Kotlin, it is recommended that all your Subsystems are Kotlin objects. As such, there
 * can only ever be a single instance. This eliminates the need to create reference variables
 * to the various subsystems in this container to pass into to commands. The commands can just
 * directly reference the (single instance of the) object.
 */
object RobotContainer : StateEventListener
{

    private val driverController = CommandXboxController(OperatorConstants.DRIVER_CONTROLLER_PORT)

    private val intakeCommand: IntakeCommand = IntakeCommand()


    // This looks cleaner to me, but it also feels kind of off
    val storedTrigger: Trigger = LauncherSubsystem.storedTrigger
    val primedTrigger: Trigger = LauncherSubsystem.primedTrigger
    val emptyTrigger: Trigger = LauncherSubsystem.emptyTrigger
    val atSpeedTrigger: Trigger = LauncherSubsystem.atSpeedTrigger
    val switchToFiring: Trigger = atSpeedTrigger.and { driverController.hid.rightBumper }
    val firingTrigger: Trigger = LauncherSubsystem.firingTrigger


    init
    {
        configureBindings()
        // Reference the Autos object so that it is initialized, placing the chooser on the dashboard
        Autos
    }

    // Replace with CommandPS4Controller or CommandJoystick if needed

    /**
     * Use this method to define your `trigger->command` mappings. Triggers can be created via the
     * [Trigger] constructor that takes a [BooleanSupplier][java.util.function.BooleanSupplier]
     * with an arbitrary predicate, or via the named factories in [GenericHID][edu.wpi.first.wpilibj2.command.button.CommandGenericHID]
     * subclasses such for [Xbox][CommandXboxController]/[PS4][edu.wpi.first.wpilibj2.command.button.CommandPS4Controller]
     * controllers or [Flight joysticks][edu.wpi.first.wpilibj2.command.button.CommandJoystick].
     */
    private fun configureBindings()
    {
        storedTrigger.whileTrue(intakeCommand)
        primedTrigger.whileTrue(PrimeLauncherCommand())
        switchToFiring.onTrue(FireLauncherCommand())
        firingTrigger.whileTrue(FireCommand())
    }

    // DON'T DO IT THIS WAY
    // IT'S REALLY DUMB
    override fun receive(event: StateEvent) {
//        if(event is StateChangeEvent<*>){
//            if(event.subsystem is LauncherSubsystem){
//                if(event.fromState == event.subsystem.STORED){
//                    intakeCommand.schedule()
//                }
//            }
//        }
    }
}