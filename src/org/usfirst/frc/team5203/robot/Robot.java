/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5203.robot;

import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class. The
 * SampleRobot class is the base of a robot application that will automatically
 * call your Autonomous and OperatorControl methods at the right time as
 * controlled by the switches on the driver station or the field controls.
 *
 * <p>The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 *
 * <p>WARNING: While it may look like a good choice to use for your code if
 * you're inexperienced, don't. Unless you know what you are doing, complex code
 * will be much more difficult under this system. Use IterativeRobot or
 * Command-Based instead if you're new.
 */
@SuppressWarnings("deprecation")
public class Robot extends SampleRobot {
	//Talon SRX motors, the number 1,2,3,4, or 5, refer to the id for the motor, given to in in the web based configuration
	WPI_TalonSRX frontLeft = new WPI_TalonSRX(1);
	WPI_TalonSRX rearLeft = new WPI_TalonSRX(2);
	WPI_TalonSRX frontRight = new WPI_TalonSRX(3);
	WPI_TalonSRX rearRight = new WPI_TalonSRX(4);
	WPI_TalonSRX claw = new WPI_TalonSRX(5);
	//The two  climber motor controllers used to power the winch that raises the robot
	Spark climber1 = new Spark(0);
	Spark climber2 = new Spark(1);
	//Gyro used for giving direction of the robot, useful for things like turning in autonomous
	ADXRS450_Gyro gyro = new ADXRS450_Gyro();
	
	//Encoder objects
	Encoder encoderFrontLeft = new Encoder(0,1,false,Encoder.EncodingType.k4X);
	//Encoder encoderFrontRight = new Encoder(0,1,false,Encoder.EncodingType.k4X);
	 
	//Distance per pulse for the encoder
	//For reference, the encoder being used for 2018 have 256 pulses per revolution
	//Units used are inches
	double kDistPerPulse = 0.0751650586;
	
	//Used to control pnuematics (regulates air allowing the piston for the climber to raise)
	Solenoid solenoid = new Solenoid(1);
	
	//Variables for encoder
	//count gets pulses
	int countLeft;
	double encoderDistanceLeft;
	double rateLeft;
	boolean directionLeft;
	boolean stoppedLeft;
	int countRight;
	double encoderDistanceRight;
	double rateRight;
	boolean directionRight;
	boolean stoppedRight;
	
	//Organizes the motors on each side into groups
	SpeedControllerGroup leftDrive = new SpeedControllerGroup(frontLeft, rearLeft);
	SpeedControllerGroup rightDrive = new SpeedControllerGroup(frontRight, rearRight);
	//DifferentialDrive takes the two groups for the robotDrive. The robotDrive is used later to control the robot's movement
	DifferentialDrive robotDrive = new DifferentialDrive(leftDrive, rightDrive);
	//DefaultAuto and CustomAuto are basic options for autonomous, which will appear on the smart dashboard
	private static final String kDefaultAuto = "Default Robot Auto";
	private static final String kCustomAuto = "My Robot Auto";
	private static final String kCustomAuto2 = "My Second Robot Auto ";
	
	//Alliance color variable
	DriverStation.Alliance color;
	//Robot position-on-field variable
	int station;
	//String telling the robot the locations of the switches on field
	String gameData;
	
	
	//Game controller 
	private Joystick m_stick = new Joystick(0);
	//Chooser on the smartdashboard for autonomous modes
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	
	public Robot() {
		robotDrive.setExpiration(0.1);
	}
	
