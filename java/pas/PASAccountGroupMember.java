public class PASAccountGroupMember {
    String AccountID;
    String SafeName;
    String PlatformID;
    String Address;
    String UserName;

    public void print() {
	System.out.println( "AccountID: " + this.AccountID);
	System.out.println( "SafeName: " + this.SafeName);
	System.out.println( "PlatformID: " + this.PlatformID);
	System.out.println( "Address: " + this.Address);
	System.out.println( "UserName: " + this.UserName);
    }
} // PASAccountGroupMember
