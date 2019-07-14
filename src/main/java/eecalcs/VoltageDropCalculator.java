package eecalcs;

import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.List;

public class VoltageDropCalculator {

	//region private local variables
	private boolean recalculationRequired = false;
	private ConductorProperties conductor;
	private double wireResistance;
	private double wireReactance;
	//input variables
	private double sourceVoltage;
	private int phases;
	private String wireSize = "";
	private ConduitProperties.ConduitMaterial conduitMaterial;
	private ConductorProperties.ConductorMetal conductorMetal;
	private int numberOfSets;
	private double oneWayLength;
	private double loadCurrent;
	private double powerFactor;
	private boolean isDC;
	private ConductorProperties.CopperCoating copperCoating;
	private double maxVDropPercent;
	private double maxLength;
	//output variables
	private double voltageDropVolts;
	private double voltageAtLoad;
	private double voltageDropPercentage;
	//endregion

	//region public fields
	public List<Error> Errors =  new ArrayList<>();
	public class Error {
		private String errorMessage;
		private int errorNumber;

		public Error(String errorMessage, int errorNumber) {
			this.errorMessage = errorMessage;
			this.errorNumber = errorNumber;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public int getErrorNumber() {
			return errorNumber;
		}
	}
	//endregion

	//region Constructors
	public VoltageDropCalculator(double sourceVoltage, int phases, String wireSize, ConduitProperties.ConduitMaterial conduitMaterial,
	                             ConductorProperties.ConductorMetal conductorMetal, int numberOfSets, double oneWayLength,
	                             double loadCurrent, double powerFactor, boolean isDC, ConductorProperties.CopperCoating copperCoating) {
		setSourceVoltage(sourceVoltage);
		setPhases(phases);
		setWireSize(wireSize);
		setConduitMaterial(conduitMaterial);
		setConductorMetal(conductorMetal);
		setNumberOfSets(numberOfSets);
		setOneWayLength(oneWayLength);
		setLoadCurrent(loadCurrent);
		setPowerFactor(powerFactor);
		this.isDC = isDC;
		this.copperCoating = copperCoating;
		maxVDropPercent = 0;
		maxLength = 0;
		//getVoltageDropVolts();
	}
	//endregion

	//region Setters
	public void setSourceVoltage(double sourceVoltage) {
		if(this.sourceVoltage != sourceVoltage) {
			this.sourceVoltage = sourceVoltage;
			recalculationRequired = true;
		}
		if(sourceVoltage < 0){
			recalculationRequired = false;
			addError("Source voltage cannot be negative.",-1);
		}
	}

	public void setPhases(int phases) {
		if(this.phases != phases) {
			this.phases = phases;
			recalculationRequired = true;
		}
		if(!(phases == 1 || phases == 3)){
			recalculationRequired = false;
			addError("Number of phases must be 1 or 3.",-2);
		}
	}

	public void setWireSize(String wireSize) {
		if(!this.wireSize.equals(wireSize)){
			this.wireSize = wireSize;
			recalculationRequired = true;
		}
		if(ConductorProperties.isValidWireSize(wireSize))
			this.conductor = ConductorProperties.getConductorBySize(wireSize);
		else{
			recalculationRequired = false;
			addError("\"" + wireSize + "\"" +" is not a valid conductor size.",-3);
		}
		checkSets();
		checkAmpacity();
	}

	public void setConduitMaterial(ConduitProperties.ConduitMaterial conduitMaterial) {
		if(this.conduitMaterial != conduitMaterial){
			this.conduitMaterial = conduitMaterial;
			recalculationRequired = true;
		}
	}

	public void setConductorMetal(ConductorProperties.ConductorMetal conductorMetal) {
		if(this.conductorMetal != conductorMetal) {
			this.conductorMetal = conductorMetal;
			recalculationRequired = true;
		}
	}

	public void setNumberOfSets(int numberOfSets) {
		if(this.numberOfSets != numberOfSets) {
			this.numberOfSets = numberOfSets;
			recalculationRequired = true;
		}
		if(numberOfSets < 1 || numberOfSets > 10){
			recalculationRequired = false;
			addError("Number of sets must be between 1 and 10.",-4);
		}
		checkSets();
		checkAmpacity();
	}

	public void setOneWayLength(double oneWayLength) {
		if(this.oneWayLength != oneWayLength) {
			this.oneWayLength = oneWayLength;
			recalculationRequired = true;
		}
		if(oneWayLength <= 0){
			recalculationRequired = false;
			addError("One way conductor length must be greater than 0",-5);
		}
	}

	public void setLoadCurrent(double loadCurrent) {
		if(this.loadCurrent != loadCurrent) {
			this.loadCurrent = loadCurrent;
			recalculationRequired = true;
		}
		if(loadCurrent <= 0){
			recalculationRequired = false;
			addError("Load current must be greater than 0",-6);
		}
		checkSets();
		checkAmpacity();
	}

