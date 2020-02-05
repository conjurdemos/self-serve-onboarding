public class PASAccountGroup {
    String GroupID;
    String GroupName;
    String GroupPlatformID;
    String Safe;

    public void print() {
	System.out.println( "GroupID: " + this.GroupID);
	System.out.println( "GroupName: " + this.GroupName);
	System.out.println( "GroupPlatformID: " + this.GroupPlatformID);
	System.out.println( "Safe: " + this.Safe);
    }
} // PASAccountGroup