	@Override
	public void robotInit() {
		//Enables the camera. No other onjects or code is reqiured besides this
		CameraServer.getInstance().startAutomaticCapture();
		//Sends "kDefaultAuto", kCustomAuto", and "kCustomAuto2" to the smartdahsboard as chooseable options
		m_chooser.addDefault("Default Robot Auto", kDefaultAuto);
		m_chooser.addObject("My Robot Auto", kCustomAuto);
		m_chooser.addObject("My Second Robot Auto", kCustomAuto2);
		SmartDashboard.putData("Auto modes", m_chooser);
		
	}
	
	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the if-else structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 *
	 * <p>If you wanted to run a similar autonomous mode with an IterativeRobot
	 * you would write:
	 *
	 * <blockquote><pre>{@code
	 * Timer timer = new Timer();
	 *
	 * // This function is run once each time the robot enters autonomous mode
	 * public void autonomousInit() {
	 *     timer.reset();
	 *     timer.start();
	 * }
	 *
	 * // This function is called periodically during autonomous
	 * public void autonomousPeriodic() {
	 * // Drive for 2 seconds
	 *     if (timer.get() < 2.0) {
	 *         myRobot.drive(-0.5, 0.0); // drive forwards half speed
	 *     } else if (timer.get() < 5.0) {
	 *         myRobot.drive(-1.0, 0.0); // drive forwards full speed
	 *     } else {
	 *         myRobot.drive(0.0, 0.0); // stop robot
	 *     }
	 * }
	 * }</pre></blockquote>
	 * @param  
	 */
	/*	public void disabled() {
		station = 0;
		color = null;
		gameData = null;
		while(station == 0 || color == null || gameData.length() > 0) {
			//Gets the alliance color for the round
			color = DriverStation.getInstance().getAlliance();
			//Gets position that the robot is in (1-3)
			station = DriverStation.getInstance().getLocation();
			//Gets locations of switches on the field
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			}
	}
*/
	@Override
	public void autonomous() {
		//Gets the alliance color for the round
		color = DriverStation.getInstance().getAlliance();
		//Gets position that the robot is in (1-3)
		station = DriverStation.getInstance().getLocation();
		//Gets locations of switches on the field
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		String autoSelected = m_chooser.getSelected();
		//autoSelected = SmartDashboard.getString(m_chooser.getSelected(), kDefaultAuto);
		System.out.println("1Auto selected: " + autoSelected + m_chooser.getSelected());
		// MotorSafety improves safety when motors are updated in loops
		// but is disabled here because motor updates are not looped in
		// this autonomous mode.
		robotDrive.setSafetyEnabled(false);
		//Resets gyro on autonomous
		gyro.reset();
		//Display Gyro variable thingy
		SmartDashboard.putNumber("Gyro",gyro.getAngle());
		//Encoder parameters for front left encoder
		encoderFrontLeft.setMaxPeriod(.1);
		encoderFrontLeft.setMinRate(10);
		//In inches
		encoderFrontLeft.setDistancePerPulse(0.00390625);
		encoderFrontLeft.setReverseDirection(false);
		encoderFrontLeft.setSamplesToAverage(10);
		
		//Encoder parameters for front right encoder
		//encoderFrontRight.setMaxPeriod(.1);
		//encoderFrontRight.setMinRate(10);
		//In inches
		//encoderFrontRight.setDistancePerPulse(kDistPerPulse);
		//encoderFrontRight.setReverseDirection(false);
		//encoderFrontRight.setSamplesToAverage(7);
		
		//A switch that uses the autonomous options on the smartdashboard as cases, depending on what case is chosen, the switch will perform the code written for that case
		System.out.println("2Auto selected: " + autoSelected);
		switch (autoSelected) {
		//An autonomous mode that is used for testing purpouses
			case kDefaultAuto:
				climber1.set(1);
				climber2.set(1);
				break;
		//The autonomous mode that is planned to be used for the competition
		//Gets the team color, station, and switch positions to decide what "if" statement to use
			case kCustomAuto:
				if(color == DriverStation.Alliance.Blue && station == 1) {
					if(gameData.charAt(0) == 'R') {
						autoDrive(238);
						autoTurn(90);
						autoDrive(40);
						openClaw();
						autoDrive(-35);
						autoTurn(-90);
						//autoDrive(unknown);
						//autoTurn(unknown);
					}
					else if(gameData.charAt(0) == 'L') {
						autoDrive(238);
						autoTurn(90);
						autoDrive(40);
						openClaw();
						autoDrive(-35);
						autoTurn(-90);
						//autoDrive(unknown);
						//autoTurn(unknown);
					}
					else {
						robotDrive.arcadeDrive(0,0);			
					}
				
				}
				else if(color == DriverStation.Alliance.Blue && station == 2) {
					if(gameData.charAt(0) == 'R') {
						//autoDrive(unknown);
					}
					else if(gameData.charAt(0) == 'L') {
						//autoDrive(unknown);
					}
					else {
						robotDrive.arcadeDrive(0,0);
					}
				}
				else if(color == DriverStation.Alliance.Blue && station == 3) {
					if(gameData.charAt(0) == 'R') {
						//autoDrive(unknown);
					}
					else if(gameData.charAt(0) == 'L') {
						//autoDrive(unknown);
					}
					else {
						robotDrive.arcadeDrive(0,0);
					}
				}
					
				break;
		//Another autonomous mode used for testing
			case kCustomAuto2:
				System.out.println("In CA2");
				double speed = 0.05;
				encoderFrontLeft.reset();
				SmartDashboard.putNumber("Testliness", 330);
				while(-encoderFrontLeft.get() < 1792) {
					encoderFrontLeft.get();
					SmartDashboard.putNumber("EncPulses", -encoderFrontLeft.get());
					frontLeft.set(speed);
					frontRight.set(speed);
					rearLeft.set(speed);
					rearRight.set(speed);
					
					}
				speed = -0.05;
				encoderFrontLeft.reset();
				while(-encoderFrontLeft.get() > -512) {
					encoderFrontLeft.get();
					SmartDashboard.putNumber("EncPulses", -encoderFrontLeft.get());
					frontLeft.set(speed);
					frontRight.set(speed);
					rearLeft.set(speed);
					rearRight.set(speed);
				}
				Timer.delay(2);
				speed = 0.05;
				encoderFrontLeft.reset();
				while(-encoderFrontLeft.get() < 1024) {
					encoderFrontLeft.get();
					SmartDashboard.putNumber("EncPulses",  -encoderFrontLeft.get());
					frontLeft.set(speed);
					frontRight.set(speed);
					rearLeft.set(speed);
					rearRight.set(speed);
					}
				speed = 0;
				frontLeft.set(speed);
				frontRight.set(speed);
				rearLeft.set(speed);
				rearRight.set(speed);
				break;
		}
	}

