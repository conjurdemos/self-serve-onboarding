public class PASPlatformGeneral{
    String id;
    String name;
    String systemType;
    String active;
    String description;
    String platformBaseID;
    String platformType;

    public void print() {
	System.out.println( "  id: " + this.id);
	System.out.println( "  name: " + this.name);
	System.out.println( "  systemType: " + this.systemType);
	System.out.println( "  active: " + this.active);
	System.out.println( "  platformBaseID: " + this.platformBaseID);
	System.out.println( "  platformType: " + this.platformType);
    };
}