	public void setPowerFactor(double powerFactor) {
		if(this.powerFactor != powerFactor) {
			this.powerFactor = powerFactor;
			recalculationRequired = true;
		}
		if(powerFactor < 0.7 || powerFactor > 1.0){
			recalculationRequired = false;
			addError("Power factor must be between 0.7 and 1.0",-7);
		}
	}

	public void setMaxVDropPercent(double maxVDropPercent) {
		if(this.maxVDropPercent != maxVDropPercent) {
			this.maxVDropPercent = maxVDropPercent;
			getConductorForVoltageDrop();
		}
	}
	//endregion

	//region Getters
	public ConductorProperties getConductor() {
		return conductor;
	}

	public double getSourceVoltage() {
		return sourceVoltage;
	}

	public int getPhases() {
		return phases;
	}

	public String getWireSize() {
		return wireSize;
	}

	public ConduitProperties.ConduitMaterial getConduitMaterial() {
		return conduitMaterial;
	}

	public ConductorProperties.ConductorMetal getConductorMetal() {
		return conductorMetal;
	}

	public int getNumberOfSets() {
		return numberOfSets;
	}

	public double getOneWayLength() {
		return oneWayLength;
	}

	public double getLoadCurrent() {
		return loadCurrent;
	}

	public double getPowerFactor() {
		return powerFactor;
	}

	public boolean isDC() {
		return isDC;
	}

	public ConductorProperties.CopperCoating getCopperCoating() {
		return copperCoating;
	}

	public double getVoltageDropVolts() {
		calculateVoltageDrop();
		return voltageDropVolts;
	}

	public double getVoltageAtLoad() {
		calculateVoltageDrop();
		return voltageAtLoad;
	}

	public double getVoltageDropPercentage() {
		calculateVoltageDrop();
		return voltageDropPercentage;
	}

	public boolean hasErrors(){
		checkSets();
		checkAmpacity();
		for(Error error: Errors){
			if(error.getErrorNumber() < 0) return true;
		}
		return false;
	}

	public double getMaxLength() {
		return maxLength;
	}
	//endregion

	//region private methods
	private void checkSets(){
		if(numberOfSets > 1 && ConductorProperties.compareSizes(wireSize,"1/0") < 0) {
			recalculationRequired = false;
			addError("Paralleled power conductors in sizes smaller than 1/0 AWG are not permitted. NEC-310.10(H)(1)", -9);
		}
	}

	private void checkAmpacity(){
		if(getMaxSetAmpacity() < loadCurrent) {
			recalculationRequired = false;
			addError("Load current exceeds maximum allowed conductor set's ampacity at 90°C. NEC-310.15(B)(16)",-8);
		}
	}

	private double getMaxSetAmpacity(){
		if(conductorMetal == ConductorProperties.ConductorMetal.COPPER)
			return conductor.copper.ampacity.t90 * numberOfSets;
		else
			return conductor.aluminum.ampacity.t90 * numberOfSets;
	}

	private void calculateVoltageDrop() {
		if(!recalculationRequired) return;

		//region compute k
		double k;
		if(phases == 1 || isDC){
			k = 2;
		}else{
			k = Math.sqrt(3);
		}
		//endregion

		//region compute total wire resistance
		if(conductorMetal == ConductorProperties.ConductorMetal.COPPER){
			if(isDC) {
				if(copperCoating == ConductorProperties.CopperCoating.COATED){
					wireResistance = conductor.copper.resistance.dc.coated;
				} else {
					wireResistance = conductor.copper.resistance.dc.uncoated;
				}
			} else {
				if (conduitMaterial == ConduitProperties.ConduitMaterial.PVC) {
					wireResistance = conductor.copper.resistance.ac.inPVCCond;
				} else if (conduitMaterial == ConduitProperties.ConduitMaterial.ALUMINUM) {
					wireResistance = conductor.copper.resistance.ac.inALCond;
				} else {
					wireResistance = conductor.copper.resistance.ac.inSteelCond;
				}
			}
		}else{ //conductor is aluminum
			if(isDC) {
				wireResistance = conductor.aluminum.resistance.dc;
			} else {
				if (conduitMaterial == ConduitProperties.ConduitMaterial.PVC) {
					wireResistance = conductor.aluminum.resistance.ac.inPVCCond;
				} else if (conduitMaterial == ConduitProperties.ConduitMaterial.ALUMINUM) {
					wireResistance = conductor.aluminum.resistance.ac.inALCond;
				} else {
					wireResistance = conductor.aluminum.resistance.ac.inSteelCond;
				}
			}
		}
		wireResistance = wireResistance * oneWayLength * 0.001 / numberOfSets;
		//endregion

		//region compute total wire reactance
		if(isDC)
			wireReactance = 0;
		else {
			if (conduitMaterial == ConduitProperties.ConduitMaterial.PVC || conduitMaterial == ConduitProperties.ConduitMaterial.ALUMINUM) {
				wireReactance = conductor.reactance.inNonMagCond;
			} else {
				wireReactance = conductor.reactance.inMagCond;
			}
			wireReactance = wireReactance * oneWayLength * 0.001 / numberOfSets;
		}
		//endregion

		//region compute voltage drop
		Complex wireImpedance = new Complex(k * wireResistance, k * wireReactance);
		Complex sourceVoltageComplex = new Complex(sourceVoltage, 0);
		Complex current = new Complex(loadCurrent * powerFactor, -loadCurrent * Math.sin(Math.acos(powerFactor)));
		Complex wireVoltage = wireImpedance.multiply(current);
		Complex loadVoltage = sourceVoltageComplex.subtract(wireVoltage);
		//output variables
		voltageAtLoad = loadVoltage.abs();
		voltageDropVolts = sourceVoltage - voltageAtLoad;
		voltageDropPercentage = 100 * voltageDropVolts / sourceVoltage;
		recalculationRequired = false;
		//endregion
	}

