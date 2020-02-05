public class PASSafe {

    String SafeName;
    String Description;
    String ManagingCPM;
    Integer NumberOfDaysRetention;
    Integer NumberOfVersionsRetention;
    Boolean OLACEnabled;

    public void print() {
	System.out.println( "SafeName: " + this.SafeName);
	System.out.println( "Description: " + this.Description);
	System.out.println( "ManagingCPM: " + this.ManagingCPM);
	System.out.println( "NumberOfDaysRetention: " + this.NumberOfDaysRetention);
	System.out.println( "NumberOfVersionsRetention: " + this.NumberOfVersionsRetention);
	System.out.println( "OLACEnabled: " + this.OLACEnabled);
    };
}
