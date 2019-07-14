package eecalcs;

public class ConduitProperties {

	//region enums
	public static enum ConduitMaterial{
		PVC, ALUMINUM, STEEL
	}
	//endregion

	public static ConduitMaterial getConduitMaterialPerIndex(int conduitTypeIndex){
		if(conduitTypeIndex==0)
			return ConduitMaterial.PVC;
		else if(conduitTypeIndex==1)
			return ConduitMaterial.ALUMINUM;
		else
			return ConduitMaterial.STEEL;
	}
}