	private void getConductorForVoltageDrop(){
		for(ConductorProperties _conductor: ConductorProperties.getTable()){
			wireSize = _conductor.Size;
			conductor = ConductorProperties.getConductorBySize(wireSize);
			recalculationRequired = true;
			calculateVoltageDrop();
			if(voltageDropPercentage <= maxVDropPercent){
				maxLength = computeMaxLength();
				if(maxLength < 0) addError("No length can achieve that voltage drop under the given conditions.", -10);
				return;
			}
		}
		addError("No building wire can achieve that voltage drop under these conditions.", -11);
	}

	private double computeMaxLength(){
		double wireR = (wireResistance / oneWayLength)/* / numberOfSets*/;
		double wireX = (wireReactance / oneWayLength)/* / numberOfSets*/;
		double theta = Math.acos(powerFactor);
		double Vs2 = Math.pow(sourceVoltage, 2);
		double A = 2 * loadCurrent * (wireR * powerFactor + wireX * Math.sin(theta));
		double B = 2 * loadCurrent * (wireX * powerFactor - wireR * Math.sin(theta));
		double C = Vs2 * (1 - Math.pow(1 - maxVDropPercent/100, 2));
		double Rad = 4 * Vs2 * A * A - 4 * (A * A + B * B) * C;
		if(Rad<0) return Rad;
		//double L1 = (2 * sourceVoltage * A + Math.sqrt(Rad))/(2 * (A * A + B * B));
		//L2 is always the lesser value between the two Ls and produces a voltage drop across the wires that is less that the voltage
		// source, that is L2 is always the correct value, unless it's a negative number.
		double L2 = (2 * sourceVoltage * A - Math.sqrt(Rad))/(2 * (A * A + B * B));
		//if(L1 > 0) return L1;
		if(L2 > 0) return L2;
		return -1.0;
	}

	private void addError(String errorMsg, int errorNumber) {
		if(errorNumber < 0) {
			voltageDropVolts = 0;
			voltageAtLoad = 0;
			voltageDropPercentage = 0;
		}
		for(Error error: Errors){
			if(error.errorNumber == errorNumber) return;
		}
		Errors.add(new  Error(errorMsg, errorNumber));
	}
	//endregion
}
/*todo
*  need to decouple the two functions of this class.
*  one idea is that the method calculate voltage drop does not modify any class private or public field pertaining to any type of
*  calculation
*  the other idea, much better is to create separate classes. Maybe one can inherited from the other....and override some functionalities...
*
*
*
*
* */



/*
Voltage drop manual of use:

1. Create a new VoltageDropCalculator object by calling its constructor.
2. Check if there are any error by calling object.hasErrors(), and if any, do not ask for results, otherwise results will be always 0.

3. Every time a value is changed, errors can occurs, so again check for errors.

4. If there are any errors, they can be listed in the object.Errors list.

------------For calculating conductor for maximum voltage drop--------------
a. Execute step 1 (assigning all required values for a voltage drop calculation), wire size can be any valid wire size, like "1/0"
b. There might be errors that should be checked before calculating the conductor; See step "e."
c. Set the maximum voltage drop by calling object.setMaxVDropPercent(maxVD). This will calculate the conductor, its actual voltage drop and the maximum length for the maximum given voltage drop. This calculation do not take into account any possible error that could happen when initiating the required values in the constructor or setting them later. That's why the user should check for the errors before proceding with this step c and decide if to proceed or not.
d. Check for errors after step c. This will give a list of error that could not avoid or interfere with the calculation but that are usefull for decision making (like, are these values and results code compliant?) and will also give error that affect the result like error #-11 "No building wire can achieve that voltage drop under these conditions." or (very rare) error #-11 "No length can achieve that voltage drop under the given conditions."

e. Do not proceed with step b if the following errors occurred:
Msg, error #
"Source voltage cannot be negative.", -1
"Number of phases must be 1 or 3.",-2
"One way conductor length must be greater than 0",-5
"Load current must be greater than 0",-6
"Power factor must be between 0.7 and 1.0",-7

Errors that can be ignore before proceeding:
Msg, error #
"Number of sets must be between 1 and 10.",-4
*/
