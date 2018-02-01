/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5203.robot;

import edu.wpi.first.wpilibj.SampleRobot;

import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.DriverStation;
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
	WPI_TalonSRX frontLeft = new WPI_TalonSRX(1);
	WPI_TalonSRX rearLeft = new WPI_TalonSRX(2);
	WPI_TalonSRX frontRight = new WPI_TalonSRX(3);
	WPI_TalonSRX rearRight = new WPI_TalonSRX(4);
	//Gyro used for giving direction of the robot, useful for autonomous
	//ADXRS450_Gyro gyro = new ADXRS450_Gyro();
	
	//Encoder objects
	Encoder encoderFrontLeft = new Encoder(0,1,false,Encoder.EncodingType.k2X);
	//Encoder encoderFrontRight = new Encoder(0,1,false,Encoder.EncodingType.k4X);
	 
	//Distance per pulse for the encoder
	//For reference, the encoder being used for 2018 have 256 pulses per revolution
	//Units used are inches
	double kDistPerPulse = 0.0751650586;
	
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
	//DifferentialDrive takes the two groups for the robotDrive, the robotDrive is used later to control the robot's movement
	DifferentialDrive robotDrive = new DifferentialDrive(leftDrive, rightDrive);
	//DefaultAuto and CustomAuto are basic options for autonomous, which will appear on the smart dashboard
	private static final String kDefaultAuto = "Default Robot Auto";
	private static final String kCustomAuto = "My Robot Auto";
	private static final String kCustomAuto2 = "My Second Robot Auto ";
	
	//Alliance color variable
	DriverStation.Alliance color;
	//Robot position-on-field variable
	int station;
	//String telling the robot the locations of the switches
	String gameData;
	
	
	//Game controller 
	private Joystick m_stick = new Joystick(0);
	
	private SendableChooser<String> m_chooser = new SendableChooser<>();

	public Robot() {
		robotDrive.setExpiration(0.1);
	}

	@Override
	public void robotInit() {
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
		//gyro.reset();
		
		//Display Gyro vvariable thingy
		//SmartDashboard.putNumber("Gyro",gyro.getAngle());
		
		//Encoder parameters for front left encoder
		encoderFrontLeft.setMaxPeriod(.1);
		encoderFrontLeft.setMinRate(10);
		//In inches
		encoderFrontLeft.setDistancePerPulse(kDistPerPulse);
		encoderFrontLeft.setReverseDirection(false);
		encoderFrontLeft.setSamplesToAverage(20);
		
		//Encoder parameters for front right encoder
	//	encoderFrontRight.setMaxPeriod(.1);
//		encoderFrontRight.setMinRate(10);
		//In inches
	//	encoderFrontRight.setDistancePerPulse(kDistPerPulse);
//		encoderFrontRight.setReverseDirection(false);
//		encoderFrontRight.setSamplesToAverage(7);
		
		//Random Autos made to test encoders and gyro
		System.out.println("2Auto selected: " + autoSelected);
		switch (autoSelected) {
			case kDefaultAuto:
				autoDrive(10);
				robotDrive.arcadeDrive(0,0);
				break;
			case kCustomAuto:
				if(color == DriverStation.Alliance.Blue && station == 1) {
					if(gameData.charAt(0) == 'R') {
						autoDrive(238);
					//	autoTurn(90);
						autoDrive(40);
						//drop cube in plate
						autoDrive(-35);
					//	autoTurn(-90);
						//autoDrive(unknown);
						//autoTurn(unknown);
					}
					else if(gameData.charAt(0) == 'L') {
						autoDrive(238);
					//	autoTurn(90);
						autoDrive(40);
						//Drop cube in plate
						autoDrive(-35);
					//	autoTurn(-90);
						//autoDrive(unknown);
						//autoTurn(unknown);
					}
					else {
						robotDrive.arcadeDrive(0,0);			
					}
				
				}
				if(color == DriverStation.Alliance.Blue && station == 2) {
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
				if(color == DriverStation.Alliance.Blue && station == 3) {
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
			case kCustomAuto2:
				System.out.println("In CA2");
				encoderFrontLeft.reset();
				SmartDashboard.putNumber("Testliness", 330);
				while(-encoderFrontLeft.get() < 1280) {
					encoderFrontLeft.get();
					SmartDashboard.putNumber("EncPulses", -encoderFrontLeft.get());
					frontLeft.set(0.05);
					frontRight.set(0.05);
					rearLeft.set(0.05);
					rearRight.set(0.05);
					
				}
				frontLeft.setNuetralMode();
				frontRight.setNuetralMode();
				rearLeft.setNuetralMode();
				rearRight.setNuetralMode();
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
		
		while (isOperatorControl() && isEnabled()) {
			/* Drive arcade style
			*Math.pow refers to the sensitivity of the stick for controlling the motors. The higher the number,
			*the more you need to move the joystick to reach higher speeds
			*The second double in Math.pow needs to be odd or the robot will not drive properly
			*/
			
			
			robotDrive.arcadeDrive(-Math.pow(m_stick.getY(),3), Math.pow(m_stick.getX(),3));
			//Uses buttons "A" and "B" to test basic electrical setup on spark controllers
			if(m_stick.getRawButton(1)) {
				frontLeft.set(0.1);
				frontRight.set(0.1);
				rearRight.set(0.1);
				rearLeft.set(0.1);
			}
			else {
				frontLeft.set(0);
				frontRight.set(0);
				rearRight.set(0);
				rearLeft.set(0);
			}
			if(m_stick.getRawButton(2)) {
				frontLeft.set(-0.05);
				frontRight.set(-0.05);
				rearRight.set(-0.05);
				rearLeft.set(-0.05);
			}
			else {
				frontLeft.set(0);
				frontRight.set(0);
				rearRight.set(0);
				rearLeft.set(0);
			}
			SmartDashboard.putNumber("Pulses",encoderFrontLeft.get());
			// The motors will be updated every 5ms
			Timer.delay(0.005);
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
	//	encoderFrontRight.reset();
		encoderDistanceLeft = 0;
		double speed = 0.6;
		while(distance > encoderDistanceLeft) {
			//tests if the robot is within 2 feet of target distance. If so, the robot slows down to make sure it doesn't overshoot the target distance
			if(distance - encoderDistanceLeft <= 24){
				speed = 0.3;
			}
			robotDrive.arcadeDrive(speed,0);
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
	//the turn function allows you to input any angle for turning, this way you can easily turn any angle you want, without multiple functions
	/*public void autoTurn(double angle) {
		
		//Resets encoder after running autoDrive
		encoderFrontLeft.reset();
		encoderFrontRight.reset();
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
		
	} */
	/*public void test() {
		robotDrive.setSafetyEnabled(false);
		gyro.reset();
		SmartDashboard.putNumber("Gyro",gyro.getAngle());
		encoderFrontLeft.setMaxPeriod(.1);
		encoderFrontLeft.setMinRate(10);
		encoderFrontLeft.setDistancePerPulse(kDistPerPulse);
		encoderFrontLeft.setReverseDirection(false);
		encoderFrontLeft.setSamplesToAverage(7);
		encoderFrontRight.setMaxPeriod(.1);
		encoderFrontRight.setMinRate(10);
		encoderFrontRight.setDistancePerPulse(kDistPerPulse);
		encoderFrontRight.setReverseDirection(false);
		encoderFrontRight.setSamplesToAverage(7);
		
		//Robot test code goes here:
	} */
}