	/**
	 * Runs the motors with arcade steering.
	 *
	 * <p>If you wanted to run a similar teleoperated mode with an IterativeRobot
	 * you would write:
	 *
	 * <blockquote><pre>{@code
	 * // This function is called periodically during operator control
	 * public void teleopPeriodic() {
	 *     myRobot.arcadeDrive(stick);
	 * }
	 * }</pre></blockquote>
	 */
	@Override
	public void operatorControl() {
		robotDrive.setSafetyEnabled(true);
		boolean goingForward = false;
		boolean goingReverse = false;
		//sets up current limiting for the claw
		claw.enableCurrentLimit(true);
		claw.configPeakCurrentLimit(6,1);
		claw.configPeakCurrentDuration(1,1);
		claw.configContinuousCurrentLimit(5,1);
		while (isOperatorControl() && isEnabled()) {
			
			//Because the 2018 robot has problems with slowly turning to one side, we use booleans and "if"  statments to tell it to apply more positive or negative power to the motor controller
			//By setting the goingForward boolean to true, it tells the code that the robot is going forward, and that it should make the left side motors go faster forwards
			//By setting the goingReverse boolean to true, it tells the code to make the left side motors to go backwards faster
			if(m_stick.getY() > 0) {
				//gets the y-axis and x-axis of the joystick, and applies the output to the motor power.
				//The Math.pow creates a exponential function using the output from the joystick, and raises it to the power of the second number (3 in this case)
				//This makes it so that the robot goes much slower when moving the joystick less, but once the joystick is pushed enough in one direction, the speed of the motors ramps up much faster
				robotDrive.arcadeDrive(Math.pow(m_stick.getY(), 3), Math.pow(m_stick.getX(), 3));
				goingForward = true;
				goingReverse = false;
			}
			else if(m_stick.getY() < 0) {
				robotDrive.arcadeDrive(Math.pow(m_stick.getY(), 3), Math.pow(m_stick.getX(), 3));
				goingForward = false;
				goingReverse = true;
			}
			if(goingForward == true) {
				frontRight.set(- 0.05);
				rearRight.set(- 0.05);
			}
			else if(goingReverse == true) {
				frontRight.set(+ 0.05);
				rearRight.set(+ 0.05);
			}
			else if(goingReverse == false && goingForward == false) {
				robotDrive.arcadeDrive(0,0);
			}
			if(m_stick.getRawButton(12)) {
				autoTurn(180);
			}
			if(m_stick.getRawButton(14)) {
				autoTurn(-90);
			}
			if(m_stick.getRawButton(15)) {
				autoTurn(90);
			}
			SmartDashboard.putNumber("Pulses",encoderFrontLeft.get());
			//By holding X, the claw will clamp down on the cube
			if(m_stick.getRawButton(3)) {
				closeClaw();
				System.out.println("Close claw");
			}
			//By pressing Y, the claw will open
			else if(m_stick.getRawButton(4)) {
				openClaw();
				System.out.println("Open claw");
			}
			//By pressing  the right bumper (button 6), it will set the claw to 0 power. Only useful for testing the claw motor.
			else if(m_stick.getRawButton(6)){
				claw.set(0);
			}
			else {
				claw.set(0);
			}
			//By pressing the left bumper, the solenoid allows air to pass to the pnuematics, raising the climber.
			if(m_stick.getRawButton(7)) {
				solenoid.set(true);
			}
			else {
				solenoid.set(false);
			}
			//Checks what the voltage of the claw motor is, used for testing the the current limiter.
			SmartDashboard.putNumber("Amperes",claw.getOutputCurrent());
			// The motors will be updated every 5ms
			Timer.delay(0.005);
			}
	}
	
