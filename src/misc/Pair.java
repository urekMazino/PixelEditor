package misc;

public class Pair<T1, T2> {
	private T1 t1;
	private T2 t2;
	
	public Pair(T1 tempt1,T2 tempt2){
		t1 = tempt1;
		t2 = tempt2;
	}
	
	public T1 getT1(){
		return t1;
	}
	public T2 getT2(){
		return t2;
	}
	
	
}
