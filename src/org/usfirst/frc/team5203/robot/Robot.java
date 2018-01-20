/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5203.robot;

import edu.wpi.first.wpilibj.SampleRobot;

import edu.wpi.first.wpilibj.SpeedControllerGroup;
import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
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
public class Robot extends SampleRobot {
	
	@SuppressWarnings("deprecation") //CANTalon is depreciated
	
	//CANTalon motors "(1,2,3, or 4)" refers to the port on the robot
	CANTalon frontLeft = new CANTalon(1);
	CANTalon rearLeft = new CANTalon(2);
	CANTalon frontRight = new CANTalon(3);
	CANTalon rearRight = new CANTalon(4);
	
	//Encoder objects
	Encoder encoderBackLeft = new Encoder(0, 1, false, Encoder.EncodingType.k4X);
	Encoder encoderBackRight = new Encoder(0,1,false,Encoder.EncodingType.k4X);
	Encoder encoderFrontLeft = new Encoder(0,1,false,Encoder.EncodingType.k4X);
	Encoder encoderFrontRight = new Encoder(0,1,false,Encoder.EncodingType.k4X);
	
	//Distance per pulse for the old test encoder, get rid of later
	double kOldDistPerPulse = 0.01022495;
	
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
	
	//Spark motor was for testing
	Spark spark = new Spark(0);
	//Organizes the motors on each side into groups
	SpeedControllerGroup leftDrive = new SpeedControllerGroup(frontLeft, rearLeft);
	SpeedControllerGroup rightDrive = new SpeedControllerGroup(frontRight, rearRight);
	//DifferentialDrive takes the two groups for the robotDrive, the robotDrive is used later to control the robot's movement
	DifferentialDrive robotDrive = new DifferentialDrive(leftDrive, rightDrive);
	//DefaultAuto and CustomAuto are basic options for autonomous, which will appear on the smart dashboard
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private static final String kCustomAuto2 = "My Second Auto";


	private Joystick m_stick = new Joystick(0);
	private SendableChooser<String> m_chooser = new SendableChooser<>();

	public Robot() {
		robotDrive.setExpiration(0.1);
	}

	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		m_chooser.addObject("My Second Auto", kCustomAuto2);
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
	 */
	@Override
	public void autonomous() {
		String autoSelected = m_chooser.getSelected();
		// String autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + autoSelected);

		// MotorSafety improves safety when motors are updated in loops
		// but is disabled here because motor updates are not looped in
		// this autonomous mode.
		robotDrive.setSafetyEnabled(false);
		
		//Encoder parameters for back left encoder
		encoderBackLeft.setMaxPeriod(.1);
		encoderBackLeft.setMinRate(10);
		//In inches
		encoderBackLeft.setDistancePerPulse(kOldDistPerPulse);
		encoderBackLeft.setReverseDirection(false);
		encoderBackLeft.setSamplesToAverage(7);
		
		//Encoder parameters for back right encoder
		encoderBackRight.setMaxPeriod(.1);
		encoderBackRight.setMinRate(10);
		//In inches
		encoderBackRight.setDistancePerPulse(kOldDistPerPulse);
		encoderBackRight.setReverseDirection(false);
		encoderBackRight.setSamplesToAverage(7);
		
		//Encoder parameters for front left encoder
		encoderFrontLeft.setMaxPeriod(.1);
		encoderFrontLeft.setMinRate(10);
		//In inches
		encoderFrontLeft.setDistancePerPulse(kOldDistPerPulse);
		encoderFrontLeft.setReverseDirection(false);
		encoderFrontLeft.setSamplesToAverage(7);
		
		//Encoder parameters for front right encoder
		encoderFrontRight.setMaxPeriod(.1);
		encoderFrontRight.setMinRate(10);
		//In inches
		encoderFrontRight.setDistancePerPulse(kOldDistPerPulse);
		encoderFrontRight.setReverseDirection(false);
		encoderFrontRight.setSamplesToAverage(7);
		
		switch (autoSelected) {
			case kCustomAuto:
			
				
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
		
		while (isOperatorControl() && isEnabled()) {
			/* Drive arcade style
			*Math.pow refers to the sensitivity of the stick for controlling the motors. The higher the number,
			*the more you need to move the joystick to reach higher speeds
			*The second double in Math.pow needs to be odd or the robot will not drive properly
			*/
			
			robotDrive.arcadeDrive(-Math.pow(m_stick.getY(),3), Math.pow(m_stick.getX(),3));
			
			if(m_stick.getRawButton(1)) {
				spark.set(0.3);
			}
			
			else if(m_stick.getRawButton(2)) {
				spark.set(-0.3); }
			
			else {
				spark.set(0);
			// The motors will be updated every 5ms
			Timer.delay(0.005);
			}
		}
	}

	/**
	 * Runs during test mode.
	 */

	public void autoDrive(double distance) {
		
		//The distance and pulses for the encoder, displayed on the smartdashboard
		//Every 489 pulses, there is one rotation. Gear is 5 inches
		SmartDashboard.putNumber("Pulses",countLeft);
		SmartDashboard.putNumber("Inches", distance);
		//Resets encoder after running autoDrive
		encoderFrontLeft.reset();
		encoderFrontRight.reset();
		encoderDistanceLeft = 0;
		
		while(distance > encoderDistanceLeft) {
			//Drive robot forward
			robotDrive.arcadeDrive(1,0);
			//Variables for front left encoder
			countLeft = encoderFrontLeft.get();
			encoderDistanceLeft = encoderFrontLeft.getDistance();
			rateLeft = encoderFrontLeft.getRate();
			directionLeft = encoderFrontLeft.getDirection();
			stoppedLeft = encoderFrontLeft.getStopped();
			Timer.delay(0.005);
		}
		
	}

	public void leftTurn() {
		
		//Resets encoder after running autoDrive
		encoderFrontLeft.reset();
		encoderFrontRight.reset();
		encoderDistanceLeft = 0;
		encoderDistanceRight = 0;
		//While for turning is a work in progress
		
		/*
		while(distance > encoderDistanceLeft && distance > encoderDistanceRight) {
			robotDrive.arcadeDrive(0,-1);
			
			//Variables for front left encoder
			countLeft = encoderFrontLeft.get();
			encoderDistanceLeft = encoderFrontLeft.getDistance();
			rateLeft = encoderFrontLeft.getRate();
			directionLeft = encoderFrontLeft.getDirection();
			stoppedLeft = encoderFrontLeft.getStopped();
			
			//Variables for front right encoder
			countRight = encoderFrontRight.get();
			encoderDistanceRight = encoderFrontRight.getDistance();
			rateRight = encoderFrontRight.getRate();
			directionRight = encoderFrontRight.getDirection();
			stoppedRight = encoderFrontRight.getStopped();
			Timer.delay(0.005);
		}
		*/
		
	}
}