	/**
	 * Drives the robot using a single paramenter, distance, as well as encoders to track the movement of the robot. Capable of moving the robot forwards and back without needing to write arcadeDrive each time.
	 * @param distance
	 */
	public void autoDrive(double distance) {
		
		//The distance and pulses for the encoder, displayed on the smartdashboard
		//Every 489 pulses, there is one rotation. Gear is 5 inches
		SmartDashboard.putNumber("Pulses",countLeft);
		SmartDashboard.putNumber("Inches", distance);
		//Resets encoder after running autoDrive
		encoderFrontLeft.reset();
		//encoderFrontRight.reset();
		encoderDistanceLeft = 0;
		double speed = 0.4;
		//Tests if the robot has hit the target distance by checking the distance from the encoder.
		while(distance > encoderDistanceLeft) {
			//tests if the robot is within 2 feet of target distance. If so, the robot slows down to make sure it doesn't overshoot the target distance
			if(distance - encoderDistanceLeft <= 24){
				speed = 0.3;
			}
			frontLeft.set(speed);
			frontRight.set(speed * 0.90);
			rearRight.set(speed * 0.90);
			rearLeft.set(speed);
			//Variables for front left encoder
			countLeft = encoderFrontLeft.get();
			encoderDistanceLeft = encoderFrontLeft.getDistance();
			rateLeft = encoderFrontLeft.getRate();
			directionLeft = encoderFrontLeft.getDirection();
			stoppedLeft = encoderFrontLeft.getStopped();
			Timer.delay(0.005);
		}
		robotDrive.arcadeDrive(0,0);
		
	}
	/**
	 * The turn function allows you to input any angle for turning, this way you can easily turn any angle you want, without multiple functions
	 * @param angle
	 */
	public void autoTurn(double angle) {
		
		//Resets encoder after running autoDrive
		encoderFrontLeft.reset();
		//encoderFrontRight.reset();
		encoderDistanceLeft = 0;
		encoderDistanceRight = 0;
		double turn = -0.5;
		while(angle > -gyro.getAngle()) {	
			//tests if the angle left for the robot to turn is within 20 degress. If so, it slows the robot
			if(angle - gyro.getAngle() <= 20) {
				turn = -0.2;
			}
			robotDrive.arcadeDrive(0,turn);
			SmartDashboard.putNumber("Gyro",-gyro.getAngle());
			Timer.delay(0.005);
		}
		robotDrive.arcadeDrive(0,0);
		
	}
	
