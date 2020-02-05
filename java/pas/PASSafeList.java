public class PASSafeList {
    PASSafe[] GetSafesResult;

    public void print() {
	for(Integer i=0; i<GetSafesResult.length; i++) {
		this.GetSafesResult[i].print();
		System.out.println("");
	}
    }
}
