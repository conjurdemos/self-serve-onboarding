// This class is mostly useful for Gson parsing of the json output from get accounts.

public class PASPlatformList {
    PASPlatform[] Platforms;
    Integer Total;

    public void print() {
	System.out.println("PAS Platform List ===========");
	for(Integer i=0; i<this.Platforms.length; i++) {
		this.Platforms[i].print();
		System.out.println("");
	}
    }
}