	/**
	 * A function that first limits the claw motor to 5 amps, and then runs the motor at 25%.
	 */
	public void closeClaw() {
		System.out.println("I'm at closeClaw");
		claw.enableCurrentLimit(true);
		claw.configPeakCurrentLimit(6,1);
		claw.configPeakCurrentDuration(1,1);
		claw.configContinuousCurrentLimit(5,1);
		claw.set(0.25);
	}
	/**
	 * A function that sets the claw motor to run backwards (open the claw) for half a second, and then set the motor back to 0 power. Timer.delay is used to run the motor for the specified 1 second.
	 */
	public void openClaw() {
		claw.enableCurrentLimit(true);
		claw.configPeakCurrentLimit(6,1);
		claw.configPeakCurrentDuration(1,1);
		claw.configContinuousCurrentLimit(5,1);
		System.out.println("I'm at openClaw");
		claw.set(-0.25);
		Timer.delay(0.5);
		claw.set(0);
	}

	//A random project currently not being used, but took a lot of time to copy and paste. Use this as a learning example of what not to do when trying to program a robot to move
	
	/*public void robotDriveCustom() {
		System.out.println("I'm using the new drive class");
		if(Math.abs(m_stick.getY()) < 0.1) {
			robotDrive.arcadeDrive(0,0);
		 }
		else if(m_stick.getY() <= 0.1 && m_stick.getY() >= 0.2) {
			robotDrive.arcadeDrive(0.1,0);
		}
		else if(m_stick.getY() <= 0.2 && m_stick.getY() >= 0.3) {
			robotDrive.arcadeDrive(0.2,0);
			}
		else if(m_stick.getY() <= 0.3 && m_stick.getY() >= 0.4) {
		 	robotDrive.arcadeDrive(0.3,0);
		  	}
	 	else if(m_stick.getY() <= 0.4 && m_stick.getY() >= 0.5) {
		  	robotDrive.arcadeDrive(0.4,0);
		  	}
		else if(m_stick.getY() <= 0.5 && m_stick.getY() >= 0.6) {
		  	robotDrive.arcadeDrive(0.5,0);
		  	}
		else if(m_stick.getY() <= 0.6 && m_stick.getY() >= 0.7) {
			robotDrive.arcadeDrive(0.6,0);
		  	}
		else if(m_stick.getY() <= 0.7 && m_stick.getY() >= 0.8) {
		  	robotDrive.arcadeDrive(0.7,0);
		  	}
		else if(m_stick.getY() <= 0.8 && m_stick.getY() >= 0.9) {
		  	robotDrive.arcadeDrive(0.7,0);
		  	}
		else if(m_stick.getY() <= 0.9 && m_stick.getY() > 1) {
		  	robotDrive.arcadeDrive(0.9,0);
		  	}
		else if(m_stick.getY() == 1) {
		  	robotDrive.arcadeDrive(1,0);
		  	}
		else if(m_stick.getY() < -0.1 && m_stick.getY() >= 0) {
			robotDrive.arcadeDrive(0,0);
		 }
		else if(m_stick.getY() <= -0.1 && m_stick.getY() >= -0.2) {
			robotDrive.arcadeDrive(-0.1,0);
		  	}
		else if(m_stick.getY() <= -0.2 && m_stick.getY() >= -0.3) {
			robotDrive.arcadeDrive(-0.2,0);
		  	}
		else if(m_stick.getY() <= -0.3 && m_stick.getY() >= -0.4) {
			robotDrive.arcadeDrive(-0.3,0);
		  	}
	 	else if(m_stick.getY() <= -0.4 && m_stick.getY() >= -0.5) {
			robotDrive.arcadeDrive(-0.4,0);
		  	}
		else if(m_stick.getY() <= -0.5 && m_stick.getY() >= -0.6) {
		  	robotDrive.arcadeDrive(-0.5,0);
		  	}
		else if(m_stick.getY() <= -0.6 && m_stick.getY() >= -0.7) {
				robotDrive.arcadeDrive(-0.6,0);
		  	}
	 	else if(m_stick.getY() <= -0.7 && m_stick.getY() >= -0.8) {
		  	robotDrive.arcadeDrive(-0.7,0);
		  	}
		else if(m_stick.getY() <= -0.8 && m_stick.getY() >= -0.9) {
		  		robotDrive.arcadeDrive(-0.8,0);
		  	}
		else if(m_stick.getY() <= -0.9 && m_stick.getY() > -1) {
		  		robotDrive.arcadeDrive(-0.9,0);
		  	}
		else if(m_stick.getY() == -1) {
		  		robotDrive.arcadeDrive(-1,0);
		  	}
		else if(Math.abs(m_stick.getX()) < 0.1) {
				robotDrive.arcadeDrive(0,0);
		 }
		else if(m_stick.getX() <= -0.1 && m_stick.getX() >= -0.2) {
				robotDrive.arcadeDrive(0,-0.1);
		  	}
		else if(m_stick.getX() <= -0.2 && m_stick.getX() >= -0.3) {
			robotDrive.arcadeDrive(0,-0.2);
		  	}
		else if(m_stick.getX() <= -0.3 && m_stick.getX() >= -0.4) {
		 	robotDrive.arcadeDrive(0,-0.3);
		  	}
	 	else if(m_stick.getX() <= -0.4 && m_stick.getX() >= -0.5) {
		  	robotDrive.arcadeDrive(0,-0.4);
		  	}
		else if(m_stick.getX() <= -0.5 && m_stick.getX() >= -0.6) {
		  	robotDrive.arcadeDrive(0,-0.5);
		  	}
		else if(m_stick.getX() <= -0.6 && m_stick.getX() >= -0.7) {
			robotDrive.arcadeDrive(0,-0.6);
		  	}
	 	else if(m_stick.getX() <= -0.7 && m_stick.getX() >= -0.8) {
		  	robotDrive.arcadeDrive(0,-0.6);
		  	}
		else if(m_stick.getX() <= -0.8 && m_stick.getX() >= -0.9) {
		  	robotDrive.arcadeDrive(0,-0.6);
		  	}
		else if(m_stick.getX() <= -0.9 && m_stick.getX() > -1) {
		  	robotDrive.arcadeDrive(0,-0.6);
		  	}
		else if(m_stick.getX() == -1) {
		  	robotDrive.arcadeDrive(0,-0.6);
		  	}
		else if(m_stick.getX() < 0.1 && m_stick.getX() >= 0) {
			robotDrive.arcadeDrive(0,0);
		 }
		else if(m_stick.getX() <= 0.1 && m_stick.getX() >= 0.2) {
			robotDrive.arcadeDrive(0,0.1);
		  	}
		else if(m_stick.getX() <= 0.2 && m_stick.getX() >= 0.3) {
			robotDrive.arcadeDrive(0,0.2);
		  	}
		else if(m_stick.getX() <= 0.3 && m_stick.getX() >= 0.4) {
		 	robotDrive.arcadeDrive(0,0.3);
		  	}
	 	else if(m_stick.getX() <= 0.4 && m_stick.getX() >= 0.5) {
		  	robotDrive.arcadeDrive(0,0.4);
		  	}
	 	else if(m_stick.getX() <= 0.5 && m_stick.getX() >= 0.6) {
		  	robotDrive.arcadeDrive(0,0.5);
		  	}
	  	else if(m_stick.getX() <= 0.6 && m_stick.getX() >= 0.7) {
			robotDrive.arcadeDrive(0,0.6);
		  	}
		else if(m_stick.getX() <= 0.7 && m_stick.getX() >= 0.8) {
		  	robotDrive.arcadeDrive(0,0.6);
		  	}
		else if(m_stick.getX() <= 0.8 && m_stick.getX() >= 0.9) {
		  	robotDrive.arcadeDrive(0,0.6);
		  	}
		else if(m_stick.getX() <= 0.9 && m_stick.getX() > 1) {
		  	robotDrive.arcadeDrive(0,0.6);
		  	}
		else if(m_stick.getX() == 1) {
		  	robotDrive.arcadeDrive(0,0.6);
		  	}
	}
	*/
	
	//Another project not being used. (keep for later testing and ideas)
	
/*public void robotDriveCustom2() {
	SmartDashboard.putNumber("Magnitude", m_stick.getMagnitude());
	/*if(m_stick.getY() >= 0) {
		robotDrive.arcadeDrive(m_stick.getMagnitude(),Math.pow(m_stick.getX(),3));
	}
	else if(m_stick.getY() < 0) {
		robotDrive.arcadeDrive(-m_stick.getMagnitude(),m_stick.getX());
	}
	
*/
}